package com.goldenowl.ecommerce.adapter

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
import com.goldenowl.ecommerce.utils.Utils.glide2View
import com.goldenowl.ecommerce.utils.Utils.strike

class CategoryProductListAdapter(
    private val mLayoutManager: GridLayoutManager,
    private val listener: IClickListener
) :
    RecyclerView.Adapter<CategoryProductListAdapter.CategoryProductViewHolder>() {

    private var mListProductData = listOf<ProductData>()

    fun setData(listProductData: List<ProductData>) {
        mListProductData = listProductData
        notifyDataSetChanged()
    }

    class CategoryProductViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var productName: TextView? = null
        var productBrand: TextView? = null
        var tvColor: TextView? = null
        var tvDiscountPercent: TextView? = null
        var tvSize: TextView? = null
        var originPrice: TextView? = null
        var discountPrice: TextView? = null
        var tvNumberReviews: TextView? = null
        var productImg: ImageView? = null
        var productRatingBar: RatingBar? = null
        var ivFavorite: ImageView? = null
        var layoutLoading: FrameLayout? = null
        var layoutFrameLoading: FrameLayout? = null

        init {
            productName = itemView.findViewById(R.id.product_name)
            productBrand = itemView.findViewById(R.id.product_brand)
            productImg = itemView.findViewById(R.id.product_img)
            productRatingBar = itemView.findViewById(R.id.product_rating_bar)
            tvNumberReviews = itemView.findViewById(R.id.tv_number_reviews)
            ivFavorite = itemView.findViewById(R.id.iv_favorite)
            layoutLoading = itemView.findViewById(R.id.layout_loading)
            if (layoutLoading != null) {
                layoutFrameLoading = layoutLoading!!.findViewById(R.id.loading_frame_layout) ?: null
            }
            tvColor = itemView.findViewById(R.id.tv_color)
            tvSize = itemView.findViewById(R.id.tv_size)
            originPrice = itemView.findViewById(R.id.product_origin_price)
            discountPrice = itemView.findViewById(R.id.product_discount_price)
            tvDiscountPercent = itemView.findViewById(R.id.tv_discount_percent)
        }
    }


    companion object {
        val TAG = "CategoryAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryProductViewHolder {
        if (viewType == Constants.GRID_VIEW) {
            return CategoryProductViewHolder(
                (LayoutInflater.from(parent.context)).inflate(R.layout.item_product_list_category_grid, parent, false)
            )
        }
        return CategoryProductViewHolder(
            (LayoutInflater.from(parent.context)).inflate(R.layout.item_product_list_category, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CategoryProductViewHolder, position: Int) {
        val product = mListProductData[position].product
        val favorite = mListProductData[position].favorite


        holder.ivFavorite?.setOnClickListener {
            listener.onClickFavorite(product, favorite)
        }

        holder.itemView.setOnClickListener {

        }

        holder.itemView?.setOnClickListener {
            listener.onClickItem(mListProductData[position])
        }

        holder.productBrand?.text = product.brandName
        holder.productName?.text = product.title

        glide2View(holder.productImg!!, holder.layoutLoading!!, product.getImage()!!)

        holder.tvNumberReviews?.text = "(${product.numberReviews})"

        holder.tvSize?.text = favorite?.size
        holder.tvColor?.text = favorite?.color
        holder.productRatingBar?.rating = product.reviewStars.toFloat()

        product.salePercent.let {
            if (it != null) {
                holder.tvDiscountPercent?.apply {
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
                holder.tvDiscountPercent?.visibility = View.INVISIBLE
                holder.discountPrice?.visibility = View.INVISIBLE
                holder.originPrice?.text =
                    holder.itemView.context.resources.getString(R.string.money_unit_int, product.getOriginPrice())
                holder.originPrice?.paintFlags = Paint.ANTI_ALIAS_FLAG
            }
        }

        if (favorite != null)
            holder.ivFavorite?.setImageResource(R.drawable.ic_favorites_selected)
        else
            holder.ivFavorite?.setImageResource(R.drawable.ic_favorites_bold)
    }

    override fun getItemViewType(position: Int): Int {
        val spanCount = mLayoutManager.spanCount
        return if (spanCount == Constants.SPAN_COUNT_ONE) {
            Constants.LIST_VIEW
        } else {
            Constants.GRID_VIEW
        }
    }

    override fun getItemCount() = mListProductData.size
}



