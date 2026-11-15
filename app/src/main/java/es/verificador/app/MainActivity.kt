package es.verificador.app

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var plateInput: EditText
    private lateinit var resultBox: TextView

    private val registryUrl =
        "https://apps.fomento.gob.es/crgt/servlet/ServletController?accion=inicio&modulo=datosconsulta"

    private val cameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openScanner()
        else Toast.makeText(this, "Necesitas permitir la cámara.", Toast.LENGTH_LONG).show()
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
        val scroll = ScrollView(this).apply {
            fillViewport = true
            isVerticalScrollBarEnabled = true
            setBackgroundColor(Color.rgb(244, 247, 251))
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(32))
        }

        scroll.addView(
            root,
            ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        val brand = TextView(this).apply {
            text = "VERIFICADOR"
            textSize = 29f
            setTextColor(Color.rgb(20, 58, 99))
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        root.addView(brand, matchWrap())

        val subtitle = TextView(this).apply {
            text = "IDENTIFICACIÓN DE VEHÍCULOS · ESPAÑA"
            textSize = 13f
            setTextColor(Color.rgb(90, 103, 118))
            gravity = Gravity.CENTER
            setPadding(0, dp(7), 0, dp(28))
        }
        root.addView(subtitle, matchWrap())

        val heading = TextView(this).apply {
            text = "Consulta una matrícula"
            textSize = 23f
            setTextColor(Color.rgb(30, 43, 58))
            typeface = Typeface.DEFAULT_BOLD
        }
        root.addView(heading, matchWrap())

        val explanation = TextView(this).apply {
            text = "Escribe la matrícula o utiliza la cámara para leerla."
            textSize = 15f
            setTextColor(Color.rgb(90, 103, 118))
            setPadding(0, dp(6), 0, dp(14))
        }
        root.addView(explanation, matchWrap())

        plateInput = EditText(this).apply {
            hint = "Ejemplo: 1234 ABC"
            textSize = 22f
            isSingleLine = true
            inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            setTextColor(Color.rgb(20, 30, 42))
            setHintTextColor(Color.GRAY)
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background = rounded(Color.WHITE, 12)
            filters = arrayOf(InputFilter.AllCaps(), InputFilter.LengthFilter(9))
        }
        root.addView(
            plateInput,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(60)
            ).apply { bottomMargin = dp(14) }
        )

        addButton(
            root,
            "📷  Escanear matrícula",
            Color.rgb(31, 83, 132),
            Color.WHITE,
            0
        ) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) openScanner()
            else cameraPermission.launch(Manifest.permission.CAMERA)
        }

        addButton(
            root,
            "Preparar consulta",
            Color.rgb(225, 232, 240),
            Color.rgb(30, 43, 58),
            10
        ) {
            verifyPlate()
        }

        val resultTitle = TextView(this).apply {
            text = "RESULTADO"
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(31, 83, 132))
            setPadding(0, dp(25), 0, dp(9))
        }
        root.addView(resultTitle, matchWrap())

        resultBox = TextView(this).apply {
            text = "Todavía no se ha consultado ninguna matrícula."
            textSize = 16f
            setTextColor(Color.rgb(45, 55, 68))
            setPadding(dp(17), dp(18), dp(17), dp(18))
            setLineSpacing(dp(4).toFloat(), 1f)
            background = rounded(Color.WHITE, 12)
        }
        root.addView(resultBox, matchWrap())

        addButton(
            root,
            "ABRIR REGISTRO OFICIAL",
            Color.rgb(20, 58, 99),
            Color.WHITE,
            16
        ) {
            openOfficialRegistry()
        }

        val instructions = TextView(this).apply {
            text = "CÓMO HACER LA CONSULTA\n\n" +
                "1. Prepara la matrícula en esta aplicación.\n" +
                "2. Pulsa ABRIR REGISTRO OFICIAL.\n" +
                "3. En la web del Ministerio selecciona «Vehículo».\n" +
                "4. Selecciona «Matrícula» e introduce la matrícula copiada.\n" +
                "5. Pulsa el botón de búsqueda del portal.\n\n" +
                "IMPORTANTE\n" +
                "El resultado del REAT no equivale por sí solo a una " +
                "confirmación de TAXI o VTC. Si no aparece un vehículo, " +
                "eso tampoco demuestra que sea particular. La aplicación " +
                "todavía no recibe automáticamente la respuesta del registro."
            textSize = 14f
            setTextColor(Color.rgb(83, 94, 107))
            setPadding(dp(4), dp(20), dp(4), 0)
            setLineSpacing(dp(3).toFloat(), 1f)
        }
        root.addView(instructions, matchWrap())

        setContentView(scroll)
    }

    private fun verifyPlate() {
        val plate = plateInput.text.toString()
            .uppercase(Locale.ROOT)
            .replace(" ", "")
            .replace("-", "")

        if (!plate.matches(Regex("\\d{4}[BCDFGHJKLMNPRSTVWXYZ]{3}"))) {
            resultBox.text =
                "MATRÍCULA NO VÁLIDA\n\nComprueba el formato español. Ejemplo: 1234 ABC."
            return
        }

        plateInput.setText("${plate.take(4)} ${plate.drop(4)}")

        resultBox.text =
            "MATRÍCULA: ${plate.take(4)} ${plate.drop(4)}\n\n" +
            "ESTADO: PENDIENTE DE CONSULTA OFICIAL\n\n" +
            "La matrícula tiene un formato válido. " +
            "Todavía no se ha confirmado si el vehículo es TAXI, VTC " +
            "u otro tipo de vehículo.\n\n" +
            "Pulsa «ABRIR REGISTRO OFICIAL» para continuar la consulta."
    }

    private fun openOfficialRegistry() {
        val plate = plateInput.text.toString()
            .uppercase(Locale.ROOT)
            .replace(" ", "")
            .replace("-", "")

        if (!plate.matches(Regex("\\d{4}[BCDFGHJKLMNPRSTVWXYZ]{3}"))) {
            verifyPlate()
            return
        }

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Matrícula", plate))

        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(registryUrl)))
            Toast.makeText(
                this,
                "Matrícula copiada. Pégala en el registro oficial.",
                Toast.LENGTH_LONG
            ).show()
        } catch (_: Exception) {
            Toast.makeText(
                this,
                "No se pudo abrir el portal oficial.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun openScanner() {
        scannerResult.launch(Intent(this, ScannerActivity::class.java))
    }

    private fun addButton(
        root: LinearLayout,
        label: String,
        backgroundColor: Int,
        textColor: Int,
        top: Int,
        action: () -> Unit
    ) {
        val button = Button(this).apply {
            text = label
            textSize = 16f
            isAllCaps = false
            setTextColor(textColor)
            background = rounded(backgroundColor, 12)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            minHeight = dp(54)
            setOnClickListener { action() }
        }
        root.addView(
            button,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(top) }
        )
    }

    private fun rounded(color: Int, radius: Int) =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }

    private fun dp(value: Int) =
        (value * resources.displayMetrics.density).toInt()

    private fun matchWrap() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
}
