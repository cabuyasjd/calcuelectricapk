package com.calcuelectric.app.network

import com.calcuelectric.app.Operation
import com.calcuelectric.app.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE

interface ApiService {
    @GET("auth.php")
    suspend fun getSession(): Response<SessionResponse>

    @POST("auth.php")
    suspend fun login(@Body body: Map<String, String>): Response<AuthResponse>

    @GET("users.php")
    suspend fun getUsers(): Response<UsersResponse>

    @GET("operations.php")
    suspend fun getOperations(): Response<OperationsResponse>

    @POST("operations.php")
    suspend fun createOperation(@Body body: Map<String, String>): Response<OperationResponse>

    @PUT("operations.php")
    suspend fun updateOperation(@Body body: Map<String, String>): Response<OperationResponse>

    @DELETE("operations.php")
    suspend fun deleteOperation(@Body body: Map<String, Int>): Response<GenericResponse>
}

data class SessionResponse(val success: Boolean, val user: User?)
data class AuthResponse(val success: Boolean, val message: String?, val user: User?)
data class UsersResponse(val success: Boolean, val users: List<User>?)
data class OperationResponse(val success: Boolean, val operation: Operation?)
data class OperationsResponse(val success: Boolean, val operations: List<Operation>?)
data class GenericResponse(val success: Boolean, val message: String?)
