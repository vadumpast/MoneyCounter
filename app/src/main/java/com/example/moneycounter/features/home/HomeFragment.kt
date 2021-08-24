package com.example.moneycounter.features.home

import android.content.Context
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.get
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.moneycounter.R
import com.example.moneycounter.app.App
import com.example.moneycounter.base.BaseFragment
import com.example.moneycounter.databinding.FragmentHomeBinding
import com.example.moneycounter.features.analytics.AnalyticsFragment
import com.example.moneycounter.features.category.CategoryFragment
import com.example.moneycounter.features.info.InfoFragment
import com.example.moneycounter.features.lock_settings.LockSettingsFragment
import com.example.moneycounter.features.write_to_us.WriteToUsFragment
import com.example.moneycounter.model.entity.ui.MoneyType
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.internal.NavigationMenuView
import com.ssynhtn.waveview.WaveView
import kotlin.collections.set


class HomeFragment: BaseFragment<FragmentHomeBinding>(), HomeContract {

    private val presenter: HomePresenter by lazy { HomePresenter() }
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var drawerToggle: ActionBarDrawerToggle

    private var lastTranslation = 0f
    private var isSidebarOpened = false

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun attachToPresenter() {
        presenter.attachView(this)
    }

    override fun initView() {
        setupChart()
        setupSideBar()
        initListeners()
        setupOnBackPressed()
    }

    override fun onPause() {
        super.onPause()
        pauseWaves()
    }

    override fun onResume() {
        super.onResume()
        resumeWaves()
        if(isSidebarOpened){
            presenter.onSlide(lastTranslation.toInt(), 1f)
        }
    }

    private fun setupOnBackPressed(){
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                   presenter.onBackClicked()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    /**
     * Contract
     */

    override fun vibrate(){
        val vibrator = App.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(100)
        }
    }

    override fun playSound(){
        val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtoneManager = RingtoneManager.getRingtone(
            context,
            notification
        )
        ringtoneManager.play()
    }



    override fun setWaves(wavesList: MutableList<WaveView.WaveData>) {
        for (wave in wavesList){
            binding.homeWaveView.addWaveData(wave)
        }
    }

    override fun pauseWaves() {
        binding.homeWaveView.pauseAnimation()
    }

    override fun resumeWaves() {
        binding.homeWaveView.resumeAnimation()
    }

    override fun startWaves() {
        binding.homeWaveView.startAnimation()
    }

    override fun setGeneral(sum: Int){
        binding.financeTextView.text = context?.getString(R.string.title_general)
        binding.financePercentView.text = sum.toString()
        binding.financeSumView.text = null
    }

    override fun setIncome(percent: Float, sum: Int){
        binding.financeTextView.text = context?.getString(R.string.title_income)
        var percentString = String.format("%.2f", percent)
        percentString += "%"
        binding.financePercentView.text = percentString
        binding.financeSumView.text = sum.toString()
    }

    override fun setCosts(percent: Float, sum: Int) {
        binding.financeTextView.text = context?.getString(R.string.title_costs)
        var percentString = String.format("%.2f", percent)
        percentString += "%"
        binding.financePercentView.text = percentString
        binding.financeSumView.text=sum.toString()
    }

    override fun setChartData(incomeAmount: Float, costsAmount: Float ){
        val pieEntries: ArrayList<PieEntry> = ArrayList()
        val label = "label"

        val pieChartDataMap: MutableMap<String, Float> = HashMap()
        pieChartDataMap[" "] = costsAmount
        pieChartDataMap["  "] = incomeAmount

        val colors: ArrayList<Int> = ArrayList()
        colors.add(App.context.getColor(R.color.costs_color))
        colors.add(App.context.getColor(R.color.light_blue))

        for (type in pieChartDataMap.keys) {
            pieEntries.add(PieEntry(pieChartDataMap[type]!!.toFloat(), type))
        }

        val pieDataSet = PieDataSet(pieEntries, label)
        pieDataSet.colors = colors
        pieDataSet.setDrawValues(false)
        pieDataSet.notifyDataSetChanged()

        val pieData = PieData(pieDataSet)
        pieData.notifyDataChanged()

        binding.homeFinanceChart.data = pieData
        binding.homeFinanceChart.notifyDataSetChanged()
        binding.homeFinanceChart.invalidate()
    }

    override fun openCategoriesIncome() {
        CategoryFragment.start(findNavController(), MoneyType.INCOME)
    }

    override fun openCategoriesCosts() {
        CategoryFragment.start(findNavController(), MoneyType.COSTS)
    }

    override fun openCategoriesAnalytics() {
        AnalyticsFragment.start(findNavController())
    }

    override fun openInfoFragment(){
        InfoFragment.start(findNavController())
    }

    override fun openLockSettingsFragment() {
        LockSettingsFragment.start(findNavController())
    }

    override fun openWriteToUsFragment(){
        WriteToUsFragment.start(findNavController())
    }

    override fun closeApp() {
        requireActivity().finishAndRemoveTask()
    }

    override fun onDataImported(incomeAmount: Float, costsAmount: Float){
        presenter.onDataImported(incomeAmount, costsAmount)
    }

    override fun openSideBar() {
        drawerLayout.open()
    }

    override fun closeSideBar() {
        drawerLayout.close()
    }

    override fun getIsSidebarOpened(): Boolean = isSidebarOpened

    override fun setItemEnabled(item: MenuItem, isEnabled: Boolean){
        if(isEnabled){
            item.icon.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                Color.TRANSPARENT,
                BlendModeCompat.SRC_ATOP
            )
        }else{
            item.icon.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                requireContext().getColor(R.color.unabled),
                BlendModeCompat.SRC_ATOP
            )
        }
    }

    override fun setItemClickable(item: MenuItem, isClickable: Boolean) {
        item.isEnabled = isClickable
    }

    override fun getSideBarMenuSize(): Int = binding.homeSidebar.menu.size()

    override fun getSideBarMenuItem(index: Int): MenuItem = binding.homeSidebar.menu[index]

    override fun setScrimAlpha(alpha: Float) {
        binding.scrimLayout.alpha = alpha
    }

    override fun setMainLayoutTranslation(translation: Float) {
        lastTranslation = translation
        binding.homeMainLayout.translationX = translation
    }

    /**
     * Help fun-s
     */

    private fun setupChart(){
        binding.homeFinanceChart.description.isEnabled=false
        binding.homeFinanceChart.legend.isEnabled = false
        binding.homeFinanceChart.isRotationEnabled = false
        binding.homeFinanceChart.isHighlightPerTapEnabled = true


        binding.homeFinanceChart.setHoleColor(requireContext().getColor(R.color.transparent))
        binding.homeFinanceChart.holeRadius = 90f

        binding.homeFinanceChart.setNoDataText(requireContext().getString(R.string.wait))
        binding.homeFinanceChart.setNoDataTextColor(requireContext().getColor(R.color.light_blue))
        binding.homeFinanceChart.renderer.paintRender.setShadowLayer(
            1f,
            0f,
            16f,
            ContextCompat.getColor(requireContext(),
                R.color.shadow)
        )
    }

    private fun setupSideBar(){
        toolbar = binding.homeToolbar

        val activity = activity as AppCompatActivity
        activity.setSupportActionBar(toolbar)

        drawerLayout = binding.homeDrawerLayout
        drawerLayout.setScrimColor(requireContext().getColor(R.color.transparent))
        drawerLayout.drawerElevation = 0f

        drawerToggle = ActionBarDrawerToggle(
            getActivity(),
            drawerLayout,
            R.string.add,
            R.string.app_name
        )

        drawerToggle.syncState()


        val navMenuView = binding.homeSidebar.getChildAt(0) as NavigationMenuView
        navMenuView.addItemDecoration(
            com.example.moneycounter.ui.adapter.DividerItemDecoration(requireContext())
        )

        binding.homeSidebar.itemIconTintList = null

        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener{
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                presenter.onSlide(drawerView.width, slideOffset)
            }

            override fun onDrawerOpened(drawerView: View) {
                isSidebarOpened = true
            }

            override fun onDrawerClosed(drawerView: View) {
                isSidebarOpened = false
            }

            override fun onDrawerStateChanged(newState: Int) {}
        })

    }

    private fun initListeners() {
        binding.buttonHomeIncome.setOnClickListener {
            presenter.onButtonIncomeClicked()
        }
        binding.buttonHomeCosts.setOnClickListener {
            presenter.onButtonCostsClicked()
        }
        binding.buttonHomeAnalytics.setOnClickListener {
            presenter.onButtonAnalyticsClicked()
        }
        binding.homeFinanceChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener
        {
            override fun onValueSelected(e: Entry, h: Highlight?) {
                presenter.onFinanceSelected(e.y)
            }

            override fun onNothingSelected() {
                presenter.onNothingSelected()
            }
        })
        binding.homeMenuButton.setOnClickListener {
            presenter.onMenuButtonClicked()
        }

        binding.homeSidebar.setNavigationItemSelectedListener {
            presenter.onSidebarItemSelected(it)
            true
        }
    }

    companion object {
        fun start(navController: NavController) {
            navController.navigate(R.id.action_to_home)
        }
    }
}