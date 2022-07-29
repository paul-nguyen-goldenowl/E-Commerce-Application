package com.goldenowl.ecommerce.models.data

import com.goldenowl.ecommerce.models.repo.ProductDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

class LocalProductsDataSource(
    private val productDao: ProductDao,
    private val favoriteDao: FavoriteDao,
    private val cartDao: CartDao,
    private val orderDao: OrderDao,
    private val addressDao: AddressDao,
) : ProductDataSource {

    private val dispatchers = Dispatchers.IO

    val allFavorite = favoriteDao.getListFavorite()
    val allCart = cartDao.getListCart()
    val allAddress = addressDao.getListAddress()
    val allOrder = orderDao.getListOrder()

    override suspend fun getAllProducts(): List<Product> {
        return productDao.getListProduct()
    }

    suspend fun updateProduct(product: Product) {
        return productDao.updateProduct(product)
    }

    override suspend fun insertFavorite(favorite: Favorite) {
        favoriteDao.insertFavorite(favorite)
    }

    override suspend fun removeFavorite(favorite: Favorite) {
        favoriteDao.removeFavorite(favorite)
    }

    suspend fun insertMultipleProduct(productsList: List<Product>) {
        productDao.deleteTable()
        productDao.insertMultipleProduct(productsList)
    }

    suspend fun insertCart(cart: Cart) {
        cartDao.insertCart(cart)
    }

    suspend fun updateCart(cart: Cart) {
        cartDao.updateCart(cart)
    }

    suspend fun removeCart(cart: Cart) {
        cartDao.removeCart(cart)
    }

    suspend fun emptyCartTable() {
        cartDao.deleteTable()
    }

    suspend fun insertOrder(order: Order) {
        orderDao.insertOrder(order)
    }

    suspend fun removeOrder(order: Order) {
        orderDao.removeOrder(order)
    }

    suspend fun insertAddress(address: Address) {
        addressDao.insertAddress(address)
    }

    suspend fun updateAddress(address: Address) {
        addressDao.updateAddress(address)
    }

    suspend fun removeAddress(address: Address) {
        addressDao.removeAddress(address)
    }

    suspend fun getProductById(productId: String): Product? {
        return productDao.getProductById(productId)
    }


    suspend fun insertMultipleCart(listCart: List<Cart>) {
        cartDao.insertMultipleCart(listCart)
    }

    suspend fun insertMultipleOrder(listOrder: List<Order>) {
        orderDao.insertMultipleOrder(listOrder)
    }

    suspend fun insertMultipleFavorite(allFavorite: List<Favorite>) {
        favoriteDao.insertMultipleFavorite(allFavorite)
    }

    suspend fun clearLocalDataSource() {
        supervisorScope {
            listOf(
                async { favoriteDao.deleteTable() },
                async { cartDao.deleteTable() },
                async { orderDao.deleteTable() },
            ).awaitAll()
        }
    }
}
