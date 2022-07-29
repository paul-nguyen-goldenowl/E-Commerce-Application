package com.goldenowl.ecommerce.ui.global.home

import android.view.View
import androidx.navigation.fragment.findNavController
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentRatingReviewBinding
import com.goldenowl.ecommerce.models.data.ProductData
import com.goldenowl.ecommerce.models.data.ReviewData
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment
import com.goldenowl.ecommerce.ui.global.bottomsheet.BottomSheetWriteReview
import com.goldenowl.ecommerce.utils.BaseLoadingStatus
import com.goldenowl.ecommerce.utils.Constants
import com.goldenowl.ecommerce.viewmodels.ReviewAdapter


class RatingReviewFragment : BaseHomeFragment<FragmentRatingReviewBinding>() {

    private lateinit var productData: ProductData
    private lateinit var adapter: ReviewAdapter
    private var mListReviewData: List<ReviewData> = emptyList()

    override fun init() {
        productData = arguments?.get(Constants.KEY_PRODUCT) as ProductData
        viewModel.loadingStatus.value = BaseLoadingStatus.NONE
        viewModel.getReviewByProductId(productData.product.id)
    }


    override fun setObservers() {
        viewModel.listReviewData.observe(viewLifecycleOwner) {
            mListReviewData = it
            if (it.isEmpty()) {
                binding.tvNoReviewYet.visibility = View.VISIBLE
                binding.layoutReviewRating.visibility = View.INVISIBLE
            } else {
                binding.tvNoReviewYet.visibility = View.INVISIBLE
                binding.layoutReviewRating.visibility = View.VISIBLE
                refreshData()
            }
        }
        viewModel.loadingStatus.observe(viewLifecycleOwner) {
            when (it) {
                BaseLoadingStatus.LOADING -> {
                    binding.layoutLoading.loadingFrameLayout.visibility = View.VISIBLE
                    binding.layoutRating.visibility = View.INVISIBLE
                }
                else -> {
                    binding.layoutLoading.loadingFrameLayout.visibility = View.INVISIBLE
                    binding.layoutRating.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun setViews() {
        adapter = ReviewAdapter(object : ReviewAdapter.IClickListener {
            override fun onHelpfulClick(reviewData: ReviewData) {
                viewModel.updateHelpful(reviewData)
            }

        })
        binding.rcvReviews.adapter = adapter
        binding.layoutWriteReview.setOnClickListener {
            if (viewModel.isLoggedIn())
                toggleBottomSheetWriteReview()
            else
                toggleDialogLogIn()
        }
        binding.checkWithPhoto.setOnClickListener {
            it.isSelected = !it.isSelected
            refreshData()
        }
    }

    private fun refreshData() {
        var list = mListReviewData
        if (binding.checkWithPhoto.isSelected) {
            list = mListReviewData.filter { it.review.images.isNotEmpty() }
        }
        adapter.setData(list)
        binding.tvNumberReviews.text =
            resources.getQuantityString(R.plurals.num_review, list.size, list.size)
        binding.tvNumRating.text =
            resources.getQuantityString(R.plurals.num_rating, mListReviewData.size, mListReviewData.size)

        val map = ReviewData.getStat(mListReviewData)
        val max = map.values.toList().maxOrNull()!!

        binding.progress1.max = max
        binding.progress1.progress = map[1]!!
        binding.tvNum1.text = map[1]!!.toString()

        binding.progress2.max = max
        binding.progress2.progress = map[2]!!
        binding.tvNum2.text = map[2]!!.toString()

        binding.progress3.max = max
        binding.progress3.progress = map[3]!!
        binding.tvNum3.text = map[3]!!.toString()

        binding.progress4.max = max
        binding.progress4.progress = map[4]!!
        binding.tvNum4.text = map[4]!!.toString()

        binding.progress5.max = max
        binding.progress5.progress = map[5]!!
        binding.tvNum5.text = map[5]!!.toString()

        if (mListReviewData.isNotEmpty()) {
            val averageRating = ReviewData.getAverageRating(mListReviewData)
            binding.tvAverageRating.text = String.format("%.1f", averageRating)
        } else {
            binding.tvAverageRating.visibility = View.GONE
        }
    }

    private fun toggleBottomSheetWriteReview() {
        val modalBottomSheet = BottomSheetWriteReview(productData)
        modalBottomSheet.enterTransition = View.GONE
        modalBottomSheet.show(parentFragmentManager, BaseHomeFragment.TAG)
    }

    override fun setAppbar() {
        binding.topAppBar.toolbar.apply {
            setNavigationOnClickListener {
                findNavController().navigateUp()
                viewModel.loadingStatus.value = BaseLoadingStatus.NONE
            }
            title = getString(R.string.rating_review)
        }
    }


    companion object {
        const val TAG = "RatingFragment"
    }

    override fun getViewBinding(): FragmentRatingReviewBinding {
        return FragmentRatingReviewBinding.inflate(layoutInflater)
    }

}


