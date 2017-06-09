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

package giuliolodi.gitnav.ui.user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.squareup.picasso.Picasso
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseActivity
import giuliolodi.gitnav.ui.repositorylist.RepoListAdapter
import kotlinx.android.synthetic.main.user_activity2.*
import kotlinx.android.synthetic.main.user_activity_content.*
import org.eclipse.egit.github.core.Repository
import org.eclipse.egit.github.core.User
import javax.inject.Inject

/**
 * Created by giulio on 03/06/2017.
 */

class UserActivity2 : BaseActivity(), UserContract2.View {

    @Inject lateinit var mPresenter: UserContract2.Presenter<UserContract2.View>

    private lateinit var mUser: User
    private lateinit var username: String

    private var IS_FOLLOWED: Boolean = false
    private var IS_LOGGED_USER: Boolean = false

    private var mFilterRepos: HashMap<String,String> = HashMap()
    private var PAGE_N_REPOS = 1
    private val ITEMS_PER_PAGE_REPOS = 10
    private var LOADING_REPOS = false

    private var PAGE_N_FOLLOWERS = 1
    private val ITEMS_PER_PAGE_FOLLOWERS = 10
    private var LOADING_FOLLOWERS = false

    private var PAGE_N_FOLLOWING = 1
    private val ITEMS_PER_PAGE_FOLLOWING = 10
    private var LOADING_FOLLOWING = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_activity2)

        initLayout()

        username = intent.getStringExtra("username")

        getActivityComponent().inject(this)

        mPresenter.onAttach(this)

        if (isNetworkAvailable())
            mPresenter.subscribe(username)
        else
            Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show()
    }

    private fun initLayout() {
        setSupportActionBar(user_activity2_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        user_activity2_bottomnv.selectedItemId = R.id.user_activity_bottom_menu_info
        user_activity2_bottomnv.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.user_activity_bottom_menu_following -> {
                    user_activity2_appbar.setExpanded(false)
                    user_activity2_nestedscrollview.isNestedScrollingEnabled = false
                }
                R.id.user_activity_bottom_menu_followers -> {
                    user_activity2_appbar.setExpanded(false)
                    user_activity2_nestedscrollview.isNestedScrollingEnabled = false
                }
                R.id.user_activity_bottom_menu_info -> {
                    user_activity2_appbar.setExpanded(true)
                    user_activity2_nestedscrollview.isNestedScrollingEnabled = true
                }
                R.id.user_activity_bottom_menu_repos -> {
                    user_activity2_appbar.setExpanded(false)
                    user_activity2_nestedscrollview.isNestedScrollingEnabled = false
                    onRepoNavClick()
                }
                R.id.user_activity_bottom_menu_events -> {
                    user_activity2_appbar.setExpanded(false)
                    user_activity2_nestedscrollview.isNestedScrollingEnabled = false
                }
            }
            true
        }
        user_activity2_bottomnv.setOnNavigationItemReselectedListener {  }
    }

    override fun showUser(mapUserFollowed: Map<User, String>) {
        mUser = mapUserFollowed.keys.first()
        if (mapUserFollowed[mUser] == "f")
            IS_FOLLOWED = true
        else if (mapUserFollowed[mUser] == "u")
            IS_LOGGED_USER = true

        user_activity2_fab.visibility = View.VISIBLE

        if (IS_FOLLOWED)
            user_activity2_fab.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_star_full_24dp))
        else
            user_activity2_fab.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_star_empty_24dp))

        user_activity2_collapsing_toolbar.title = mUser.name ?: mUser.login
        Picasso.with(applicationContext).load(mUser.avatarUrl).into(user_activity2_image)
    }

    private fun onRepoNavClick() {
        mFilterRepos.put("sort","created")

        val llmRepos = LinearLayoutManager(applicationContext)
        llmRepos.orientation = LinearLayoutManager.VERTICAL
        user_activity_content_rv.layoutManager = llmRepos
        user_activity_content_rv.addItemDecoration(HorizontalDividerItemDecoration.Builder(this).showLastDivider().build())
        user_activity_content_rv.itemAnimator = DefaultItemAnimator()
        user_activity_content_rv.adapter = RepoListAdapter()
        (user_activity_content_rv.adapter as RepoListAdapter).setFilter(mFilterRepos)

        val mScrollListenerRepos = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (LOADING_REPOS || mFilterRepos["sort"] == "stars")
                    return
                val visibleItemCount = (user_activity_content_rv.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (user_activity_content_rv.layoutManager as LinearLayoutManager).itemCount
                val pastVisibleItems = (user_activity_content_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    if (isNetworkAvailable()) {
                        LOADING_REPOS = true
                        PAGE_N_REPOS += 1
                        (user_activity_content_rv.adapter as RepoListAdapter).addLoading()
                        mPresenter.getRepos(username, PAGE_N_REPOS, ITEMS_PER_PAGE_REPOS, mFilterRepos)
                    } else if (dy > 0) {
                        Handler(Looper.getMainLooper()).post({ Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show() })
                    }
                }
            }
        }
        user_activity_content_rv.setOnScrollListener(mScrollListenerRepos)

        showLoading()
        mPresenter.getRepos(mUser.login, PAGE_N_REPOS, ITEMS_PER_PAGE_REPOS, mFilterRepos)
    }

    override fun showUserRepos(repoList: List<Repository>) {
        (user_activity_content_rv.adapter as RepoListAdapter).addRepos(repoList)
        if (PAGE_N_REPOS == 1 && repoList.isEmpty()) {
            user_activity_content_no.visibility = View.VISIBLE
            user_activity_content_no.text = getString(R.string.no_repositories)
        }
        LOADING_REPOS = false
    }

    override fun showLoading() {
        user_activity_content_progress_bar.visibility = View.VISIBLE

    }

    override fun hideLoading() {
        if (user_activity_content_progress_bar.visibility == View.VISIBLE)
            user_activity_content_progress_bar.visibility = View.GONE
    }

    override fun showError(error: String) {
        Toasty.error(applicationContext, error, Toast.LENGTH_LONG).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0,0)
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, UserActivity2::class.java)
        }
    }

}