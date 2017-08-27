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

package giuliolodi.gitnav.ui.stargazerlist

import giuliolodi.gitnav.di.scope.PerActivity
import giuliolodi.gitnav.ui.base.BaseContract
import org.eclipse.egit.github.core.User

/**
 * Created by giulio on 25/08/2017.
 */
interface StargazerListContract {

    interface View : BaseContract.View {

        fun showUserList(userList: List<User>)

    }

    @PerActivity
    interface Presenter<V: StargazerListContract.View> : BaseContract.Presenter<V> {

        fun subscribe(repoOwner: String?, repoName: String?, isNetworkAvailable: Boolean)

    }

}