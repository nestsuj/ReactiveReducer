package nestsuj.apps.reactivereducerdemo.services

import com.squareup.moshi.Json
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path


interface TodolyService {
    @GET("authentication/token.json")
    fun authenticate(@Header("Authorization") authorizationHeader: String): Call<Token>

    @GET("projects.json")
    fun getProjects(): Call<List<Project>>

    @GET("projects/{projectId}/items.json")
    fun getTodos(@Path("projectId") projectId : Int) : Call<List<Todo>>
}

/**
 * Representation of an error body from todoly API
 */
data class TodolyError(
        @Json(name = "ErrorMessage")
        val errorMessage: String,

        @Json(name = "ErrorCode")
        val errorCode: Int
)

/**
 * Authentication token data
 */
data class Token(
        @Json(name = "TokenString")
        val tokenString: String,

        @Json(name = "UserEmail")
        val userEmail: String,

        @Json(name = "ExpirationTime")
        val expirationTime: String
)

/**
 * A single project item
 */
data class Project(
        @Json(name = "Id")
        val id : Int,

        @Json(name = "Content")
        val content : String,

        @Json(name = "ItemsCount")
        val itemsCount : String,

        @Json(name = "Children")
        val children : List<Project>
)

/**
 * A single todo item
 */
data class Todo(
        @Json(name = "Id")
        val id : Int,

        @Json(name = "Content")
        val content : String,

        @Json(name = "ProjectId")
        val projectId : Int,

        @Json(name = "ParentId")
        val parentId : Int?,

        @Json(name = "Children")
        val children : List<Todo>
)