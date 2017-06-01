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

package giuliolodi.gitnav.ui.gistlist

import giuliolodi.gitnav.di.scope.PerActivity
import giuliolodi.gitnav.ui.base.BaseContract
import org.eclipse.egit.github.core.Gist

/**
 * Created by giulio on 23/05/2017.
 */

interface GistListContract {

    interface View : BaseContract.View {

        fun showMineGists(gistList: List<Gist>)

        fun showStarredGists(gistList: List<Gist>)

        fun showLoadingMine()

        fun showLoadingStarred()

        fun hideLoadingMine()

        fun hideLoadingStarred()

        fun showError(error: String)

    }

    @PerActivity
    interface Presenter<V: GistListContract.View> : BaseContract.Presenter<V> {

        fun getMineGists(pageN: Int, itemsPerPage: Int)

        fun getStarredGists(pageN: Int, itemsPerPage: Int)

    }

}