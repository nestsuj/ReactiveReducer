package nestsuj.apps.reactivereducerdemo.actions.creators

import android.util.Log
import android.util.TimingLogger
import nestsuj.apps.reactivereducerdemo.AppExecutors
import nestsuj.apps.reactivereducerdemo.actions.LoginFailedAction
import nestsuj.apps.reactivereducerdemo.actions.LoginStartedAction
import nestsuj.apps.reactivereducerdemo.actions.LoginSucceededAction
import nestsuj.apps.reactivereducerdemo.okhttp.TokenInterceptor
import nestsuj.apps.reactivereducerdemo.services.TodolyService
import nestsuj.apps.reactivereducerdemo.services.Token
import nestsuj.apps.reactivereducerdemo.states.AppState
import okhttp3.Credentials
import org.rekotlin.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class AuthActionCreator @Inject constructor(private val todolyService: TodolyService, private val tokenInterceptor: TokenInterceptor, private val appExecutors: AppExecutors) {
    fun createLoginAction(username: String, password: String): AsyncActionCreator<AppState, StoreType<AppState>> {
        val timingLogger = TimingLogger("LoginAction", "Created")

        return { _, store, _ ->
            store.dispatch(LoginStartedAction(username, timingLogger))
            timingLogger.addSplit("About to start executing login task")
            executeLoginTask(username, password, store.dispatchFunction, timingLogger)
            timingLogger.addSplit("Login task is executing")
        }
    }

    private fun executeLoginTask(username: String, password: String, dispatch: DispatchFunction, timingLogger: TimingLogger) {
        appExecutors.networkIO().execute {
            val credentials = Credentials.basic(username, password)
            todolyService.authenticate(credentials.toString()).enqueue(object : Callback<Token> {
                override fun onFailure(call: Call<Token>, t: Throwable) {
                    timingLogger.addSplit("Invoking LoginFailedAction due to thrown exception")
                    dispatch.invoke(LoginFailedAction(username, t.message ?: "", t, timingLogger))
                    timingLogger.addSplit("Invoked LoginFailedAction")
                }

                override fun onResponse(call: Call<Token>, response: Response<Token>) {
                    Log.d("Middleware", "Received response for request url: ${response.raw().request().url()}")
                    if (response.isSuccessful && response.body() != null) {
                        response.body()?.let {
                            timingLogger.addSplit("Invoking LoginSucceededAction")
                            tokenInterceptor.token = it.tokenString
                            dispatch.invoke(LoginSucceededAction(username, it, timingLogger))
                            timingLogger.addSplit("Invoked LoginSuceededAction")
                        }
                    } else {
                        timingLogger.addSplit("Invoking LoginFailedAction due to failed response")
                        dispatch.invoke(LoginFailedAction(username, response.errorBody()?.string()
                                ?: response.message(), timingLogger = timingLogger))
                        timingLogger.addSplit("Invoked LoginFailedAction")
                    }
                }

            })
        }
    }
}