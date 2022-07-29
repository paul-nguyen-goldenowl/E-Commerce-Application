package com.goldenowl.ecommerce.ui.global.profile

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentProfileBinding
import com.goldenowl.ecommerce.models.auth.UserManager
import com.goldenowl.ecommerce.models.data.Card
import com.goldenowl.ecommerce.ui.auth.LoginSignupActivity
import com.goldenowl.ecommerce.ui.global.BaseFragment
import com.goldenowl.ecommerce.ui.global.MainActivity
import com.goldenowl.ecommerce.viewmodels.AuthViewModel
import com.goldenowl.ecommerce.viewmodels.ShopViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class ProfileFragment : BaseFragment<FragmentProfileBinding>() {
    private val TAG = "ProfileFragment"

    private lateinit var auth: FirebaseAuth
    private lateinit var userManager: UserManager
    private val viewModel: AuthViewModel by activityViewModels()
    private val shopViewModel: ShopViewModel by activityViewModels()

    override fun init() {
        userManager = UserManager.getInstance(requireContext())
        auth = Firebase.auth
    }

    override fun getViewBinding(): FragmentProfileBinding {
        return FragmentProfileBinding.inflate(layoutInflater)
    }

    override fun setViews() {
        if (userManager.isLoggedIn()) {
            setUpUserUI()
        } else {
            setUpGuessUI()
        }
    }

    override fun setObservers() {
        if (!userManager.isLoggedIn())
            return

        shopViewModel.mListReview.observe(viewLifecycleOwner) {
            binding.tvActionReview.text = getQuantityString(R.plurals.num_review, it.size)
        }
        shopViewModel.listAddress.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                binding.tvActionAddress.text = getString(R.string.no_address)
                return@observe
            }
            shopViewModel.defaultAddressIndex.value?.let {
                if (it >= 0) {
                    val address = list[it]
                    binding.tvActionAddress.text = address?.getShippingAddress()
                }
            }
        }
        shopViewModel.allOrder.observe(viewLifecycleOwner) { list ->
            binding.tvActionOrders.text = if (list.isEmpty()) getString(R.string.no_orders)
            else getQuantityString(R.plurals.num_order, list.size)
        }
        shopViewModel.listPromo.observe(viewLifecycleOwner) { list ->

            binding.tvActionPromocode.text =
                if (list.isEmpty()) getString(R.string.no_promo) else getQuantityString(R.plurals.num_promo, list.size)
        }

        shopViewModel.listCard.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                binding.tvActionPayment.text = getString(R.string.no_card)
                return@observe
            }
            shopViewModel.defaultCardIndex.value?.let {
                if (it >= 0) {
                    val card = list[it]
                    binding.tvActionPayment.text =
                        if (card != null) Card.getProfileCard(card.cardNumber) else getString(R.string.no_card)
                }
            }
        }
    }

    private fun setUpUserUI() {
        with(binding) {
            val userName = userManager.name
            tvName.text = userName
            val userEmail = userManager.email
            tvEmail.text = userEmail
            userManager.avatar.let {
                val uri = Uri.parse(it)
                if (!it.isNullOrBlank()) {
                    Glide.with(this@ProfileFragment)
                        .load(uri)
                        .placeholder(R.drawable.ic_user)
                        .apply(options).into(userIcon)
                }
            }
            layoutUserActions.visibility = View.VISIBLE
            layoutGuessActions.visibility = View.GONE

            btnLogOut.setOnClickListener {
                val loginIntent = Intent(activity, MainActivity::class.java)
                logOut()
                context?.startActivity(loginIntent)
                activity?.finish()
            }

            actionOrders.setOnClickListener {
                findNavController().navigate(R.id.my_order_dest)
            }

            actionSettings.setOnClickListener(
                Navigation.createNavigateOnClickListener(com.goldenowl.ecommerce.R.id.next_action, null)
            )

            actionAddress.setOnClickListener {
                findNavController().navigate(R.id.address_dest)
            }

            actionPayment.setOnClickListener {
                findNavController().navigate(R.id.payment_dest)
            }

            actionPromocode.setOnClickListener {
                findNavController().navigate(R.id.my_promo_dest)
            }

            actionReview.setOnClickListener {
                findNavController().navigate(R.id.my_review_dest)
            }
        }
    }

    private fun setUpGuessUI() {
        with(binding) {
            setSpannableString(tvEmail)

            tvEmail.movementMethod = LinkMovementMethod.getInstance()
            tvEmail.highlightColor = Color.RED

            layoutUserActions.visibility = View.GONE
            layoutGuessActions.visibility = View.VISIBLE
        }
    }

    private fun setSpannableString(tvEmail: TextView) {
        val ss = SpannableString(getString(com.goldenowl.ecommerce.R.string.guest_email))
        val clickableSpan: ClickableSpan = object : ClickableSpan() {

            override fun onClick(textView: View) {

                startActivity(Intent(requireActivity(), LoginSignupActivity::class.java))
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = Color.RED
                ds.isUnderlineText = false
            }
        }
        ss.setSpan(clickableSpan, 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val boldSpan = StyleSpan(Typeface.BOLD)
        ss.setSpan(boldSpan, 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        tvEmail.text = ss
    }


    private fun logOut() {
        viewModel.logOut()
    }

    override fun setAppbar() {
        binding.topAppBar.collapsingToolbarLayout.title = getString(com.goldenowl.ecommerce.R.string.profile)
        binding.topAppBar.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    companion object {
        var options: RequestOptions = RequestOptions()
            .centerCrop()
            .error(com.goldenowl.ecommerce.R.drawable.img_broken)

    }
}