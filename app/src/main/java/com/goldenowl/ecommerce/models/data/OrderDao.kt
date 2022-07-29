package com.goldenowl.ecommerce.models.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("select  * from order_table")
    fun getListOrder(): Flow<List<Order>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order)

    @Delete
    suspend fun removeOrder(order: Order): Int

    @Update
    suspend fun updateOrder(order: Order)

    @Query("Delete from order_table")
    suspend fun deleteTable()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultipleOrder(listOrder: List<Order>)
}