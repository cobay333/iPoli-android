package io.ipoli.android.common.rate

import android.preference.PreferenceManager
import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import io.ipoli.android.Constants
import io.ipoli.android.common.view.asThemedWrapper
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import java.util.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 11/15/17.
 */
class RatePopupJob : Job() {

    override fun onRunJob(params: Params): Result {
        val pm = PreferenceManager.getDefaultSharedPreferences(context)
        val shouldShowRateDialog = pm.getBoolean(Constants.KEY_SHOULD_SHOW_RATE_DIALOG, true)
        val appRun = pm.getInt(Constants.KEY_APP_RUN_COUNT, 0)
        val shouldShowRandom = Random().nextBoolean()

        if (!shouldShowRateDialog || appRun <= 2 || !shouldShowRandom) {
            return Result.SUCCESS
        }

        val c = context.asThemedWrapper()
        GlobalScope.launch(Dispatchers.Main) {
            RatePopup().show(c)
        }

        return Result.SUCCESS
    }

    companion object {
        const val TAG = "job_rate_tag"
    }
}

interface RatePopupScheduler {
    fun schedule()
}

class AndroidRatePopupScheduler : RatePopupScheduler {

    override fun schedule() {
        JobRequest.Builder(RatePopupJob.TAG)
            .setExact(5000)
            .build()
            .schedule()
    }
}