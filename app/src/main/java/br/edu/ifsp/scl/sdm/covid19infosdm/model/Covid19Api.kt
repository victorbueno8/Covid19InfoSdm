package br.edu.ifsp.scl.sdm.covid19infosdm.model

import br.edu.ifsp.scl.sdm.covid19infosdm.model.dataclass.ByCountryResponseList
import br.edu.ifsp.scl.sdm.covid19infosdm.model.dataclass.DayOneResponseList
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

object Covid19Api {
    val BASE_URL = "https://api.covid19api.com/"

    val COUNTRIES_ENDPOINT = "countries"

    interface RetrofitServices {
//        Get List Of Cases Per Country Per Province By Case Type From The First Recorded Case
        @GET("dayone/country/{countryName}/status/{status}")
        fun getDayOne(
            @Path("countryName") countryName: String,
            @Path("status") status: String
        ): Call<DayOneResponseList>

//        Get List Of Cases Per Country Per Province By Case Type
        @GET("country/{countryName}/status/{status}")
        fun getByCountry(
            @Path("countryName") countryName: String,
            @Path("status") status: String
        ): Call<ByCountryResponseList>
    }
}