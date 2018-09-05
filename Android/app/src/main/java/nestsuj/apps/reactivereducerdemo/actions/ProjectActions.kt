package nestsuj.apps.reactivereducerdemo.actions

import android.util.TimingLogger
import nestsuj.apps.reactivereducerdemo.services.Project
import org.rekotlin.Action

sealed class ProjectAction : Action, TimedAction

data class LoadProjectsFailedAction(
        val message: String,
        val t: Throwable? = null,
        override val timingLogger: TimingLogger
) : ProjectAction(), FinalTimedAction

data class LoadProjectsSucceededAction(
        val projects: List<Project>,
        override val timingLogger: TimingLogger
) : ProjectAction(), FinalTimedAction

data class LoadProjectsStartedAction(
        override val timingLogger: TimingLogger
) : ProjectAction()

