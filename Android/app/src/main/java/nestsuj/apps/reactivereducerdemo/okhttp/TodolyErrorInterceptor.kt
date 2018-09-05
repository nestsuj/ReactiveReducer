package nestsuj.apps.reactivereducerdemo.okhttp

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import nestsuj.apps.reactivereducerdemo.services.TodolyError
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import okio.BufferedSource
import okio.Okio
import java.net.URLDecoder
import javax.inject.Inject

private const val HTTP_STATUS_CODE_BAD_REQUEST = 400
private const val HTTP_STATUS_CODE_NOT_UNAUTHORIZED = 401
private const val HTTP_STATUS_CODE_FORBIDDEN = 403
private const val HTTP_STATUS_CODE_NOT_FOUND = 404
private const val HTTP_STATUS_CODE_CONFLICT = 409

/**
 * This interceptor is necessary because the bloody API doesn't return correct http status codes on error.
 *
 * To make it worse they even return a json error object in a 200 response, meaning converting response to expected type is impossible.
 *
 * Can't handle it in retrofit converters or call adapters, so are moving the handling here
 *
 * Note: this interceptor loads the entire response body into memory i.e. large responses may cause OOMs
 *
 * Also twice the parsing is twice the delay...
 *
 */
class TodolyErrorInterceptor @Inject constructor (moshi: Moshi) : Interceptor {
    private val adapter: JsonAdapter<TodolyError> = moshi.adapter(TodolyError::class.java)

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        if (response.isSuccessful) {
            response.body()?.let { responseBody ->
                val responseBuilder = response.newBuilder()
                val contentType = responseBody.contentType()
                var content = responseBody.string() // Note: Complete responsebody is loaded to memory
                return try {
                    val error: TodolyError? = adapter.fromJson(content)

                    error?.let {
                        val todolyErrorCode = TodolyErrorCode.values().find { todolyErrorCode -> todolyErrorCode.todolyErrorCode == error.errorCode }
                        responseBuilder.code(todolyErrorCode?.httpStatusCode ?: HTTP_STATUS_CODE_BAD_REQUEST)
                        content = URLDecoder.decode(it.errorMessage, "UTF-8")
                    }
                    responseBuilder.body(ResponseBody.create(contentType, content)).build()
                } catch (_: Exception) {
                    responseBuilder.body(ResponseBody.create(contentType, content)).build()
                }
            }
        }

        return response
    }
}

enum class TodolyErrorCode (val todolyErrorCode: Int, val httpStatusCode: Int) {
    INVALID_REQUEST(101, HTTP_STATUS_CODE_BAD_REQUEST),
    NOT_AUTHENTICATED(102, HTTP_STATUS_CODE_NOT_UNAUTHORIZED),
    INVALID_TOKEN(103, HTTP_STATUS_CODE_BAD_REQUEST),
    NOT_SUPPORTED_FEATURE(104, HTTP_STATUS_CODE_NOT_FOUND),
    ACCOUNT_ALREADY_EXISTS(201, HTTP_STATUS_CODE_CONFLICT),
    PASSWORD_TOO_SHORT(202, HTTP_STATUS_CODE_BAD_REQUEST),
    INVALID_ID(301, HTTP_STATUS_CODE_NOT_FOUND),
    INVALID_INPUT_DATA(302, HTTP_STATUS_CODE_BAD_REQUEST),
    INVALID_PARENT_ITEM_ID(303, HTTP_STATUS_CODE_NOT_FOUND),
    INVALID_PROJECT_ID(304, HTTP_STATUS_CODE_NOT_FOUND),
    TOO_SHORT_PROJECT_NAME(305, HTTP_STATUS_CODE_BAD_REQUEST),
    INVALID_FULLNAME(306, HTTP_STATUS_CODE_BAD_REQUEST),
    INVALID_EMAIL_ADDRESS(307, HTTP_STATUS_CODE_BAD_REQUEST),
    TOO_SHORT_ITEM_NAME(308, HTTP_STATUS_CODE_BAD_REQUEST),
    NO_ACCESS_TO_ITEM(309, HTTP_STATUS_CODE_FORBIDDEN),
    NO_ACCESS_TO_PARENT_PROJECT(401, HTTP_STATUS_CODE_FORBIDDEN),
    NO_ACCESS_TO_PROJECT(402, HTTP_STATUS_CODE_FORBIDDEN)
}