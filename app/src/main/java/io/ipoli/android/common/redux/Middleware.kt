package io.ipoli.android.common.redux

import io.ipoli.android.common.redux.MiddleWare.Result.Continue
import io.ipoli.android.common.redux.MiddleWare.Result.Stop
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/20/2018.
 */
interface MiddleWare<in S : State> {

    fun onCreate() {}

    fun execute(state: S, dispatcher: Dispatcher, action: Action): Result

    sealed class Result {
        object Continue : Result()
        object Stop : Result()
    }
}

interface SimpleMiddleware<in S : State> : MiddleWare<S> {

    override fun execute(state: S, dispatcher: Dispatcher, action: Action): MiddleWare.Result {
        onExecute(state, dispatcher, action)
        return Continue
    }

    fun onExecute(state: S, dispatcher: Dispatcher, action: Action)
}

interface AsyncMiddleware<in S : State> : MiddleWare<S> {
    override fun execute(state: S, dispatcher: Dispatcher, action: Action): MiddleWare.Result {
        GlobalScope.launch(Dispatchers.IO) {
            onExecute(state, dispatcher, action)
        }
        return Continue
    }

    fun onExecute(state: S, dispatcher: Dispatcher, action: Action)
}

class CompositeMiddleware<in S : State>(private val middleware: List<MiddleWare<S>>) :
    MiddleWare<S> {

    override fun onCreate() {
        for(m in middleware) {
            m.onCreate()
        }
    }

    override fun execute(state: S, dispatcher: Dispatcher, action: Action): MiddleWare.Result {
        for (m in middleware) {
            if (m.execute(state, dispatcher, action) == Stop) {
                return Stop
            }
        }

        return Continue
    }
}