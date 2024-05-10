package com.example.desafiogryfo

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.desafiogryfo.databinding.ActivityCameraPreviewBackBinding
import com.example.desafiogryfo.model.Image
import com.example.desafiogryfo.util.NetworkUtils
import com.google.android.material.snackbar.Snackbar
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraPreviewBackActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraPreviewBackBinding

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraSelector: CameraSelector

    private var imageCapture: ImageCapture? = null

    private lateinit var  imgCaptureExecutor: ExecutorService

    private lateinit var saveBackImg: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCameraPreviewBackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        imgCaptureExecutor = Executors.newSingleThreadExecutor()

        startCamera()

        binding.btnTakeSecondPhoto.setOnClickListener {
                backTakePhoto()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    blinkPreview()
                }



                binding.root.postDelayed({
                    CoroutineScope(Dispatchers.IO).launch {
                    val base64ImageBack = convertImageToBase64(saveBackImg)
                    val image = Image()
                    image.document_img = base64ImageBack

                    val gson = Gson()
                    val imageJson = gson.toJson(image)

                    val network = NetworkUtils()

                    println("BASE64 $image")
                        val response = network.post(imageJson)
                        // Handle the response from the server (optional)
                        if (response.isSuccessful) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(binding.root.context, "Imagem enviada com sucesso!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Log.e("CameraPreview", "Erro ao enviar imagem: ${response.code}")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(binding.root.context, "Erro ao enviar imagem!", Toast.LENGTH_SHORT).show()

                            }
                        }

                    }
                 }, 1000)
        }

    }

    private fun startCamera(){
        cameraProviderFuture.addListener({

            imageCapture = ImageCapture.Builder().build()

            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraPreviewBack.surfaceProvider)
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

            } catch (e: Exception){
                Log.e("cameraPreview", "Falha ao abrir a câmera")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun backTakePhoto(){

        imageCapture?.let{
            val fileName = "FOTO_JPEG_${System.currentTimeMillis()}"
            val fileBack = File(externalMediaDirs[0], fileName)
            saveBackImg = fileBack

            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(fileBack).build()

            it.takePicture(
                outputFileOptions,
                imgCaptureExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        Log.i("CameraPreview", "A imagem foi salva na pasta: ${fileBack.toUri()}")
                        Snackbar.make(binding.root, "Foto salva com sucesso.", Snackbar.LENGTH_SHORT).show()

                    }

                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(binding.root.context, "Erro ao salvar foto.", Toast.LENGTH_LONG).show()
                        Log.e("CameraPreview", "Exceção ao gravar arquivo da foto: $exception")
                    }

                }
            )

        }

    }
    @RequiresApi(Build.VERSION_CODES.M)
    private fun blinkPreview(){
        binding.root.postDelayed({
            binding.root.foreground = ColorDrawable(Color.WHITE)
            binding.root.postDelayed({
                binding.root.foreground = null
            }, 50)
        }, 100)
    }

    private fun convertImageToBase64(imageFile: File): String {
        val inputStream = FileInputStream(imageFile)
        val bytes = inputStream.readBytes()
        inputStream.close()

        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }
}