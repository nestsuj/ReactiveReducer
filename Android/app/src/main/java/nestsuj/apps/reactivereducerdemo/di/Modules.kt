package nestsuj.apps.reactivereducerdemo.di

import android.app.Application
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import nestsuj.apps.reactivereducerdemo.middleware.loggingMiddleware
import nestsuj.apps.reactivereducerdemo.okhttp.TodolyErrorInterceptor
import nestsuj.apps.reactivereducerdemo.okhttp.TokenInterceptor
import nestsuj.apps.reactivereducerdemo.reducers.appReducer
import nestsuj.apps.reactivereducerdemo.services.TodolyService
import nestsuj.apps.reactivereducerdemo.states.AppState
import nestsuj.apps.reactivereducerdemo.states.AuthenticationState
import nestsuj.apps.reactivereducerdemo.states.ProjectsState
import nestsuj.apps.reactivereducerdemo.states.TodosState
import nestsuj.apps.reactivereducerdemo.utils.calculateDiskCacheSize
import nestsuj.apps.reactivereducerdemo.utils.createDefaultCacheDir
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.rekotlin.Store
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class ApplicationModule(private val application: Application){

    @Provides
    fun provideApplication(): Application = application
}

@Module
class ServiceModule(private val todolyServiceUrl: String) {
    @Provides
    fun provideMoshi() : Moshi {
        return Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }

    @Provides
    @Singleton
    fun provideAuthenticationTokenInterceptor(): TokenInterceptor {
        return TokenInterceptor()
    }

    @Provides
    fun provideOkHttpCache(application: Application): Cache {
        val cacheDir = createDefaultCacheDir(application)
        return Cache(cacheDir, calculateDiskCacheSize(cacheDir))
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(cache: Cache, tokenInterceptor: TokenInterceptor): OkHttpClient =
            OkHttpClient.Builder()
                    .cache(cache)
                    .addInterceptor(tokenInterceptor)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()

    @Provides
    fun provideTodolyService(okHttpClient: OkHttpClient, moshi: Moshi, todolyErrorInterceptor: TodolyErrorInterceptor): TodolyService =
            Retrofit.Builder()
                    .client(okHttpClient
                            .newBuilder()
                            .addInterceptor(todolyErrorInterceptor)
                            .build()
                    )
                    .baseUrl(todolyServiceUrl)
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .build()
                    .create(TodolyService::class.java)
}

@Module
class ReduxModule {
    @Provides
    @Singleton
    fun provideMainStore(): Store<AppState> {
        val state = AppState(AuthenticationState(), ProjectsState(), TodosState())
        return Store(reducer = ::appReducer,
                state = state,
                middleware = arrayListOf(
                        loggingMiddleware
                )
        )
    }
}