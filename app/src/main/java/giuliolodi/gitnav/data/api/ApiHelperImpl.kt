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

package giuliolodi.gitnav.data.api

import android.content.Context
import android.os.Build
import android.os.StrictMode
import giuliolodi.gitnav.di.scope.AppContext
import giuliolodi.gitnav.di.scope.UrlInfo
import io.reactivex.Observable
import org.eclipse.egit.github.core.Authorization
import org.eclipse.egit.github.core.Repository
import org.eclipse.egit.github.core.User
import org.eclipse.egit.github.core.event.Event
import org.eclipse.egit.github.core.service.*
import javax.inject.Inject
import java.io.IOException
import org.jsoup.Jsoup
import org.jsoup.nodes.Element


/**
 * Created by giulio on 12/05/2017.
 */

class ApiHelperImpl : ApiHelper {

    private val mContext: Context
    private val mUrlMap: Map<String,String>

    @Inject
    constructor(@AppContext context: Context, @UrlInfo urlMap: Map<String,String>) {
        mContext = context
        mUrlMap = urlMap
    }

    override fun apiAuthToGitHub(username: String, password: String): String {
        val oAuthService: OAuthService = OAuthService()
        oAuthService.client.setCredentials(username, password)

        // This will set the token parameters and its permissions
        var auth = Authorization()
        auth.scopes = arrayListOf("repo", "gist", "user")
        val description = "GitNav - " + Build.MANUFACTURER + " " + Build.MODEL
        auth.note = description

        // Required for some reason
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        // Check if token already exists and deletes it.
        try {
            for (authorization in oAuthService.authorizations) {
                if (authorization.note == description) {
                    oAuthService.deleteAuthorization(authorization.id)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }

        // Create authorization
        try {
            auth = oAuthService.createAuthorization(auth)
            return auth.token
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }
    }

    override fun apiGetUser(token: String, username: String): Observable<User> {
        return Observable.defer {
            val userService: UserService = UserService()
            userService.client.setOAuth2Token(token)
            Observable.just(userService.getUser(username))
        }
    }

    override fun apiPageEvents(token: String, username: String, pageN: Int, itemsPerPage: Int): Observable<List<Event>> {
        return Observable.defer {
            val eventService: EventService = EventService()
            eventService.client.setOAuth2Token(token)
            Observable.just(ArrayList(eventService.pageUserReceivedEvents(username, false, pageN, itemsPerPage).next()))
        }
    }

    override fun apiPageRepos(token: String, username: String, pageN: Int, itemsPerPage: Int, filter: HashMap<String, String>?): Observable<List<Repository>> {
        return Observable.defer {
            val repositoryService: RepositoryService = RepositoryService()
            repositoryService.client.setOAuth2Token(token)
            if (filter?.get("sort") == "starred")
                Observable.just(ArrayList(repositoryService.getRepositories(username).sortedByDescending { it.watchers }))
            else
                Observable.just(ArrayList(repositoryService.pageRepositories(username, filter, pageN, itemsPerPage).next()))
        }
    }

    override fun apiGetTrending(token: String, period: String): Observable<Repository> {
        return Observable.create { disposable ->
            var URL: String = ""
            val ownerRepoList: MutableList<String> = mutableListOf()
            when (period) {
                "daily" ->  URL = mUrlMap["base"] + mUrlMap["daily"]
                "weekly" ->  URL = mUrlMap["base"] + mUrlMap["weekly"]
                "monthly" ->  URL = mUrlMap["base"] + mUrlMap["monthly"]
            }
            try {
                val document = Jsoup.connect(URL).get()
                val repoList = document.getElementsByTag("ol")[0].getElementsByTag("li")
                if (repoList != null && !repoList.isEmpty()) {
                    var string: Element
                    var ss: String
                    for (i in 0..repoList.size - 1) {
                        string = repoList[i].getElementsByTag("div")[0].getElementsByTag("h3")[0].getElementsByTag("a")[0]
                        ss = string.children()[0].ownText() + string.ownText()
                        val t = ss.split("/")
                        val a = t[0].replace(" ", "")
                        val b = t[1]
                        ownerRepoList.add(a)
                        ownerRepoList.add(b)
                    }
                    val repositoryService: RepositoryService = RepositoryService()
                    repositoryService.client.setOAuth2Token(token)
                    for (i in 0..ownerRepoList.size - 1 step 2) {
                        disposable.onNext(repositoryService.getRepository(ownerRepoList[i], ownerRepoList[i+1]))
                    }
                    disposable.onComplete()
                }
            } catch (e: Exception) {
                if (!disposable.isDisposed)
                    disposable.onError(e)
            }
        }
    }

    override fun apiPageStarred(token: String, username: String, pageN: Int, itemsPerPage: Int, filter: HashMap<String, String>?): Observable<List<Repository>> {
        return Observable.defer {
            val starService: StarService = StarService()
            starService.client.setOAuth2Token(token)
            if (filter?.get("sort") == "starred")
                Observable.just(ArrayList(starService.getStarred(username).sortedByDescending { it.watchers }))
            else
                Observable.just(ArrayList(starService.pageStarred(username, filter, pageN, itemsPerPage).next()))
        }
    }

    override fun apiGetFollowed(token: String, username: String): Observable<Boolean> {
        return Observable.defer {
            val userService: UserService = UserService()
            userService.client.setOAuth2Token(token)
            Observable.just(userService.isFollowing(username))
        }
    }

    override fun apiGetFollowers(token: String, username: String?, pageN: Int, itemsPerPage: Int): Observable<List<User>> {
        return Observable.defer {
            val userService: UserService = UserService()
            userService.client.setOAuth2Token(token)
            Observable.just(ArrayList(userService.pageFollowers(username, pageN, itemsPerPage).next()))
        }
    }

    override fun apiGetFollowing(token: String, username: String?, pageN: Int, itemsPerPage: Int): Observable<List<User>> {
        return Observable.defer {
            val userService: UserService = UserService()
            userService.client.setOAuth2Token(token)
            Observable.just(ArrayList(userService.pageFollowing(username, pageN, itemsPerPage).next()))
        }
    }

}
