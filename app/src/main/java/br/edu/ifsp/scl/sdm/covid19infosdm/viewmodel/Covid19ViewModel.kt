package br.edu.ifsp.scl.sdm.covid19infosdm.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import br.edu.ifsp.scl.sdm.covid19infosdm.model.Covid19Service

class Covid19ViewModel(context: Context): ViewModel() {
    private val model = Covid19Service(context)

    fun fetchCountries() = model.callGetCountries()
}