package nestsuj.apps.reactivereducerdemo.actions

import android.util.TimingLogger
import nestsuj.apps.reactivereducerdemo.services.Token
import org.rekotlin.Action

sealed class AuthAction : Action, TimedAction

data class LoginStartedAction(
        val username: String,
        override val timingLogger : TimingLogger
) : AuthAction()

data class LoginFailedAction(
        val username: String,
        val message: String,
        val t: Throwable? = null,
        override val timingLogger : TimingLogger
) : AuthAction(), FinalTimedAction

data class LoginSucceededAction(
        val username: String,
        val token: Token,
        override val timingLogger : TimingLogger
) : AuthAction(), FinalTimedAction