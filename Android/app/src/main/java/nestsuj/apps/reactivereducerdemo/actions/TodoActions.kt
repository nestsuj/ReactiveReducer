package nestsuj.apps.reactivereducerdemo.actions

import android.util.TimingLogger
import nestsuj.apps.reactivereducerdemo.services.Todo
import org.rekotlin.Action

sealed class TodoAction : Action, TimedAction

data class FetchTodosStartedAction(
        val projectId: Int,
        override val timingLogger : TimingLogger
) : TodoAction()

data class FetchTodosFailedAction(
        val projectId: Int,
        val message: String,
        val t: Throwable? = null,
        override val timingLogger : TimingLogger
) : TodoAction(), FinalTimedAction

data class FetchTodosSuccededAction(
        val projectId: Int,
        val todos : List<Todo>,
        override val timingLogger : TimingLogger
) : TodoAction(), FinalTimedAction