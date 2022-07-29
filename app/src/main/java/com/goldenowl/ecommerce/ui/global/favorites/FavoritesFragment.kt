package com.goldenowl.ecommerce.ui.global.favorites

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isEmpty
import androidx.recyclerview.widget.GridLayoutManager
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentFavoritesBinding
import com.goldenowl.ecommerce.models.data.ProductData
import com.goldenowl.ecommerce.ui.auth.LoginSignupActivity
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment
import com.goldenowl.ecommerce.utils.Constants
import com.goldenowl.ecommerce.utils.SortType
import com.goldenowl.ecommerce.utils.Utils.hideKeyboard
import com.goldenowl.ecommerce.viewmodels.FavoriteProductListAdapter
import com.goldenowl.ecommerce.viewmodels.SortFilterViewModel
import kotlinx.coroutines.*

class FavoritesFragment : BaseHomeFragment<FragmentFavoritesBinding>() {
    override fun getViewBinding(): FragmentFavoritesBinding {
        return FragmentFavoritesBinding.inflate(layoutInflater)
    }

    //        private val sortViewModel: SortFilterViewModel by activityViewModels()
//    private lateinit var sortViewModel: SortFilterViewModel
//    private val sortViewModel: SortFilterViewModel by viewModels()
    private val sortViewModel = SortFilterViewModel()


    private lateinit var adapterGrid: FavoriteProductListAdapter
    private lateinit var gridLayoutManager: GridLayoutManager

    private lateinit var listProductData: List<ProductData>
    private lateinit var listCategory: Set<String>
    private var filterType: String? = null
    private var sortType: SortType? = null
    private var searchTerm: String = ""


    private var searchView: SearchView? = null
    private var queryTextListener: SearchView.OnQueryTextListener? = null


    override fun setObservers() {
        viewModel.listProductData.observe(viewLifecycleOwner) {
            listProductData = it
            refreshList()
        }

        viewModel.allFavorite.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.tvNoProduct.visibility = View.VISIBLE
                binding.layoutContent.visibility = View.GONE
            } else {
                binding.tvNoProduct.visibility = View.GONE
                binding.layoutContent.visibility = View.VISIBLE
                viewModel.reloadListProductData()
            }
        }
        viewModel.allCart.observe(viewLifecycleOwner) {
            viewModel.reloadListProductData()
        }

        sortViewModel.filterType.observe(viewLifecycleOwner) {
            if (it == null) binding.topAppBar.collapsingToolbar.title = "My Favorites"
            else {
                filterType = it
                binding.topAppBar.collapsingToolbar.title = it
                refreshList()
            }
        }

        sortViewModel.sortType.observe(viewLifecycleOwner) {
            sortType = it
            refreshList()
        }

        sortViewModel.searchTerm.observe(viewLifecycleOwner) {
            searchTerm = it
            refreshList()
        }
    }

    private fun refreshList() {
        adapterGrid.setData(listProductData, filterType, sortType, searchTerm)
    }


    override fun init() {
        listCategory = viewModel.categoryList
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
        adapterGrid = FavoriteProductListAdapter(gridLayoutManager, this)

        binding.rcvCategoryGrid.adapter = adapterGrid
        binding.rcvCategoryGrid.layoutManager = gridLayoutManager

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
                }
            }
        }
    }

    private fun getListCategory(): List<String> {
        return viewModel.categoryList.toList()
    }


    override fun setAppbar() {
        setAppBarMenu()
    }
}





