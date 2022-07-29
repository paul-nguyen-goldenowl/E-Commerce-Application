package com.goldenowl.ecommerce.models.data

import android.util.Log
import com.goldenowl.ecommerce.models.repo.ProductDataSource
import com.goldenowl.ecommerce.models.repo.RemoteAuthDataSource
import com.goldenowl.ecommerce.utils.Constants
import com.goldenowl.ecommerce.utils.Constants.APP_DATA_COLLECTION
import com.goldenowl.ecommerce.utils.Constants.PRODUCTS_COLLECTION
import com.goldenowl.ecommerce.utils.Constants.PROMOTIONS_COLLECTION
import com.goldenowl.ecommerce.utils.Constants.REVIEW_COLLECTION
import com.goldenowl.ecommerce.utils.Constants.USER_ORDER_COLLECTION
import com.goldenowl.ecommerce.utils.MyResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.util.*
import kotlin.coroutines.CoroutineContext


class RemoteProductsDataSource : ProductDataSource {
    private var lastVisible: DocumentSnapshot? = null
    private val dispatcherIO: CoroutineContext = Dispatchers.IO
    private val db: FirebaseFirestore = Firebase.firestore
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userOrderRef = db.collection(USER_ORDER_COLLECTION)
    private val reviewRef = db.collection(REVIEW_COLLECTION)
    private val appDataRef = db.collection(APP_DATA_COLLECTION)
    private val storageRef = Firebase.storage.reference

    var source = Source.DEFAULT

    override suspend fun getAllProducts(): List<Product> {
        val listProducts = mutableListOf<Product>()
        val documents = db.collection(PRODUCTS_COLLECTION).get(source).await()
        if (documents != null) {
            for (d in documents) {
                val product = d.toObject<Product>()
                listProducts.add(product)
            }
        }
        return listProducts
    }

    suspend fun updateProduct(product: Product) {
        db.collection(PRODUCTS_COLLECTION).document(product.id)
            .set(product)
            .await()
    }

    override suspend fun insertFavorite(favorite: Favorite) {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw(java.lang.Exception("[Firestore] User not found!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, listOf(favorite)))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)
            if (userOrder == null) {
                Log.w(TAG, "insertFavorite: user cart NULL")
            }
            var listFavorite: MutableList<Favorite> =
                userOrder?.favorites?.toMutableList() ?: mutableListOf()

            listFavorite.add(favorite)

            userOrderRef
                .update("favorites", listFavorite)
        }
    }

    override suspend fun removeFavorite(favorite: Favorite) {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw (Exception("[Firestore] User not logged in!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, listOf(favorite)))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)
            if (userOrder == null) {
                Log.w(TAG, "insertFavorite: user cart NULL")
            }
            var listFavorite: MutableList<Favorite> =
                userOrder?.favorites?.toMutableList() ?: mutableListOf()

            val remove = listFavorite.find { it.productId == favorite.productId && it.size == favorite.size }
            listFavorite.remove(remove)
            userOrderRef.update("favorites", listFavorite).await()
        }

    }

    suspend fun emptyProductTable() {
        val documents = db.collection(PRODUCTS_COLLECTION).get(source).await()
        for (d in documents) {
            d.reference.delete()
        }

    }


    suspend fun getFavoriteList(userId: String): List<Favorite> {
        return withContext(dispatcherIO) {
            val userOrder = db.collection(USER_ORDER_COLLECTION).document(userId).get(source).await()
            if (userOrder.exists()) {
                val orderObj = userOrder.toObject(UserOrder::class.java)
                return@withContext orderObj?.favorites ?: emptyList()
            } else
                return@withContext emptyList()
        }
    }

    suspend fun getUserOrder(userId: String): MyResult<UserOrder> {
        Log.d(RemoteAuthDataSource.TAG, "restoreDatabase restoring")
        return withContext(dispatcherIO) {
            try {
                val userDataSnapshot = userOrderRef.document(userId).get(source).await()
                if (!userDataSnapshot.exists()) {
                    Log.d(RemoteAuthDataSource.TAG, "restoreDatabase: userorder ${userId} not exist")
                    return@withContext MyResult.Error(java.lang.Exception("UserOrder ${userId}  not exist"))
                } else {
                    val userOrder = userDataSnapshot.toObject(UserOrder::class.java)
                        ?: return@withContext MyResult.Error(java.lang.Exception("UserOrder ${userId}  NULL"))

                    Log.d(RemoteAuthDataSource.TAG, "restoreDatabase: $userOrder")
                    return@withContext MyResult.Success(userOrder)
                }
            } catch (e: Exception) {
                Log.e(RemoteAuthDataSource.TAG, "restoreDatabase: ERROR", e)
                return@withContext MyResult.Error(e)
            }
        }
    }


    suspend fun insertCart(cart: Cart) {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw(java.lang.Exception("[Firestore] User not found!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, favorites = emptyList(), carts = listOf(cart)))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)
            if (userOrder == null) {
                Log.w(TAG, "insertCart: user cart NULL")
            }
            var listCart: MutableList<Cart> =
                userOrder?.carts?.toMutableList() ?: mutableListOf()

            listCart.add(cart)

            userOrderRef
                .update("carts", listCart)

        }
    }

    suspend fun updateCart(cart: Cart, position: Int) {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw(java.lang.Exception("[Firestore] User not found!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, favorites = emptyList(), carts = listOf(cart)))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)
            if (userOrder == null) {
                Log.w(TAG, "insertCart: user cart NULL")
            }
            var listCart: MutableList<Cart> =
                userOrder?.carts?.toMutableList() ?: mutableListOf()

            listCart.removeAt(position)
            listCart.add(position, cart)

            userOrderRef
                .update("carts", listCart)

        }
    }


    suspend fun removeCart(cart: Cart) {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw (Exception("[Firestore] User not logged in!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, emptyList(), listOf(cart)))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)
            if (userOrder == null) {
                Log.w(TAG, "insertCart: user cart NULL")
            }
            var listCart: MutableList<Cart> =
                userOrder?.carts?.toMutableList() ?: mutableListOf()

            val remoteCart: Cart? = listCart.find {
                cart.isSame(it)
            }

            if (remoteCart != null)
                listCart.remove(remoteCart)
            userOrderRef
                .update("carts", listCart)
        }
    }

    suspend fun emptyCartTable() {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw (Exception("[Firestore] User not logged in!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        if (snapshot.exists()) {
            userOrderRef
                .update("carts", emptyList<Cart>())
        }
    }

    suspend fun insertOrder(order: Order) {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw(java.lang.Exception("[Firestore] User not found!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, orders = listOf(order)))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)
            var listOrder: MutableList<Order> =
                userOrder?.orders?.toMutableList() ?: mutableListOf()

            listOrder.add(order)

            userOrderRef
                .update("orders", listOrder)
        }
    }

    suspend fun removeOrder(order: Order) {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw (Exception("[Firestore] User not logged in!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, orders = listOf(order)))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)
            var listOrder: MutableList<Order> =
                userOrder?.orders?.toMutableList() ?: mutableListOf()

            val remoteOrder: Order? = listOrder.find {
                order.trackingNumber == it.trackingNumber
            }

            if (remoteOrder != null)
                listOrder.remove(remoteOrder)
            userOrderRef
                .update("orders", listOrder)
        }
    }

    suspend fun getListPromo(): MyResult<List<Promo>> {
        return try {
            val listPromos = mutableListOf<Promo>()
            val documents = db.collection(PROMOTIONS_COLLECTION).get(source).await()

            for (d in documents) {
                val promo = d.toObject<Promo>()
                listPromos.add(promo)
            }
            MyResult.Success(listPromos)
        } catch (e: Exception) {
            MyResult.Error(e)
        }
    }

    suspend fun getListCard(): MyResult<List<Card>> {
        return try {
            val userId =
                firebaseAuth.currentUser?.uid ?: throw(java.lang.Exception("[Firestore] User not found!"))
            var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
            val snapshot = userOrderRef.get(source).await()
            if (!snapshot.exists()) {
                return MyResult.Success(emptyList())
            } else {
                val userOrder = snapshot.toObject(UserOrder::class.java)
                var listCard: MutableList<Card> =
                    userOrder?.cards?.toMutableList() ?: mutableListOf()
                return MyResult.Success(listCard)
            }
        } catch (e: Exception) {
            MyResult.Error(e)
        }
    }

    suspend fun insertCard(card: Card) {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw(java.lang.Exception("[Firestore] User not found!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, favorites = emptyList(), cards = listOf(card)))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)
            var listCard: MutableList<Card> =
                userOrder?.cards?.toMutableList() ?: mutableListOf()

            listCard.add(card)


            userOrderRef
                .update("cards", listCard)
        }
    }

    suspend fun removeCard(position: Int) {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw (Exception("[Firestore] User not logged in!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, emptyList(), emptyList(), emptyList()))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)

            var listCard: MutableList<Card> =
                userOrder?.cards?.toMutableList() ?: mutableListOf()

            listCard.removeAt(position)

            userOrderRef
                .update("cards", listCard).await()
        }
    }

    suspend fun getListAddress(): List<Address> {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw(java.lang.Exception("[Firestore] User not found!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        if (!snapshot.exists()) {
            return emptyList()
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)
            var listAddress: MutableList<Address> =
                userOrder?.addresss?.toMutableList() ?: mutableListOf()
            return listAddress
        }
    }

    suspend fun getListReview(): List<Review> {
        firebaseAuth.currentUser?.uid ?: throw(java.lang.Exception("[Firestore] User not found!"))

        val listReviews = mutableListOf<Review>()
        val documents = db.collection(PRODUCTS_COLLECTION).get(source).await()
        if (documents != null) {
            for (d in documents) {
                val review = d.toObject<Review>()
                listReviews.add(review)
            }
        }
        return listReviews
    }

    suspend fun setDefaultCheckOut(default: Map<String, Int?>) {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw(java.lang.Exception("[Firestore] User not found!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        userOrderRef.update("defaultCheckout", default).await()
    }

    suspend fun getDefaultCheckOut(): Map<String, Int> {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw(java.lang.Exception("[Firestore] User not found!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        val userOrder = snapshot.toObject(UserOrder::class.java)
        return userOrder?.defaultCheckout ?: mapOf()

    }

    /* user address */
    suspend fun insertAddress(address: Address) {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw(java.lang.Exception("[Firestore] User not found!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, favorites = emptyList(), addresss = listOf(address)))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)

            var listAddress: MutableList<Address> =
                userOrder?.addresss?.toMutableList() ?: mutableListOf()

            listAddress.add(address)

            userOrderRef
                .update("addresss", listAddress)
        }
    }

    suspend fun removeAddress(position: Int) {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw (Exception("[Firestore] User not logged in!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, emptyList(), emptyList(), emptyList()))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)

            var listAddress: MutableList<Address> =
                userOrder?.addresss?.toMutableList() ?: mutableListOf()

            Log.d(TAG, "removeAddress: $position , ${listAddress[position]}")
            listAddress.removeAt(position)

            userOrderRef
                .update("addresss", listAddress).await()
        }
    }

    suspend fun updateAddress(address: Address, position: Int) {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw(java.lang.Exception("[Firestore] User not found!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, favorites = emptyList(), addresss = listOf(address)))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)
            if (userOrder == null) {
                Log.w(TAG, "insertAddress: user address NULL")
            }
            var listAddress: MutableList<Address> =
                userOrder?.addresss?.toMutableList() ?: mutableListOf()

            listAddress.removeAt(position)
            listAddress.add(position, address)

            userOrderRef
                .update("addresss", listAddress)
        }
    }

    suspend fun insertMultipleCart(listCart: List<Cart>) {
        listCart.forEach {
            insertCart(it)
        }
    }

    suspend fun sendReview(rating: Review): String {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw(java.lang.Exception("[Firestore] User not found!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        var reviewRef = db.collection(REVIEW_COLLECTION)

        /* review document */
        val addedReviewRef = reviewRef.add(rating).await()
        return addedReviewRef.id
    }

    suspend fun uploadReviewImage(file: String): String {
        if (firebaseAuth.currentUser == null) throw Exception("[Firebase] Please login first!")
        val stream = FileInputStream(File(file))
        val reviewRef = storageRef.child("images/reviews/${Date().time}")
        stream.let { reviewRef.putStream(it) }?.await()
        return reviewRef.downloadUrl.await().toString()
    }

    suspend fun updateHelpful(reviewId: String, helpful: Boolean) {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw(java.lang.Exception("[Firestore] User not found!"))

        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()

        val userOrder = snapshot.toObject(UserOrder::class.java)

        var setUsefulReview: MutableSet<String> = userOrder?.usefulReviews?.toMutableSet() ?: mutableSetOf()
        if (helpful)
            setUsefulReview.add(reviewId)
        else
            setUsefulReview.remove(reviewId)
        userOrderRef
            .update("usefulReviews", setUsefulReview.toList())
    }

    suspend fun getReviewByProductId(productId: String): List<ReviewData> {
        val listReviews = mutableListOf<ReviewData>()
        val documents = reviewRef
            .whereEqualTo("productId", productId)
            .orderBy("date", Query.Direction.DESCENDING)
            .get(source).await()
        if (documents != null) {
            for (d in documents) {
                val review = d.toObject<Review>()
                listReviews.add(
                    ReviewData(reviewId = d.id, review = review)
                )
            }
        }
        return listReviews
    }

    suspend fun getHelpfulReview(): List<String> {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw(java.lang.Exception("[Firestore] User not found!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()

        val userOrder = snapshot.toObject(UserOrder::class.java) ?: return emptyList()
        return userOrder.usefulReviews
    }

    var productRef: Query? = null
    suspend fun loadFirstPage(
        category: String?
    ): MutableList<Product> {
        val listProducts = mutableListOf<Product>()
        lastVisible = null
        Log.d(TAG, "loadFirstPage: category=$category")
        productRef =
            when (category) {
                Constants.KEY_SALE -> {
                    db.collection(PRODUCTS_COLLECTION).whereNotEqualTo("salePercent", null)
                        .orderBy("salePercent", Query.Direction.DESCENDING)
                }
                Constants.KEY_NEW -> {
                    db.collection(PRODUCTS_COLLECTION)
                        .orderBy("createdDate", Query.Direction.DESCENDING)
                }
                "" -> db.collection(PRODUCTS_COLLECTION).orderBy("id")
                null -> db.collection(PRODUCTS_COLLECTION).orderBy("id")
                else -> {
                    db.collection(PRODUCTS_COLLECTION).whereEqualTo("categoryName", category).orderBy("id")
                }
            }

        val querySnapshot = productRef!!
            .limit(Constants.LOAD_MORE_QUANTITY)
            .get(source)
            .await()

        if (querySnapshot.size() > 0)
            lastVisible = querySnapshot.documents[querySnapshot.size() - 1]

        if (querySnapshot != null) {
            for (d in querySnapshot) {
                val product = d.toObject<Product>()
                listProducts.add(product)
            }
        }
        return listProducts
    }

    suspend fun loadMorePage(
        category: String?
    ): MutableList<Product> {
        val listProducts = mutableListOf<Product>()

        if (productRef == null) return mutableListOf()

        var query = if (lastVisible == null)
            productRef!!
        else
            productRef!!.startAfter(lastVisible!!)
        val querySnapshot = query
            .limit(Constants.LOAD_MORE_QUANTITY)
            .get(source)
            .await()

        if (querySnapshot.size() > 0)
            lastVisible = querySnapshot.documents[querySnapshot.size() - 1]

        if (querySnapshot != null) {
            for (d in querySnapshot) {
                val product = d.toObject<Product>()
                listProducts.add(product)
            }
        }
        return listProducts
    }


    suspend fun getMyListReview(uid: String): MutableList<Review> {
        val listReviews = mutableListOf<Review>()
        val documents = reviewRef
            .whereEqualTo("userId", uid)
            .get(source).await()
        if (documents != null) {
            for (d in documents) {
                val review = d.toObject<Review>()
                listReviews.add(
                    review
                )
            }
        }
        return listReviews
    }

    suspend fun getAllFavorite(userId: String): List<Favorite> {
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        val userOrder = snapshot.toObject(UserOrder::class.java)
        return userOrder?.favorites ?: emptyList()
    }

    suspend fun getAllCart(userId: String): List<Cart> {
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        val userOrder = snapshot.toObject(UserOrder::class.java)
        return userOrder?.carts ?: emptyList()
    }

    suspend fun getAllOrder(userId: String): List<Order> {
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        val userOrder = snapshot.toObject(UserOrder::class.java)
        return userOrder?.orders ?: emptyList()
    }

    suspend fun getListAppbarImg(): List<Pair<String, String>> {
        Log.d(TAG, "getListAppbarImg: getting list appbar")
        val snapshot = appDataRef.document("homepage")
            .get(source)
            .await()
            .toObject<AppData>()
        val data = snapshot?.homepage ?: emptyList()
        val list = data.sortedBy { it.priority }
        return list.map {
            Pair(it.category, it.img)
        }
    }

    companion object {
        val TAG = "RemoteProductSource"
    }
}
