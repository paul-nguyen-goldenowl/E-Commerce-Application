package com.goldenowl.ecommerce.viewmodels

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.models.data.ProductData
import com.goldenowl.ecommerce.ui.global.IClickListener
import com.goldenowl.ecommerce.utils.Constants
import com.goldenowl.ecommerce.utils.Constants.SPAN_COUNT_ONE
import com.goldenowl.ecommerce.utils.SortType
import com.goldenowl.ecommerce.utils.Utils.glide2View
import com.goldenowl.ecommerce.utils.Utils.strike


class FavoriteProductListAdapter(private val mLayoutManager: GridLayoutManager, private val listener: IClickListener) :
    RecyclerView.Adapter<FavoriteProductListAdapter.FavoriteProductViewHolder>() {

    private var mListProductData = listOf<ProductData>()
    private var mListFavoriteProductData = listOf<ProductData>()

    fun setData(listProductData: List<ProductData>, filterType: String?, sortType: SortType?, searchTerm: String) {
        mListProductData = listProductData
        mListFavoriteProductData = getFavoriteProduct()

        if (filterType != null)
            mListFavoriteProductData = mListFavoriteProductData.filter { it.product.categoryName == filterType }

        mListFavoriteProductData = when (sortType) {
            SortType.REVIEW -> mListFavoriteProductData.sortedByDescending { it.product.reviewStars }
            SortType.PRICE_DECREASE -> mListFavoriteProductData.sortedByDescending { it.product.getDiscountPrice() }
            SortType.PRICE_INCREASE -> mListFavoriteProductData.sortedBy { it.product.getDiscountPrice() }
            SortType.POPULAR -> mListFavoriteProductData.sortedByDescending { it.product.isPopular }
            SortType.NEWEST -> mListFavoriteProductData.sortedByDescending { it.product.createdDate }
            else -> mListFavoriteProductData
        }
        if (searchTerm.isNotBlank()) {

            mListFavoriteProductData = mListFavoriteProductData.filter {
                it.product.title.indexOf(searchTerm, ignoreCase = true) >= 0 || it.product.brandName.indexOf(
                    searchTerm,
                    ignoreCase = true
                ) >= 0
            }
        }

        notifyDataSetChanged()
    }

    private fun getFavoriteProduct(): List<ProductData> {
        return mListProductData.filter {
            it.favorite != null
        }
    }

    class FavoriteProductViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var productName: TextView? = null
        var productBrand: TextView? = null
        var tvSoldOut: TextView? = null
        var tvColor: TextView? = null
        var tvDiscoutPercent: TextView? = null
        var tvSize: TextView? = null
        var originPrice: TextView? = null
        var discountPrice: TextView? = null
        var tvNumberReviews: TextView? = null
        var productImg: ImageView? = null
        var productRatingBar: RatingBar? = null
        var ivCart: ImageView? = null
        var ivRemove: ImageView? = null
        var layoutLoading: FrameLayout? = null
        var layoutGreyOut: FrameLayout? = null

        init {
            productName = itemView.findViewById(R.id.product_name)
            productBrand = itemView.findViewById(R.id.product_brand)
            productImg = itemView.findViewById(R.id.product_img)
            productRatingBar = itemView.findViewById(R.id.product_rating_bar)
            tvNumberReviews = itemView.findViewById(R.id.tv_number_reviews)
            ivCart = itemView.findViewById(R.id.iv_cart)
            ivRemove = itemView.findViewById(R.id.iv_remove)
            layoutLoading = itemView.findViewById(R.id.layout_loading)
            layoutGreyOut = itemView.findViewById(R.id.layout_grey_out)
            tvColor = itemView.findViewById(R.id.tv_color)
            tvSize = itemView.findViewById(R.id.tv_size)
            tvSoldOut = itemView.findViewById(R.id.tv_sold_out)
            originPrice = itemView.findViewById(R.id.product_origin_price)
            discountPrice = itemView.findViewById(R.id.product_discount_price)
            tvDiscoutPercent = itemView.findViewById(R.id.tv_discount_percent)
        }
    }


    companion object {
        val TAG = "FavoriteAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteProductViewHolder {
        if (viewType == Constants.GRID_VIEW) {
            return FavoriteProductViewHolder(
                (LayoutInflater.from(parent.context)).inflate(
                    R.layout.item_product_list_favorite_grid,
                    parent,
                    false
                )
            )
        }
        return FavoriteProductViewHolder(
            (LayoutInflater.from(parent.context)).inflate(R.layout.item_product_list_favorite, parent, false)
        )
    }

    override fun onBindViewHolder(holder: FavoriteProductViewHolder, position: Int) {
        val productData = mListFavoriteProductData[position]
        val product = productData.product
        val cart = productData.cart
        val favorite = productData.favorite

        holder.ivCart?.setOnClickListener {
            listener.onClickCart(product, cart)
        }

        holder.ivRemove?.setOnClickListener {
            listener.onClickRemoveFavorite(product, favorite)
        }

        holder.itemView?.setOnClickListener {
            listener.onClickItem(productData)
        }
        holder.productBrand?.text = product.brandName
        holder.productName?.text = product.title

        glide2View(holder.productImg!!, holder.layoutLoading!!, product.getImage()!!)

        holder.tvNumberReviews?.text = "(${product.numberReviews})"
        holder.tvSize?.text = favorite?.size
        holder.tvColor?.text = favorite?.color

        product.salePercent.let {
            if (it != null) {
                holder.tvDiscoutPercent?.apply {
                    visibility = View.VISIBLE
                    text = "$it%"
                }
                holder.discountPrice?.visibility = View.VISIBLE
                holder.discountPrice?.text =
                    holder.itemView.context.resources.getString(R.string.money_unit_float, product.getDiscountPrice())
                val text =
                    holder.itemView.context.resources.getString(R.string.money_unit_int, product.getOriginPrice())
                holder.originPrice?.strike(text)
            } else {
                holder.tvDiscoutPercent?.visibility = View.INVISIBLE
                holder.discountPrice?.visibility = View.INVISIBLE
                holder.originPrice?.text =
                    holder.itemView.context.resources.getString(R.string.money_unit_int, product.getOriginPrice())
                holder.originPrice?.paintFlags = Paint.ANTI_ALIAS_FLAG
            }
        }
        holder.productRatingBar?.rating = product.reviewStars.toFloat()
        if (product.isAvailable(favorite!!)) {
            if (cart != null) {
                holder.ivCart?.apply {
                    backgroundTintList = this.resources.getColorStateList(R.color.red_dark, this.context.theme)
                }

            } else {
                holder.ivCart?.apply {
                    backgroundTintList = this.resources.getColorStateList(R.color.grey_text, this.context.theme)
                }
            }
        } else {
            holder.tvSoldOut?.visibility = View.VISIBLE
            holder.ivCart?.visibility = View.INVISIBLE
            holder.layoutGreyOut?.visibility = View.VISIBLE
        }
    }

    override fun getItemViewType(position: Int): Int {
        val spanCount = mLayoutManager.spanCount
        return if (spanCount == SPAN_COUNT_ONE) {
            Constants.LIST_VIEW
        } else {
            Constants.GRID_VIEW
        }
    }

    override fun getItemCount() = mListFavoriteProductData.size

}

