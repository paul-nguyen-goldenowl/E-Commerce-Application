package com.goldenowl.ecommerce.ui.global

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.models.data.Cart
import com.goldenowl.ecommerce.models.data.Favorite
import com.goldenowl.ecommerce.models.data.Product
import com.goldenowl.ecommerce.models.data.ProductData
import com.goldenowl.ecommerce.ui.auth.LoginSignupActivity
import com.goldenowl.ecommerce.ui.global.bottomsheet.BottomSheetEnterPromo
import com.goldenowl.ecommerce.ui.global.bottomsheet.BottomSheetInsertCart
import com.goldenowl.ecommerce.ui.global.bottomsheet.BottomSheetInsertFavorite
import com.goldenowl.ecommerce.utils.BaseLoadingStatus
import com.goldenowl.ecommerce.utils.ConnectionLiveData
import com.goldenowl.ecommerce.utils.Utils
import com.goldenowl.ecommerce.viewmodels.ShopViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob


abstract class BaseHomeFragment<VBinding : ViewBinding> : BaseFragment<VBinding>(),
    IClickListener {

    protected val viewModel: ShopViewModel by activityViewModels()
    protected val uiScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    protected lateinit var connectionLiveData: ConnectionLiveData

    override fun onClickFavorite(product: Product, favorite: Favorite?) {
        if (!viewModel.isLoggedIn()) {
            toggleDialogLogIn()
            return
        }
        if (favorite == null) {
            toggleBottomSheetInsertFavorite(product)
        } else {
            viewModel.removeFavorite(favorite!!)
        }
    }

    override fun onClickItem(productData: ProductData) {
        findNavController().navigate(
            R.id.detail_dest,
            bundleOf("product_data" to productData)
        )
    }

    override fun onClickCart(product: Product, cart: Cart?) {
        if (!viewModel.isLoggedIn()) {
            toggleDialogLogIn()
            return
        }
        if (cart == null) {
            toggleBottomSheetInsertCart(product, null)
        } else {
            viewModel.removeCart(cart)
        }
    }

    override fun onClickRemoveFavorite(product: Product, favorite: Favorite?) {
        if (favorite != null) {
            viewModel.removeFavorite(favorite)
        }
    }

    override fun updateCartQuantity(cart: Cart, position: Int) {
        viewModel.updateCart(cart, position)
    }

    protected fun toggleBottomSheetInsertFavorite(product: Product) {
        val modalBottomSheet = BottomSheetInsertFavorite(product, viewModel)
        modalBottomSheet.enterTransition = View.GONE
        modalBottomSheet.show(parentFragmentManager, BottomSheetInsertCart.TAG)
    }

    /**
     * Open model bottom sheet to select size
     * @param cart if cart not null, mean user has chosen color and size in product detail screen
     */
    protected fun toggleBottomSheetInsertCart(product: Product, cart: Cart?) {
        val modalBottomSheet = BottomSheetInsertCart(product, cart, viewModel)
        modalBottomSheet.enterTransition = View.GONE
        modalBottomSheet.show(parentFragmentManager, TAG)
    }

    protected fun toggleBottomSheetEnterPromo() {
        val modalBottomSheet = BottomSheetEnterPromo(viewModel)
        modalBottomSheet.enterTransition = View.GONE
        modalBottomSheet.show(parentFragmentManager, TAG)
    }

    protected fun toggleDialogLogIn() {
        Snackbar.make(binding.root, R.string.log_in_to_continue, Snackbar.LENGTH_LONG)
            .setAction(R.string.login) {
                startActivity(Intent(requireContext(), LoginSignupActivity::class.java))
            }
            .show()
    }

    protected fun showToast(msg: String?) {
        if (msg.isNullOrBlank())
            return
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    companion object {
        val TAG = "BaseHomeFragment"
    }

    override fun init() {
        super.init()
        connectionLiveData = ConnectionLiveData(requireContext())
        viewModel.loadingStatus.value = BaseLoadingStatus.NONE
        viewModel.setNetWorkAvailable(Utils.isNetworkAvailable(requireContext()))
    }

    override fun setObservers() {
        connectionLiveData.observe(this) {
            viewModel.setNetWorkAvailable(it)
            Log.d(TAG, "NetWorkAvailable=$it")
        }
    }
}

interface IClickListener {
    fun onClickFavorite(product: Product, favorite: Favorite?)
    fun onClickItem(productData: ProductData)
    fun onClickCart(product: Product, cart: Cart?)
    fun onClickRemoveFavorite(product: Product, favorite: Favorite?)
    fun updateCartQuantity(cart: Cart, position: Int)
}
