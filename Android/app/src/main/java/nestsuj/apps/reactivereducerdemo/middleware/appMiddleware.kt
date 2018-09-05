package nestsuj.apps.reactivereducerdemo.middleware

import android.util.Log
import nestsuj.apps.reactivereducerdemo.states.AppState
import org.rekotlin.Middleware

internal val loggingMiddleware: Middleware<AppState> = { _, getState ->
    { next ->
        {action ->
            Log.d("Received ${action::class.java.simpleName}", "$action")
            next(action)
            Log.d("State Updated", getState().toString())
        }
    }
}