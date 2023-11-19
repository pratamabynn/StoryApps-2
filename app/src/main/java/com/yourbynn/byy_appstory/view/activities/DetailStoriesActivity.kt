package com.yourbynn.byy_appstory.view.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.yourbynn.byy_appstory.R
import com.yourbynn.byy_appstory.databinding.ActivityDetailStoriesBinding
import com.yourbynn.byy_appstory.view.main.MainViewModel
import com.yourbynn.byy_appstory.view.main.ViewModelFactory

class DetailStoriesActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels { ViewModelFactory.getInstance(this) }
    private lateinit var binding: ActivityDetailStoriesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = resources.getString(R.string.detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val id = intent.getStringExtra(EXTRA_ID).toString()

        showLoading(true)
        viewModel.getSession().observe(this) { story ->
            val token = story.token
            viewModel.getDetailStory(token, id)
        }

        viewModel.detail.observe(this) { story->
            binding.apply {
                Glide.with(this@DetailStoriesActivity)
                    .load(story.photoUrl)
                    .into(previewImageView)

                name.text = story.name
                description.text = story.description
                showLoading(false)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    companion object {
        const val EXTRA_ID = "extra_id"
    }

}