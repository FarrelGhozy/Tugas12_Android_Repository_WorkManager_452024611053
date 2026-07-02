package com.tugas12.repository.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tugas12.repository.data.local.PostEntity
import com.tugas12.repository.databinding.ItemPostBinding

/**
 * RecyclerView Adapter untuk menampilkan daftar posts.
 * Menggunakan ListAdapter + DiffUtil untuk performa optimal.
 */
class PostAdapter : ListAdapter<PostEntity, PostAdapter.PostViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PostViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: PostEntity) {
            binding.apply {
                tvId.text = "${post.id}"
                tvUserId.text = "User ID: ${post.userId}"
                tvTitle.text = post.title
                tvBody.text = post.body
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PostEntity>() {
        override fun areItemsTheSame(oldItem: PostEntity, newItem: PostEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PostEntity, newItem: PostEntity): Boolean {
            return oldItem == newItem
        }
    }
}
