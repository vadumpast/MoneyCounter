package com.example.moneycounter.features.analytics

import com.example.moneycounter.R
import com.example.moneycounter.base.BasePresenter

class AnalyticsPresenter: BasePresenter<AnalyticsContract>() {

    fun onBackButtonClicked(){
        rootView?.openLastFragment()
    }

    fun onGraphPageSelected(){
        rootView?.setTitleText(R.string.title_analytics)
    }

    fun onListPageSelected(){
        rootView?.setTitleText(R.string.home_text_income)
    }
}