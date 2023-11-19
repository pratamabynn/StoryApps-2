package com.yourbynn.byy_appstory.view.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.yourbynn.byy_appstory.R
import com.yourbynn.byy_appstory.data.adapter.LoadingAdapter
import com.yourbynn.byy_appstory.data.adapter.StoriesAdapter
import com.yourbynn.byy_appstory.databinding.ActivityMainBinding
import com.yourbynn.byy_appstory.view.activities.MapsActivity
import com.yourbynn.byy_appstory.view.activities.UploadStoriesActivity
import com.yourbynn.byy_appstory.view.activities.WelcomeActivity

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels { ViewModelFactory.getInstance(this) }
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvStory.layoutManager = LinearLayoutManager(this)

        showLoading(true)
        viewModel.getSession().observe(this) { user ->
            val token = user.token
            if (!user.isLogin) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
            getData(token)
        }

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this@MainActivity, UploadStoriesActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getData(token: String) {
        val adapter = StoriesAdapter()
        binding.rvStory.adapter = adapter.withLoadStateFooter(
            footer = LoadingAdapter {
                adapter.retry()
            }
        )
        viewModel.getStories(token).observe(this) {
            adapter.submitData(lifecycle, it)
            showLoading(false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.changeLanguage -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
            }
            R.id.logout -> {
                viewModel.logout()
                return true
            }
            R.id.maps -> {
                Intent(this, MapsActivity::class.java).apply {
                    startActivity(this)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}