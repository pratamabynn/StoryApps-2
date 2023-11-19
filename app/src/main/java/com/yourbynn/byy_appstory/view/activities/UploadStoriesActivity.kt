package com.yourbynn.byy_appstory.view.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.yourbynn.byy_appstory.R
import com.yourbynn.byy_appstory.data.pref.ResultValue
import com.yourbynn.byy_appstory.databinding.ActivityUploadStoriesBinding
import com.yourbynn.byy_appstory.di.getImageUri
import com.yourbynn.byy_appstory.di.reduceFileImage
import com.yourbynn.byy_appstory.di.uriToFile
import com.yourbynn.byy_appstory.view.main.MainActivity
import com.yourbynn.byy_appstory.view.main.MainViewModel
import com.yourbynn.byy_appstory.view.main.ViewModelFactory

class UploadStoriesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadStoriesBinding
    private var currentImageUri: Uri? = null
    private val viewModel: MainViewModel by viewModels { ViewModelFactory.getInstance(this) }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Permintaan Izin Diterima", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permintaan Izin Diterima", Toast.LENGTH_LONG).show()
            }
        }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadStoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = resources.getString(R.string.add_story)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.cameraButton.setOnClickListener { startCamera() }
        binding.uploadButton.setOnClickListener { uploadImage() }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun startCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun uploadImage() {
        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, this).reduceFileImage()
            Log.d("Image File", "showImage: ${imageFile.path}")
            val description = binding.descEditText.text.toString()
            viewModel.getSession().observe(this) { story ->
                val token = story.token
                viewModel.uploadImage(token, imageFile, description).observe(this) { result ->
                    if (result != null) {
                        when (result) {
                            is ResultValue.Loading -> {
                                showLoading(true)
                            }
                            is ResultValue.Success -> {
                                showToast(result.data.message)
                                showLoading(false)
                                startActivity(Intent(this@UploadStoriesActivity, MainActivity::class.java))
                            }
                            is ResultValue.Error -> {
                                showToast(result.error)
                                showLoading(false)
                            }
                        }
                    }
                }
            }
        } ?: showToast(getString(R.string.empty_image_warning))
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}