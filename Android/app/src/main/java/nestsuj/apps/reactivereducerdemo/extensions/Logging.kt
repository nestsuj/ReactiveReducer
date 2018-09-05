package nestsuj.apps.reactivereducerdemo.extensions

import android.app.Activity
import android.util.Log
import androidx.fragment.app.Fragment

fun Activity.log(message: String) {
    Log.d(this.javaClass.simpleName, message)
}

fun Fragment.log(message: String) {
    Log.d(this.javaClass.simpleName, message)
}