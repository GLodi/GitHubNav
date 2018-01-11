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

package giuliolodi.gitnav.ui.option

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import kotlinx.android.synthetic.main.option_fragment.*
import javax.inject.Inject

/**
 * Created by giulio on 07/10/2017.
 */
class OptionFragment : BaseFragment(), OptionContract.View {

    @Inject lateinit var mPresenter: OptionContract.Presenter<OptionContract.View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        getActivityComponent()?.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.option_fragment, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)
        activity?.title = getString(R.string.options)

        if (mPresenter.getTheme() == "light"){
            option_fragment_theme.text = getString(R.string.themelight)
            option_fragment_theme_switch.isChecked = false
        }
        else {
            option_fragment_theme.text = getString(R.string.themedark)
            option_fragment_theme_switch.isChecked = true
        }
        option_fragment_theme_switch.setOnClickListener {
            if (mPresenter.getTheme() == "light") {
                mPresenter.changeTheme("dark")
                Handler().postDelayed({
                    startActivity(OptionActivity.getIntent(context))
                    activity.finish()
                    activity.overridePendingTransition(0,0)
                }, 150L)
            }
            else {
                mPresenter.changeTheme("light")
                Handler().postDelayed({
                    startActivity(OptionActivity.getIntent(context))
                    activity.finish()
                    activity.overridePendingTransition(0,0)
                }, 150L)
            }
        }
    }

    override fun onThemeChanged() {
        Toasty.success(context, getString(R.string.theme_switched), Toast.LENGTH_LONG).show()
    }

}