/*
 * Copyright 2016 GLodi
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

package giuliolodi.gitnav;


import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import giuliolodi.gitnav.Adapters.RepoAdapter;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SearchRepos {

    @BindView(R.id.search_repos_progress_bar) ProgressBar progressBar;
    @BindView(R.id.search_repos_rv) RecyclerView recyclerView;
    @BindView(R.id.no_repositories) TextView noRepositories;

    private RepoAdapter repoAdapter;
    private LinearLayoutManager linearLayoutManager;

    private String query;
    private List<Repository> repositoryList;
    private Context context;

    private Observable observable;
    private Observer observer;
    private Subscription s;

    private boolean PREVENT_MULTIPLE_SEPARATOR_LINE;
    private boolean LOADING = false;

    public void populate(String query, Context context, View v, boolean PREVENT_MULTIPLE_SEPARATOR_LINE) {
        this.query = query;
        this.context = context;
        this.PREVENT_MULTIPLE_SEPARATOR_LINE = PREVENT_MULTIPLE_SEPARATOR_LINE;
        ButterKnife.bind(this, v);
        LOADING = true;

        progressBar.setVisibility(View.VISIBLE);
        noRepositories.setVisibility(View.INVISIBLE);

        observable = Observable.create(new Observable.OnSubscribe<List<Repository>>() {
            @Override
            public void call(Subscriber<? super List<Repository>> subscriber) {
                RepositoryService repositoryService = new RepositoryService();
                repositoryService.getClient().setOAuth2Token(Constants.getToken(getContext()));

                try {
                    repositoryList = repositoryService.searchRepositories(getQuery());
                } catch (IOException e) {e.printStackTrace();}

                if (repositoryList != null)
                    subscriber.onNext(repositoryList);
                else
                    subscriber.onNext(null);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        observer = new Observer<List<Repository>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.d("RX", e.getMessage());
            }

            @Override
            public void onNext(List<Repository> repositories) {
                if (repositoryList == null || repositoryList.isEmpty())
                    noRepositories.setVisibility(View.VISIBLE);

                repoAdapter = new RepoAdapter(repositoryList, getContext());
                linearLayoutManager = new LinearLayoutManager(getContext());
                recyclerView.setLayoutManager(linearLayoutManager);
                if (getPrevent())
                    recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation()));
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(repoAdapter);
                progressBar.setVisibility(View.GONE);
                repoAdapter.notifyDataSetChanged();

                LOADING = false;
            }
        };

        s = observable.subscribe(observer);
    }

    public boolean isLOADING() {
        return LOADING;
    }

    private boolean getPrevent() {
        return PREVENT_MULTIPLE_SEPARATOR_LINE;
    }

    private Context getContext() {
        return context;
    }

    private String getQuery() {
        return query;
    }

    public void unsubSearchRepos() {
        if (s != null && !s.isUnsubscribed()){
            s.unsubscribe();
            progressBar.setVisibility(View.GONE);
        }
    }

}
