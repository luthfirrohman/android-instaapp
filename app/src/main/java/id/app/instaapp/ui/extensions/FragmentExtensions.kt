package id.app.instaapp.ui.extensions

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.app.instaapp.di.appComponent

inline fun <reified VM : ViewModel> Fragment.appViewModels(): Lazy<VM> = lazy(LazyThreadSafetyMode.NONE) {
    ViewModelProvider(this, requireContext().appComponent.viewModelFactory())[VM::class.java]
}