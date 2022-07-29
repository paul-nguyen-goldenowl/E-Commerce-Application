package com.goldenowl.ecommerce.ui.global.bag

import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.adapter.CheckoutDeliveryAdapter
import com.goldenowl.ecommerce.databinding.FragmentCheckoutBinding
import com.goldenowl.ecommerce.models.data.*
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment
import com.goldenowl.ecommerce.utils.BaseLoadingStatus
import com.goldenowl.ecommerce.utils.Constants
import java.util.*

class CheckoutFragment : BaseHomeFragment<FragmentCheckoutBinding>() {
    override fun getViewBinding(): FragmentCheckoutBinding {
        return FragmentCheckoutBinding.inflate(layoutInflater)
    }

    private lateinit var listProductData: List<ProductData>

    private var listCard: List<Card> = emptyList()
    private var listAddress: List<Address> = emptyList()

    private var defaultCardIndex: Int? = null
    private var defaultAddressIndex: Int? = null

    private var card: Card? = null
    private var address: Address? = null

    private var orderPrice = 0f
    private var deliveryPrice = 0
    private var summaryPrice = 0f

    private lateinit var loadingView: FrameLayout

    override fun setObservers() {

        viewModel.listCard.observe(viewLifecycleOwner) {
            listCard = it
            if (it.isNotEmpty()) {
                binding.btnAddPayment.visibility = View.GONE
                binding.layoutCard.visibility = View.VISIBLE
                if (it.size == 1)
                    viewModel.setDefaultCard(0)
            } else {
                binding.btnAddPayment.visibility = View.VISIBLE
                binding.layoutCard.visibility = View.GONE
                setCard()
            }
        }

        viewModel.listAddress.observe(viewLifecycleOwner) {
            listAddress = it
            if (it.isEmpty()) {
                binding.btnAddAddress.visibility = View.VISIBLE
                binding.cardViewAddress.visibility = View.GONE
            } else {
                if (it.size == 1)
                    viewModel.setDefaultAddress(0)
                binding.btnAddAddress.visibility = View.GONE
                binding.cardViewAddress.visibility = View.VISIBLE
                setAddress()
            }
        }

        viewModel.orderPrice.observe(viewLifecycleOwner) {
            binding.tvOrderPrice.text = getString(R.string.money_unit_float, it)
            orderPrice = it
            summaryPrice = orderPrice + deliveryPrice
            binding.tvSummaryPrice.text = getString(R.string.money_unit_float, summaryPrice)
        }

        viewModel.defaultAddressIndex.observe(viewLifecycleOwner) {
            defaultAddressIndex = it
            setAddress()
        }

        viewModel.defaultCardIndex.observe(viewLifecycleOwner) {
            defaultCardIndex = it
            setCard()
        }

        viewModel.deliveryMethod.observe(viewLifecycleOwner) {
            if (it != null) {
                deliveryPrice = it.price
                binding.tvDeliveryPrice.text = getString(R.string.money_unit_int, it.price)
                summaryPrice = orderPrice + deliveryPrice
                binding.tvSummaryPrice.text = getString(R.string.money_unit_float, summaryPrice)
            }
        }

        viewModel.loadingStatus.observe(viewLifecycleOwner) {
            handleResult(it)
        }
    }

    private fun setCard() {
        card = defaultCardIndex?.let { listCard.getOrNull(it) }
        if (card != null) {
            binding.tvCardNumber.text = Card.getHiddenNumber(card!!.cardNumber)
            if (card!!.cardNumber[0] == '4')
                binding.ivCardImg.setImageResource(R.drawable.ic_master_card_2)
            else if (card!!.cardNumber[0] == '5')
                binding.ivCardImg.setImageResource(R.drawable.ic_visa)
        }
    }

    private fun setAddress() {
        address = defaultAddressIndex?.let { listAddress.getOrNull(it) }
        if (address != null) {
            binding.tvFullName.text = address?.fullName
            binding.tvAddress.text = address?.getAddressString()
        }
    }

    private fun handleResult(status: BaseLoadingStatus?) {
        when (status) {
            BaseLoadingStatus.LOADING -> loadingView.visibility = View.VISIBLE
            BaseLoadingStatus.SUCCEEDED -> {
                loadingView.visibility = View.INVISIBLE
                findNavController().navigate(R.id.success_dest)
            }
            else -> loadingView.visibility = View.INVISIBLE

        }
    }


    override fun init() {
        super.init()
        listProductData = viewModel.listProductData.value ?: emptyList()
    }

    override fun setViews() {
        loadingView = binding.layoutLoading.loadingFrameLayout
        val adapter = CheckoutDeliveryAdapter(object : CheckoutDeliveryAdapter.IClickDelivery {
            override fun onClickDelivery(delivery: Delivery) {
                viewModel.deliveryMethod.value = delivery
            }
        })
        binding.rcvDelivery.adapter = adapter
        binding.btnAddPayment.setOnClickListener {
            findNavController().navigate(R.id.payment_dest)
        }
        binding.tvChangePayment.setOnClickListener {
            findNavController().navigate(R.id.payment_dest)
        }
        binding.btnAddAddress.setOnClickListener {
            findNavController().navigate(R.id.add_address_dest)
        }
        binding.tvChangeAddress.setOnClickListener {
            findNavController().navigate(R.id.address_dest)
        }
        binding.btnSubmitOrder.setOnClickListener {
            if (deliveryPrice == 0) {
                showToast(getString(R.string.please_select_delivery))
                binding.scrollView.smoothScrollTo(0, binding.tvDelivery.top, 500)
                return@setOnClickListener
            }
            val listCart = viewModel.allCart.value
            if (listCart != null) {

                val order = Order(
                    orderId = Order.generateOrderId(),
                    trackingNumber = Order.generateTrackingNumber(),
                    date = Date(),
                    listCart = listCart,
                    promoCode = viewModel.curBag.value?.promo?.id ?: "",
                    cardId = card?.cardNumber ?: "",
                    totalAmount = summaryPrice,
                    delivery = viewModel.deliveryMethod.value?.id ?: Constants.listDelivery[0].id,
                    shippingAddress = address?.getShippingAddress() ?: ""
                )
                viewModel.insertOrder(order)
                viewModel.emptyCartTable()
            }
        }
    }


    companion object {
        const val TAG = "CheckoutFragment"
    }

    override fun setAppbar() {
        binding.topAppBar.collapsingToolbarLayout.title = getString(R.string.checkout)
        binding.topAppBar.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
}
