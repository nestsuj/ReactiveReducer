package nestsuj.apps.reactivereducerdemo.actions

import android.util.TimingLogger

interface TimedAction {
    val timingLogger: TimingLogger
}

interface FinalTimedAction : TimedAction