/*
 * Copyright 2017 GLodi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package giuliolodi.gitnav.ui.gist

import giuliolodi.gitnav.data.DataManager
import giuliolodi.gitnav.ui.base.BasePresenter
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import org.eclipse.egit.github.core.Gist
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by giulio on 26/05/2017.
 */
class GistPresenter<V: GistContract.View> : BasePresenter<V>, GistContract.Presenter<V> {

    val TAG = "GistPresenter"

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun subscribe(gistId: String) {
        getCompositeDisposable().add(Flowable.zip< Gist,Boolean,Map<Gist,Boolean>>(
                getDataManager().getGist(gistId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()),
                getDataManager().isGistStarred(gistId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()),
                BiFunction { gist, boolean -> return@BiFunction mapOf(gist to boolean) })
                .doOnSubscribe { getView().showLoading() }
                .subscribe(
                        { map ->
                            getView().showGist(map)
                            getView().hideLoading()
                        },
                        { throwable ->
                            getView().showError(throwable.localizedMessage)
                            getView().hideLoading()
                            Timber.e(throwable)
                        }
                ))
    }

    override fun getComments(gistId: String) {
        getCompositeDisposable().add(getDataManager().getGistComments(gistId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { getView().showLoadingComments() }
                .subscribe(
                        { gistCommentList ->
                            getView().showComments(gistCommentList)
                            getView().hideLoadingComments()
                        },
                        { throwable ->
                            getView().showError(throwable.localizedMessage)
                            getView().hideLoadingComments()
                            Timber.e(throwable)
                        }
                ))
    }

    override fun starGist(gistId: String) {
        getCompositeDisposable().add(getDataManager().starGist(gistId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { getView().onGistStarred() },
                        { throwable ->
                            getView().showError(throwable.localizedMessage)
                            Timber.e(throwable)
                        }
                ))
    }

    override fun unstarGist(gistId: String) {
        getCompositeDisposable().add(getDataManager().unstarGist(gistId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { getView().onGistUnstarred() },
                        { throwable ->
                            getView().showError(throwable.localizedMessage)
                            Timber.e(throwable)
                        }
                ))
    }

}