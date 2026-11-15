package es.verificador.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Size
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class ScannerActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_PLATE = "plate"
    }

    private val executor = Executors.newSingleThreadExecutor()
    private val busy = AtomicBoolean(false)
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private lateinit var previewView: PreviewView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = FrameLayout(this)
        previewView = PreviewView(this)
        container.addView(previewView, FrameLayout.LayoutParams(-1, -1))

        val instruction = TextView(this).apply {
            text = "Enfoca la matrícula dentro de la cámara"
            textSize = 18f
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundColor(0x99000000.toInt())
            setPadding(24, 28, 24, 28)
            gravity = android.view.Gravity.CENTER
        }
        val params = FrameLayout.LayoutParams(-1, -2, android.view.Gravity.TOP)
        container.addView(instruction, params)
        setContentView(container)
        startCamera()
    }

    private fun startCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            val provider = providerFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val analysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(executor) { proxy ->
                val mediaImage = proxy.image
                if (mediaImage == null || !busy.compareAndSet(false, true)) {
                    proxy.close()
                } else {
                    val image = InputImage.fromMediaImage(mediaImage, proxy.imageInfo.rotationDegrees)
                    recognizer.process(image)
                        .addOnSuccessListener { result ->
                            val text = result.text.uppercase()
                            val match = Regex("\\b\\d{4}\\s*[BCDFGHJKLMNPRSTVWXYZ]{3}\\b")
                                .find(text.replace("-", " "))
                            if (match != null && !isFinishing) {
                                val plate = match.value.replace(" ", "").replace("-", "")
                                runOnUiThread {
                                    setResult(Activity.RESULT_OK, Intent().putExtra(EXTRA_PLATE, plate))
                                    finish()
                                }
                            }
                        }
                        .addOnCompleteListener {
                            busy.set(false)
                            proxy.close()
                        }
                }
            }

            try {
                provider.unbindAll()
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
            } catch (_: Exception) {
                finish()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
        recognizer.close()
    }
}
