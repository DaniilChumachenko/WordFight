package com.chvma.wordfight.ads

import android.app.Activity
import java.lang.ref.WeakReference

object ActivityProvider {
    private var activityRef: WeakReference<Activity>? = null

    fun setActivity(activity: Activity) {
        activityRef = WeakReference(activity)
    }

    fun getActivity(): Activity? = activityRef?.get()
}
