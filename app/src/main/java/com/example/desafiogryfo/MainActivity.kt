package com.example.desafiogryfo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.desafiogryfo.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnOpenCamera.setOnClickListener{

            cameraProviderResult.launch(android.Manifest.permission.CAMERA)
        }


    }

    private val cameraProviderResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){
            if(it){
                abrirTelaDePreview()
            }else {
               Snackbar.make(binding.root, "Acesso a câmera não concedido.", Snackbar.LENGTH_INDEFINITE).show()
            }
    }

    private fun abrirTelaDePreview(){
        val intentCameraPreview = Intent(this, CameraPreviewActivity::class.java)
        startActivity(intentCameraPreview)
    }


}
