package com.goldenowl.ecommerce.ui.global.home

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.adapter.HomeProductListAdapter
import com.goldenowl.ecommerce.adapter.HomeViewPagerAdapter
import com.goldenowl.ecommerce.databinding.FragmentHomeBinding
import com.goldenowl.ecommerce.models.data.ProductData
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment
import com.goldenowl.ecommerce.ui.global.MainActivity
import com.goldenowl.ecommerce.utils.BaseLoadingStatus
import com.goldenowl.ecommerce.utils.Constants
import com.goldenowl.ecommerce.utils.Utils.autoScroll
import com.goldenowl.ecommerce.utils.Utils.getColor
import com.goldenowl.ecommerce.utils.Utils.prepareForTwoWayPaging

class HomeFragment : BaseHomeFragment<FragmentHomeBinding>() {
    val TAG: String = "HomeFragment"

    private lateinit var salesListAdapter: HomeProductListAdapter
    private lateinit var newsListAdapter: HomeProductListAdapter
    private lateinit var viewPagerAdapter: HomeViewPagerAdapter

    private var listProductData: List<ProductData> = emptyList()
    private val handler = Handler(Looper.getMainLooper())
    private var mListAppbarImg: List<Pair<String, String>> = emptyList()

    override fun setObservers() {
        super.setObservers()
        viewModel.dataReady.observe(viewLifecycleOwner) {
            if (it == BaseLoadingStatus.LOADING) {
                binding.layoutLoading.loadingFrameLayout.visibility = View.VISIBLE
                (requireActivity() as MainActivity).showNavBar(false)
            } else {
                binding.layoutLoading.loadingFrameLayout.visibility = View.INVISIBLE
                (requireActivity() as MainActivity).showNavBar(true)
            }

            viewModel.listProductData.observe(viewLifecycleOwner) {
                listProductData = it
                salesListAdapter.setData(listProductData, Constants.KEY_SALE)
                newsListAdapter.setData(listProductData, Constants.KEY_NEW)
            }

            viewModel.allFavorite.observe(viewLifecycleOwner) {
                viewModel.reloadListProductData()
            }

            viewModel.listAppbarImg.observe(viewLifecycleOwner) { list ->
                mListAppbarImg = list
                if (list.isEmpty())
                    return@observe
                viewPagerAdapter.setData(list.prepareForTwoWayPaging())
                binding.topAppBar.viewPager.autoScroll(handler, Constants.AUTO_SCROLL)
            }
        }
    }


    override fun setViews() {
        salesListAdapter = HomeProductListAdapter(this)
        newsListAdapter = HomeProductListAdapter(this)
        binding.rcvSales.adapter = salesListAdapter
        binding.rcvSales.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rcvNew.adapter = newsListAdapter
        binding.rcvNew.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        binding.tvViewAllSale.setOnClickListener {
            findNavController().navigate(
                R.id.action_view_all,
                bundleOf(Constants.KEY_CATEGORY to Constants.KEY_SALE)
            )
        }

        binding.tvViewAllNew.setOnClickListener {
            findNavController().navigate(
                R.id.action_view_all,
                bundleOf(Constants.KEY_CATEGORY to Constants.KEY_NEW)
            )
        }
    }

    override fun getViewBinding(): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(layoutInflater)
    }

    override fun setAppbar() {
        binding.topAppBar.collapsingToolbarLayout.apply {
            title = getString(R.string.home)
            setCollapsedTitleTextColor(getColor(context, R.color.white) ?: 0xFFFFFF)
            setExpandedTitleColor(getColor(context, R.color.white) ?: 0xFFFFFF)
        }

        viewPagerAdapter = HomeViewPagerAdapter { title ->
            findNavController().navigate(R.id.category_dest, bundleOf(Constants.KEY_CATEGORY to title))
        }
        binding.topAppBar.viewPager.adapter = viewPagerAdapter
    }

    override fun onPause() {
        super.onPause()
        handler.removeMessages(0)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeMessages(0)
    }
}
