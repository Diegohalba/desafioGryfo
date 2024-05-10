package com.example.desafiogryfo

import android.content.Intent
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
import com.example.desafiogryfo.databinding.ActivityCameraPreviewBinding
import com.example.desafiogryfo.model.Image
import com.google.android.material.snackbar.Snackbar
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraPreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraPreviewBinding

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraSelector: CameraSelector

    private var imageCapture: ImageCapture? = null

    private lateinit var  imgCaptureExecutor: ExecutorService

    private lateinit var saveFrontImg: File


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCameraPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        imgCaptureExecutor = Executors.newSingleThreadExecutor()

        startCamera()

        binding.btnTakePhoto.setOnClickListener{
            frontTakePhoto()
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                blinkPreview()
            }

            binding.root.postDelayed({
                val base64Image = convertImageToBase64(saveFrontImg)
                val image = Image()
                image.face_img = base64Image


            }, 1000)

        }

        binding.btnOpenBackCamera.setOnClickListener{
            abrirTelaDePreviewBack()
        }

    }

    private fun startCamera(){
        cameraProviderFuture.addListener({

            imageCapture = ImageCapture.Builder().build()

            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

            } catch (e: Exception){
                Log.e("cameraPreview", "Falha ao abrir a câmera")
            }
        }, ContextCompat.getMainExecutor(this))
    }

     private fun frontTakePhoto(){

        imageCapture?.let{
            val fileName = "FOTO_JPEG_${System.currentTimeMillis()}"
            val fileFront = File(externalMediaDirs[0], fileName)
            saveFrontImg = fileFront

            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(fileFront).build()

            it.takePicture(
                outputFileOptions,
                imgCaptureExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        Log.i("CameraPreview", "A imagem foi salva na pasta: ${fileFront.toUri()}")
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

    private fun abrirTelaDePreviewBack(){
        val intentCameraPreview = Intent(this, CameraPreviewBackActivity::class.java)
        startActivity(intentCameraPreview)
    }

    private fun convertImageToBase64(imageFile: File): String {
        val inputStream = FileInputStream(imageFile)
        val bytes = inputStream.readBytes()
        inputStream.close()

        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }
}