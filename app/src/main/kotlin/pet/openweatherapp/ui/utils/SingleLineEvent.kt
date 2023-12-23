package pet.openweatherapp.ui.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

// This class is needed so that when returning to a fragment, the previous state is not returned and functions are not re-called
class SingleLineEvent<T> : MutableLiveData<T>() {
    private val pending: AtomicBoolean = AtomicBoolean(false) // thread safe boolean

    override fun setValue(value: T) {
        pending.set(true)
        super.setValue(value)
    }

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner) { value ->
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(value)
            }
        }
    }
}