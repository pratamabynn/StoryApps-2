package com.yourbynn.byy_appstory.data.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yourbynn.byy_appstory.data.response.ListStoryItem
import com.yourbynn.byy_appstory.databinding.ItemsStoriesBinding
import com.yourbynn.byy_appstory.view.activities.DetailStoriesActivity

class StoriesAdapter : PagingDataAdapter<ListStoryItem, StoriesAdapter.MyViewHolder>(DIFF_CALLBACK)  {

    class MyViewHolder(private val binding: ItemsStoriesBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(story: ListStoryItem) {
            binding.usernameTextView.text = story.name
            binding.description.text = story.description
            Glide.with(itemView)
                .load(story.photoUrl)
                .into(binding.previewImageView)
            binding.story.setOnClickListener {
                val intent = Intent(it.context, DetailStoriesActivity::class.java)
                intent.putExtra(DetailStoriesActivity.EXTRA_ID, story.id)
                itemView.context.startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(itemView.context as Activity).toBundle())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemsStoriesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data)
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}