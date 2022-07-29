package com.goldenowl.ecommerce.ui.global.bag

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.core.view.isEmpty
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentBagBinding
import com.goldenowl.ecommerce.models.data.ProductData
import com.goldenowl.ecommerce.ui.auth.LoginSignupActivity
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment
import com.goldenowl.ecommerce.utils.BaseLoadingStatus
import com.goldenowl.ecommerce.utils.Constants
import com.goldenowl.ecommerce.utils.Utils.hideKeyboard
import com.goldenowl.ecommerce.viewmodels.BagProductListAdapter
import com.goldenowl.ecommerce.viewmodels.SortFilterViewModel
import kotlinx.coroutines.*

class BagFragment : BaseHomeFragment<FragmentBagBinding>() {
    override fun getViewBinding(): FragmentBagBinding {
        return FragmentBagBinding.inflate(layoutInflater)
    }

    private val sortViewModel = SortFilterViewModel()


    private lateinit var adapterGrid: BagProductListAdapter
    private lateinit var gridLayoutManager: GridLayoutManager

    private lateinit var listProductData: List<ProductData>
    private lateinit var listCategory: Set<String>
    private var searchTerm: String = ""


    private var searchView: SearchView? = null
    private var queryTextListener: SearchView.OnQueryTextListener? = null

    private var totalPrice = 0f

    override fun setObservers() {
        viewModel.listProductData.observe(viewLifecycleOwner) {
            listProductData = it
            refreshList()
        }

        viewModel.allFavorite.observe(viewLifecycleOwner) {
            viewModel.reloadListProductData()
        }
        viewModel.allCart.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.tvNoProduct.visibility = View.VISIBLE
                binding.layoutContent.visibility = View.GONE
                binding.layoutCheckout.visibility = View.GONE

            } else {
                binding.tvNoProduct.visibility = View.GONE
                binding.layoutContent.visibility = View.VISIBLE
                binding.layoutCheckout.visibility = View.VISIBLE
                viewModel.reloadListProductData()
                setPrice()
            }
        }

        viewModel.curBag.observe(viewLifecycleOwner) {
            binding.tvPromoCode.text = it?.promo?.id
            setPrice()
        }

        sortViewModel.searchTerm.observe(viewLifecycleOwner) {
            searchTerm = it
            refreshList()
        }
    }

    private fun setPrice() {
        totalPrice = adapterGrid.getPrice(viewModel.curBag.value?.promo?.salePercent ?: 0)
        binding.tvTotal.text = getString(R.string.money_unit_float, totalPrice)
        val bag = viewModel.curBag.value
        viewModel.orderPrice.value = totalPrice
    }

    private fun refreshList() {
        adapterGrid.setData(listProductData, searchTerm)
    }


    override fun init() {
        listProductData = viewModel.listProductData.value ?: emptyList()
    }

    override fun setViews() {
        if (!viewModel.isLoggedIn()) {
            binding.layoutContent.visibility = View.GONE
            binding.layoutNotLogIn.layoutNotLogIn.visibility = View.VISIBLE
            binding.layoutNotLogIn.layoutLogInToContinue.setOnClickListener {
                startActivity(Intent(requireContext(), LoginSignupActivity::class.java))
            }
        }
        gridLayoutManager = GridLayoutManager(context, Constants.SPAN_COUNT_ONE)
        adapterGrid = BagProductListAdapter(this)

        binding.rcvCategoryGrid.adapter = adapterGrid
        binding.rcvCategoryGrid.layoutManager = gridLayoutManager

        binding.layoutPromo.setOnClickListener {
            toggleBottomSheetEnterPromo()
        }

        binding.btnCheckOut.setOnClickListener {
            checkOut()
        }
    }

    private fun checkOut() {
        val listCartProductData = adapterGrid.getBag()
        viewModel.loadingStatus.value = BaseLoadingStatus.NONE
        findNavController().navigate(
            R.id.checkout_dest, bundleOf(
                "listCartProductData" to listCartProductData
            )
        )
    }


    private fun setAppBarMenu() {
        binding.topAppBar.toolbar.apply {
            if (menu.isEmpty()) {
                inflateMenu(R.menu.menu_search)
                val searchItem = menu.findItem(R.id.ic_search)
                val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager

                if (searchItem != null) {
                    searchView = searchItem.actionView as SearchView
                }
                if (searchView != null) {
                    val debounceJob: Job? = null
                    val uiScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
                    var lastInput = ""

                    searchView!!.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
                    queryTextListener = object : SearchView.OnQueryTextListener {
                        override fun onQueryTextChange(newText: String?): Boolean {
                            debounceJob?.cancel()
                            if (lastInput != newText) {
                                lastInput = newText ?: ""
                                uiScope.launch {
                                    delay(500)
                                    if (lastInput == newText) {
                                        sortViewModel.searchTerm.value = newText
                                    }
                                }
                            }
                            return true
                        }

                        override fun onQueryTextSubmit(query: String?): Boolean {
                            Log.d("onQueryTextSubmit", query!!)
                            hideKeyboard()
                            return true
                        }
                    }
                    searchView!!.setOnCloseListener {
                        false
                    }
                    searchView!!.maxWidth = Integer.MAX_VALUE
                    searchView!!.setOnQueryTextListener(queryTextListener)
                } else {
                    Log.d(TAG, "onCreateOptionsMenu: SEARCH VIEW NULL")
                }
            }
        }
    }


    companion object {
        const val TAG = "BagFragment"
    }

    override fun setAppbar() {
        binding.topAppBar.collapsingToolbar.title = getString(R.string.my_bag)
        setAppBarMenu()
    }
}
