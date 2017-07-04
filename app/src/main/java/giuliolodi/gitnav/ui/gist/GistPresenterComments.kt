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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by giulio on 04/07/2017.
 */
class GistPresenterComments<V: GistContractComments.View> : BasePresenter<V>, GistContractComments.Presenter<V> {

    val TAG = "GistPresenterComments"

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun getComments(gistId: String) {
        getCompositeDisposable().add(getDataManager().getGistComments(gistId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { getView().showLoading() }
                .subscribe(
                        { gistCommentList ->
                            getView().showComments(gistCommentList)
                            getView().hideLoading()
                        },
                        { throwable ->
                            getView().showError(throwable.localizedMessage)
                            getView().hideLoading()
                            Timber.e(throwable)
                        }
                ))
    }

}