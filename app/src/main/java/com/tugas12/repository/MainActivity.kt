package com.tugas12.repository

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tugas12.repository.databinding.ActivityMainBinding
import com.tugas12.repository.ui.MainViewModel
import com.tugas12.repository.ui.PostAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * MainActivity — entry point aplikasi.
 *
 * Activity ini hanya berinteraksi dengan ViewModel.
 * ViewModel yang mengatur Repository, dan Repository yang
 * mengatur data source (local/network).
 * 
 * Jadi Activity gak perlu tahu soal DAO, Retrofit, atau Worker!
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSwipeRefresh()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = PostAdapter()
        binding.rvPosts.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
            setHasFixedSize(true)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshData()
        }
    }

    private fun observeData() {
        // Observe daftar posts
        lifecycleScope.launch {
            viewModel.posts.collectLatest { posts ->
                adapter.submitList(posts)
                binding.layoutEmptyState.visibility =
                    if (posts.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            }
        }

        // Observe status refreshing
        lifecycleScope.launch {
            viewModel.isRefreshing.collectLatest { refreshing ->
                binding.swipeRefresh.isRefreshing = refreshing
                binding.progressBar.visibility =
                    if (refreshing) android.view.View.VISIBLE else android.view.View.GONE
            }
        }

        // Observe snackbar messages
        lifecycleScope.launch {
            viewModel.snackbarMessage.collectLatest { message ->
                message?.let {
                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
                    viewModel.clearSnackbar()
                }
            }
        }
    }
}
