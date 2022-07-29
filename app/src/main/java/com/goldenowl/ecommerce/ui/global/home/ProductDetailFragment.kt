package com.goldenowl.ecommerce.ui.global.home

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.adapter.HomeProductListAdapter
import com.goldenowl.ecommerce.adapter.ImageProductDetailAdapter
import com.goldenowl.ecommerce.databinding.FragmentProductDetailBinding
import com.goldenowl.ecommerce.models.data.Cart
import com.goldenowl.ecommerce.models.data.Favorite
import com.goldenowl.ecommerce.models.data.Product
import com.goldenowl.ecommerce.models.data.ProductData
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment
import com.goldenowl.ecommerce.utils.Constants
import com.goldenowl.ecommerce.utils.Constants.listSize
import com.goldenowl.ecommerce.utils.Utils.autoScroll
import com.goldenowl.ecommerce.utils.Utils.prepareForTwoWayPaging


class ProductDetailFragment : BaseHomeFragment<FragmentProductDetailBinding>() {

    private lateinit var relateProductAdapter: HomeProductListAdapter
    private lateinit var product: Product
    private lateinit var productData: ProductData

    private var favorite: Favorite? = null
    private var relateList: List<ProductData> = emptyList()
    private val handler = Handler(Looper.getMainLooper())

    private var listProductData: List<ProductData> = emptyList()

    override fun setObservers() {
        viewModel.listProductData.observe(viewLifecycleOwner) { it ->
            listProductData = it
            listProductData.find {
                it.product.id == product.id
            }.also {
                favorite = it?.favorite
                if (favorite != null)
                    binding.ivFavorite.setImageResource(R.drawable.ic_favorites_selected)
                else
                    binding.ivFavorite.setImageResource(R.drawable.ic_favorites_bold)
            }
            relateList = viewModel.getRelateProducts(product.tags)
            relateProductAdapter.setData(relateList, null)
        }

        viewModel.allFavorite.observe(viewLifecycleOwner) {
            viewModel.reloadListProductData()
        }

    }

    override fun init() {
        val data = arguments?.get("product_data")
        val productId = arguments?.get(Constants.KEY_PRODUCT_ID)
        if (data != null) {
            productData = data as ProductData

        } else if (productId != null) {
            Log.d(TAG, "init: $productId")
            val pData = viewModel.listProductData.value?.find {
                it.product.id == productId as String
            }
            if (pData != null)
                productData = pData
        }
        product = productData.product
        favorite = productData.favorite
    }


    override fun setViews() {

        with(binding) {

            productDescription.text = product.description
            productRatingBar.rating = product.reviewStars.toFloat()
            tvNumberReviews.text = "(${product.numberReviews})"
            productBrand.text = product.brandName
            productPrice.text =
                binding.root.context.resources.getString(R.string.money_unit_float, product.getDiscountPrice())
            productTitle.text = product.title
            layoutRating.setOnClickListener {
                findNavController().navigate(R.id.action_go_review, bundleOf(Constants.KEY_PRODUCT to productData))
            }
        }

        /* drop down menu*/
        val adapterSize = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listSize)
        val adapterColor =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, product.getListColor())
        (binding.menuSize.editText as? AutoCompleteTextView)?.setAdapter(adapterSize)
        (binding.menuColor.editText as? AutoCompleteTextView)?.setAdapter(adapterColor)

        if (favorite != null) {
            binding.tvColor.setText(favorite!!.color, false)
            binding.tvSize.setText(favorite!!.size, false)
        }

        /* viewpager*/
        binding.viewPager.adapter = ImageProductDetailAdapter(product.images.prepareForTwoWayPaging())
        binding.viewPager.autoScroll(handler, Constants.AUTO_SCROLL)
        /* recyclerView*/
        relateProductAdapter = HomeProductListAdapter(this)
        val relateProducts = getRelateProducts()
        relateProductAdapter.setData(relateProducts, null)
        binding.rcvProducts.adapter = relateProductAdapter
        binding.rcvProducts.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        binding.tvNumItem.text =
            resources.getQuantityString(R.plurals.num_item, relateProducts.size, relateProducts.size)

        binding.btnAddToCart.setOnClickListener {
            if (!viewModel.isLoggedIn()) {
                toggleDialogLogIn()
                return@setOnClickListener
            }
            val size = binding.menuSize.editText?.text.toString()
            val color = binding.menuColor.editText?.text.toString()
            if (color.isNullOrBlank()) {
                showToast(getString(R.string.please_select_color))
                binding.menuColor.requestFocus()
                (binding.menuColor.editText as? AutoCompleteTextView)?.showDropDown()
            } else {
                toggleBottomSheetInsertCart(
                    product, Cart(product.id, size, color, 1)
                )
            }
        }
        /* insert favorite btn */
        binding.ivFavorite.setOnClickListener {
            this.onClickFavorite(product, favorite)
        }
    }

    private fun getRelateProducts(): List<ProductData> {
        return viewModel.getRelateProducts(product.tags)
    }


    override fun setAppbar() {
        binding.topAppBar.toolbar.apply {

            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
            setOnMenuItemClickListener {
                false
            }
        }
        Log.d(TAG, "product.categoryName: ${product.categoryName}")
        binding.topAppBar.toolbar.title = product.categoryName
    }


    private fun getListCategory(): List<String> {
        return viewModel.categoryList.toList()
    }


    companion object {
        const val TAG = "ProductDetailFragment"
    }

    override fun getViewBinding(): FragmentProductDetailBinding {
        return FragmentProductDetailBinding.inflate(layoutInflater)
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


