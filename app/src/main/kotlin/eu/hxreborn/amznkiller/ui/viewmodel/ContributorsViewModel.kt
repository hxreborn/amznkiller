package eu.hxreborn.amznkiller.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import eu.hxreborn.amznkiller.contributors.ContributorRow
import eu.hxreborn.amznkiller.contributors.ContributorsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONException
import java.io.IOException

private const val TAG = "ContributorsViewModel"

class ContributorsViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val _rows = MutableStateFlow<List<ContributorRow>>(emptyList())
    val rows: StateFlow<List<ContributorRow>> = _rows.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                _rows.value = ContributorsRepository(application).load()
            } catch (e: IOException) {
                Log.w(TAG, "contributors snapshot unreadable", e)
            } catch (e: JSONException) {
                Log.w(TAG, "contributors snapshot malformed", e)
            }
        }
    }
}
