package es.verificador.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var plateInput: EditText
    private lateinit var resultBox: TextView

    private val cameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openScanner()
        else resultBox.text = "Para escanear matrículas necesitas permitir el acceso a la cámara."
    }

    private val scannerResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val plate = result.data?.getStringExtra(ScannerActivity.EXTRA_PLATE).orEmpty()
            if (plate.isNotBlank()) {
                plateInput.setText(plate)
                verifyPlate()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildScreen()
    }

    private fun buildScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(30), dp(24), dp(24))
            setBackgroundColor(Color.rgb(246, 248, 251))
        }

        val brand = TextView(this).apply {
            text = "VERIFICADOR"
            textSize = 28f
            setTextColor(Color.rgb(18, 59, 102))
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        root.addView(brand, matchWrap())

        val subtitle = TextView(this).apply {
            text = "IDENTIFICACIÓN DE VEHÍCULOS · ESPAÑA"
            textSize = 12f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
            setPadding(0, dp(6), 0, dp(30))
        }
        root.addView(subtitle, matchWrap())

        val heading = TextView(this).apply {
            text = "Consulta una matrícula"
            textSize = 21f
            setTextColor(Color.rgb(30, 43, 58))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        root.addView(heading, matchWrap())

        plateInput = EditText(this).apply {
            hint = "Ejemplo: 1234 ABC"
            textSize = 20f
            isSingleLine = true
            setTextColor(Color.BLACK)
            setPadding(dp(16), dp(14), dp(16), dp(14))
            setBackgroundColor(Color.WHITE)
            filters = arrayOf(android.text.InputFilter.AllCaps())
        }
        val inputParams = matchWrap().apply { topMargin = dp(14); bottomMargin = dp(14) }
        root.addView(plateInput, inputParams)

        val scanButton = Button(this).apply {
            text = "📷  ESCANEAR MATRÍCULA"
            isAllCaps = false
            setOnClickListener {
                if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) openScanner()
                else cameraPermission.launch(Manifest.permission.CAMERA)
            }
        }
        root.addView(scanButton, matchWrap())

        val searchButton = Button(this).apply {
            text = "CONSULTAR MATRÍCULA"
            isAllCaps = false
            setOnClickListener { verifyPlate() }
        }
        val searchParams = matchWrap().apply { topMargin = dp(8) }
        root.addView(searchButton, searchParams)

        resultBox = TextView(this).apply {
            text = "Elige escanear la matrícula o escribirla manualmente."
            textSize = 15f
            setTextColor(Color.rgb(55, 65, 75))
            setPadding(dp(16), dp(18), dp(16), dp(18))
            setBackgroundColor(Color.WHITE)
        }
        val resultParams = matchWrap().apply { topMargin = dp(24) }
        root.addView(resultBox, resultParams)

        val note = TextView(this).apply {
            text = "La consulta oficial de TAXI/VTC se conectará cuando dispongamos de acceso autorizado a los registros."
            textSize = 12f
            setTextColor(Color.GRAY)
            setPadding(dp(4), dp(18), dp(4), 0)
        }
        root.addView(note, matchWrap())

        setContentView(root)
    }

    private fun verifyPlate() {
        val plate = plateInput.text.toString()
            .uppercase(Locale.ROOT)
            .replace(" ", "")
            .replace("-", "")
        if (!plate.matches(Regex("\\d{4}[BCDFGHJKLMNPRSTVWXYZ]{3}"))) {
            resultBox.text = "Matrícula no reconocida. Comprueba el formato español: 1234 ABC."
            return
        }
        resultBox.text = "MATRÍCULA: ${plate.take(4)} ${plate.drop(4)}\n\nESTADO: PENDIENTE DE VERIFICACIÓN OFICIAL\n\nLa matrícula se ha leído correctamente, pero todavía no se ha consultado ningún registro oficial. VERIFICADOR no dará por confirmado que sea TAXI, VTC o particular hasta disponer de una fuente autorizada."
    }

    private fun openScanner() {
        scannerResult.launch(Intent(this, ScannerActivity::class.java))
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun matchWrap() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )
}
