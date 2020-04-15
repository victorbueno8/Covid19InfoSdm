package br.edu.ifsp.scl.sdm.covid19infosdm.model

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import br.edu.ifsp.scl.sdm.covid19infosdm.model.Covid19Api.BASE_URL
import br.edu.ifsp.scl.sdm.covid19infosdm.model.Covid19Api.COUNTRIES_ENDPOINT
import br.edu.ifsp.scl.sdm.covid19infosdm.model.dataclass.CountryList
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson

class Covid19Service(val context: Context) {
    private val requestQueue = Volley.newRequestQueue(context)
    private val gson = Gson()

    /* Acesso ao web service utilizando VOLLEY */
    fun callGetCountries(): MutableLiveData<CountryList> {
        val url = "${BASE_URL}${COUNTRIES_ENDPOINT}"
        val countriesListLd = MutableLiveData<CountryList>()

        val request = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { countriesList ->
                countriesListLd.value = gson.fromJson(countriesList.toString(), CountryList::class.java)
            },
            { error -> Log.e("Covid19InfoSdm", "${error.message}") }
        )
        requestQueue.add(request)

        return countriesListLd
    }
}