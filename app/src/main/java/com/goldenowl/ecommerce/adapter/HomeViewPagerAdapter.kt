package com.goldenowl.ecommerce.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldenowl.ecommerce.databinding.LayoutHomeViewPagerImageBinding
import com.goldenowl.ecommerce.utils.Utils

class HomeViewPagerAdapter(private val listener: (String) -> Unit) :
    RecyclerView.Adapter<HomeViewPagerAdapter.ViewHolder>() {
    private var listImg: List<Pair<String, String>> = emptyList()

    fun setData(imagesList: List<Pair<String, String>>) {
        listImg = imagesList
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: LayoutHomeViewPagerImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(url: String, title: String) {
            Utils.glide2View(binding.productImg, binding.layoutLoading.loadingFrameLayout, url)
            binding.tvTitle.text = title
            binding.root.setOnClickListener {
                listener(title)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutHomeViewPagerImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = listImg.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val url = listImg[position].second
        val title = listImg[position].first
        holder.bind(url, title)
    }

    companion object {
        val TAG = "ImageProductViewpager"
    }
}