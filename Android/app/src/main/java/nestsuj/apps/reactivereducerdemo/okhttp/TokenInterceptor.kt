package nestsuj.apps.reactivereducerdemo.okhttp

import okhttp3.Interceptor
import okhttp3.Response

private const val HEADER_AUTHENTICATION: String = "Authentication"
private const val HEADER_TOKEN: String = "Token"

class TokenInterceptor : Interceptor {
    var token: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        if (token == null || originalRequest.header(HEADER_AUTHENTICATION) != null) {
            return chain.proceed(originalRequest)
        }

        token?.let {
            return chain.proceed(
                    originalRequest
                            .newBuilder()
                            .removeHeader(HEADER_TOKEN)
                            .addHeader(HEADER_TOKEN, it)
                            .build()
            )
        }

        return chain.proceed(originalRequest)
    }
}