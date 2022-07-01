package com.goldenowl.ecommerce.models.repo

import android.util.Log
import com.goldenowl.ecommerce.models.data.*
import com.goldenowl.ecommerce.utils.MyResult
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.supervisorScope


class ProductsRepository(
    private val remoteProductDataSource: RemoteProductsDataSource,
    private val localProductDataSource: LocalProductsDataSource
) {
    private var networkAvailable: Boolean = false
    val allFavorite = localProductDataSource.allFavorite
    val allCart = localProductDataSource.allCart
    val allAddress = localProductDataSource.allAddress


    suspend fun getAllProducts(): List<Product> {
        return if (networkAvailable) {
            val products = remoteProductDataSource.getAllProducts()
            localProductDataSource.insertMultipleProduct(products)
            products
        } else {
            localProductDataSource.getAllProducts()
        }
    }

    suspend fun insertFavorite(favorite: Favorite): MyResult<Boolean> {
        if (!networkAvailable)
            return MyResult.Error(java.lang.Exception("Network unavailable!"))
        // let 2 child coroutine run async in father  supervisorScope/coroutineScope
        return supervisorScope {
            try {
                val remoteSource = async { remoteProductDataSource.insertFavorite(favorite) }
                val localSource = async { localProductDataSource.insertFavorite(favorite) }
                remoteSource.await()
                localSource.await()
                MyResult.Success(true)
            } catch (e: Exception) {
                Log.e(TAG, "insertFavorite: ERROR", e)
                MyResult.Error(e)
            }
        }

    }

    suspend fun removeFavorite(favorite: Favorite): MyResult<Boolean> {
        if (!networkAvailable)
            return MyResult.Error(java.lang.Exception("Network unavailable!"))

        return supervisorScope {
            try {
                val remoteSource = async { remoteProductDataSource.removeFavorite(favorite) }
                val localSource = async { localProductDataSource.removeFavorite(favorite) }
                remoteSource.await()
                localSource.await()
                MyResult.Success(true)
            } catch (e: Exception) {
                MyResult.Error(e)
            }
        }
    }

    suspend fun insertCart(cart: Cart): MyResult<Boolean> {
        return supervisorScope {
            try {
                val remoteSource = async { remoteProductDataSource.insertCart(cart) }
                val localSource = async { localProductDataSource.insertCart(cart) }
                remoteSource.await()
                localSource.await()
                MyResult.Success(true)
            } catch (e: Exception) {
                MyResult.Error(e)
            }
        }
    }

    suspend fun updateCart(cart: Cart, position: Int): MyResult<Boolean> {
        return supervisorScope {
            try {
                val remoteSource = async { remoteProductDataSource.updateCart(cart, position) }
                val localSource = async { localProductDataSource.updateCart(cart) }
                remoteSource.await()
                localSource.await()
                MyResult.Success(true)
            } catch (e: Exception) {
                Log.e(TAG, "insertFavorite: ERROR", e)
                MyResult.Error(e)
            }
        }
    }

    suspend fun removeCart(cart: Cart): MyResult<Boolean> {
        return supervisorScope {
            val remoteSource = async { remoteProductDataSource.removeCart(cart) }
            val localSource = async { localProductDataSource.removeCart(cart) }
            try {
                remoteSource.await()
                localSource.await()
                MyResult.Success(true)
            } catch (e: Exception) {
                MyResult.Error(e)
            }
        }
    }

    suspend fun emptyCartTable(): MyResult<Boolean> {
        return supervisorScope {
            val remoteSource = async { remoteProductDataSource.emptyCartTable() }
            val localSource = async { localProductDataSource.emptyCartTable() }
            try {
                remoteSource.await()
                localSource.await()
                MyResult.Success(true)
            } catch (e: Exception) {
                MyResult.Error(e)
            }
        }
    }

    suspend fun insertOrder(order: Order): MyResult<Boolean> {
        return supervisorScope {
            try {
                val remoteSource = async { remoteProductDataSource.insertOrder(order) }
                val localSource = async { localProductDataSource.insertOrder(order) }
                remoteSource.await()
                localSource.await()
                MyResult.Success(true)
            } catch (e: Exception) {
                MyResult.Error(e)
            }
        }
    }

    suspend fun removeOrder(order: Order): MyResult<Boolean> {
        return supervisorScope {
            val remoteSource = async { remoteProductDataSource.removeOrder(order) }
            val localSource = async { localProductDataSource.removeOrder(order) }
            try {
                remoteSource.await()
                localSource.await()
                MyResult.Success(true)
            } catch (e: Exception) {
                MyResult.Error(e)
            }
        }
    }

    suspend fun getListPromo(): MyResult<List<Promo>> {
        return remoteProductDataSource.getListPromo()
    }

    suspend fun getListCard(): MyResult<List<Card>> {
        return remoteProductDataSource.getListCard()
    }

    suspend fun getListAddress(): MyResult<List<Address>> {
        return try {
            val data = remoteProductDataSource.getListAddress()
            MyResult.Success(data)
        } catch (e: Exception) {
            MyResult.Error(e)
        }
    }

    suspend fun insertCard(card: Card): MyResult<Boolean> {
        return try {
            remoteProductDataSource.insertCard(card)
            MyResult.Success(true)
        } catch (e: Exception) {
            MyResult.Error(e)
        }
    }

    suspend fun setDefaultCheckOut(default: Map<String, Int?>): MyResult<Boolean> {
        return try {
            remoteProductDataSource.setDefaultCheckOut(default)
            MyResult.Success(true)
        } catch (e: Exception) {
            MyResult.Error(e)
        }
    }

    suspend fun removeCard(position: Int): MyResult<Boolean> {
        return try {
            remoteProductDataSource.removeCard(position)
            MyResult.Success(true)
        } catch (e: Exception) {
            MyResult.Error(e)
        }
    }

    suspend fun getDefaultCheckOut(): MyResult<Map<String, Int>> {
        return try {
            val data = remoteProductDataSource.getDefaultCheckOut()
            MyResult.Success(data)
        } catch (e: Exception) {
            MyResult.Error(e)
        }
    }

    suspend fun insertAddress(address: Address): MyResult<Boolean> {
        return supervisorScope {
            try {
                val remote = async { remoteProductDataSource.insertAddress(address) }
                val local = async { localProductDataSource.insertAddress(address) }
                remote.await()
                local.await()
                MyResult.Success(true)
            } catch (e: Exception) {
                MyResult.Error(e)
            }
        }
    }

    suspend fun removeAddress(position: Int, address: Address): MyResult<Boolean> {
        return supervisorScope {
            try {
                val remote = async { remoteProductDataSource.removeAddress(position) }
                val local = async { localProductDataSource.removeAddress(address) }
                remote.await()
                local.await()
                MyResult.Success(true)
            } catch (e: Exception) {
                MyResult.Error(e)
            }
        }
    }

    suspend fun updateAddress(address: Address, position: Int): MyResult<Boolean> {
        return supervisorScope {
            try {
                val remoteSource = async { remoteProductDataSource.updateAddress(address, position) }
                val localSource = async { localProductDataSource.updateAddress(address) }
                remoteSource.await()
                localSource.await()
                MyResult.Success(true)
            } catch (e: Exception) {
                Log.e(TAG, "insertFavorite: ERROR", e)
                MyResult.Error(e)
            }
        }
    }

    fun setNetworkAvailable(networkAvailable: Boolean) {
        this.networkAvailable = networkAvailable
    }

    suspend fun local2remote() {
        supervisorScope {
            val localCarts = localProductDataSource.allCart.first()
            val localFavorites = localProductDataSource.allFavorite.first()
            val localAddress = localProductDataSource.allAddress.first()

            //todo local 2 remote
//            remoteProductDataSource.insertMutipleFavorite(localFavorites)
//            remoteProductDataSource.insertMutipleFavorite(localCarts)
//            remoteProductDataSource.insertMutipleFavorite(localCarts)
        }
    }

    companion object {
        val TAG = "ProductsRepository"
    }

}
