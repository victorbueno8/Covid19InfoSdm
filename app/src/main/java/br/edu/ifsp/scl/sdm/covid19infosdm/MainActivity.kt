package br.edu.ifsp.scl.sdm.covid19infosdm

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import br.edu.ifsp.scl.sdm.covid19infosdm.model.dataclass.ByCountryResponseList
import br.edu.ifsp.scl.sdm.covid19infosdm.model.dataclass.ByCountryResponseListItem
import br.edu.ifsp.scl.sdm.covid19infosdm.model.dataclass.DayOneResponseList
import br.edu.ifsp.scl.sdm.covid19infosdm.model.dataclass.DayOneResponseListItem
import br.edu.ifsp.scl.sdm.covid19infosdm.viewmodel.Covid19ViewModel
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: Covid19ViewModel
    private lateinit var countryAdapter: ArrayAdapter<String>
    private lateinit var countryNameSlugMap: MutableMap<String, String>

    // Classe para servios que serao acessados no spinner hardcoded
    private enum class Information(val type: String){
        DAY_ONE("Day one"),
        BY_COUNTRY("By country")
    }

    // Classe para o status que sera buscado no servio no spinner
    private enum class Status(val type: String){
        CONFIRMED("Confirmed"),
        RECOVERED("Recovered"),
        DEATHS("Deaths")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = Covid19ViewModel(this)

        countryAdapterInit()

        informationAdapterInit()

        statusAdapterInit()
    }

    fun onRetrieveClick(view: View) {
        when (infoSp.selectedItem.toString()) {
            Information.DAY_ONE.type -> { fetchDayOne() }
            Information.BY_COUNTRY.type -> { fetchByCountry() }
        }
    }

    private fun countryAdapterInit() {
        // Preenchido pelo web service
        countryAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)
        countryNameSlugMap = mutableMapOf()
        countrySp.adapter = countryAdapter
        viewModel.fetchCountries().observe(
            this,
            Observer { countryList ->
                countryList.sortedBy { it.country }.forEach { countryListItem ->
                    if (countryListItem.country.isNotEmpty()) {
                        countryAdapter.add(countryListItem.country)
                        countryNameSlugMap[countryListItem.country] = countryListItem.slug
                    }
                }
            }
        )
    }

    private fun informationAdapterInit() {
        val informationList = arrayListOf<String>()
        Information.values().forEach { informationList.add(it.type) }

        infoSp.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, informationList)
        infoSp.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    Information.DAY_ONE.ordinal -> {
                        viewModeTv.visibility = View.VISIBLE
                        viewModeRg.visibility = View.VISIBLE
                    }
                    Information.BY_COUNTRY.ordinal -> {
                        viewModeTv.visibility = View.GONE
                        viewModeRg.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun statusAdapterInit() {
        val statusList = arrayListOf<String>()
        Status.values().forEach { statusList.add(it.type) }

        statusSp.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, statusList)
    }

    private fun fetchDayOne() {
        val countrySlug = countryNameSlugMap[countrySp.selectedItem.toString()]!!

        viewModel.fetchDayOne(countrySlug, statusSp.selectedItem.toString()).observe(
            this,
            Observer { casesList ->
                if (viewModeTextRb.isChecked) {
                    // Modo texto
                    modoGrafico(ligado = false)
                    resultTv.text = casesListToString(casesList)
                }
                else {
                    // Modo gráfico
                    modoGrafico(ligado = true)
                    resultGv.removeAllSeries()
                    resultGv.gridLabelRenderer.resetStyles()

                    // Preparando os pontos
                    val pointsArrayList = arrayListOf<DataPoint>()
                    casesList.forEach {
                        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(it.date.substring(0,10))
                        val point = DataPoint(date, it.cases.toDouble())
                        pointsArrayList.add(point)
                    }
                    val pointSeries = LineGraphSeries(pointsArrayList.toTypedArray())
                    resultGv.addSeries(pointSeries)

                    if (pointsArrayList.isNotEmpty()) {
                        // Formatando Gráfico
                        resultGv.gridLabelRenderer.setHumanRounding(false)
                        resultGv.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(this)

                        resultGv.gridLabelRenderer.numHorizontalLabels = 4
                        val primeiraDataX = Date(pointsArrayList.first().x.toLong())
                        val ultimaDataX = Date(pointsArrayList.last().x.toLong())
                        resultGv.viewport.setMinX(primeiraDataX.time.toDouble())
                        resultGv.viewport.setMaxX(ultimaDataX.time.toDouble())
                        resultGv.viewport.isXAxisBoundsManual = true

                        resultGv.gridLabelRenderer.numVerticalLabels = 4
                        val primeiraDataY = Date(pointsArrayList.first().y.toLong())
                        val ultimaDataY = Date(pointsArrayList.last().y.toLong())
                        resultGv.viewport.setMinY(primeiraDataY.time.toDouble())
                        resultGv.viewport.setMaxY(ultimaDataY.time.toDouble())
                        resultGv.viewport.isYAxisBoundsManual = true
                    }
                }
            }
        )
    }

    private fun fetchByCountry() {
        val countrySlug = countryNameSlugMap[countrySp.selectedItem.toString()]!!

        modoGrafico(ligado = false)
        viewModel.fetchByCountry(countrySlug, statusSp.selectedItem.toString()).observe(
            this,
            Observer { casesList ->
                resultTv.text = casesListToString(casesList)
            }
        )
    }

    private fun modoGrafico(ligado: Boolean) {
        if (ligado) {
            resultTv.visibility = View.GONE
            resultGv.visibility = View.VISIBLE
        }
        else {
            resultTv.visibility = View.VISIBLE
            resultGv.visibility = View.GONE
        }
    }

    private inline fun <reified T: ArrayList<*>> casesListToString(responseList: T): String {
        val resultSb = StringBuffer()

        responseList.forEach() {
            when(T::class.java) {
                DayOneResponseList::class.java -> {
                    with (it as DayOneResponseListItem) {
                        resultSb.append("Casos: ${this.cases}\n")
                        resultSb.append("Data: ${this.date.substring(0,10)}\n\n")
                    }
                }
                ByCountryResponseList::class.java -> {
                    with (it as ByCountryResponseListItem) {
                        this.province.takeIf { !this.province.isNullOrEmpty() }?.let { province ->
                            resultSb.append("Estado/Província: ${province}\n")
                        }
                        this.city.takeIf { !this.city.isNullOrEmpty() }?.let { city ->
                            resultSb.append("Cidade: ${city}\n")
                        }

                        resultSb.append("Casos: ${this.cases}\n")
                        resultSb.append("Data: ${this.date.substring(0,10)}\n\n")
                    }
                }
            }
        }
        if (resultSb.isEmpty()) {
            resultSb.append("Sem casos registrados");
        }
        return resultSb.toString()
    }

}
