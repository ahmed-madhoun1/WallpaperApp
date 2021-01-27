package com.ahmedmadhoun.wallpaperapp.ui.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.ahmedmadhoun.wallpaperapp.R
import com.ahmedmadhoun.wallpaperapp.databinding.FragmentUnsplashPhotosBinding
import com.ahmedmadhoun.wallpaperapp.model.UnsplashPhoto
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_unsplash_photos.*

@AndroidEntryPoint
class UnsplashPhotosFragment : Fragment(R.layout.fragment_unsplash_photos),
    UnsplashPhotosAdapter.OnItemClickListener {

    private val viewModel by viewModels<UnsplashViewModel>()

    private var _binding: FragmentUnsplashPhotosBinding? = null
    private val binding get() = _binding!!

    private lateinit var toggle: ActionBarDrawerToggle

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentUnsplashPhotosBinding.bind(view)

        initDrawerLayout()

        val adapter = UnsplashPhotosAdapter(this)

        binding.apply {
            recyclerView.setHasFixedSize(true)
            recyclerView.itemAnimator = null
            recyclerView.adapter = adapter.withLoadStateHeaderAndFooter(
                header = UnsplashPhotoLoadStateAdapter { adapter.retry() },
                footer = UnsplashPhotoLoadStateAdapter { adapter.retry() }
            )
            buttonRetry.setOnClickListener { adapter.retry() }
        }

        viewModel.photos.observe(viewLifecycleOwner) {
            adapter.submitData(viewLifecycleOwner.lifecycle, it)
        }

        adapter.addLoadStateListener { loadState ->
            binding.apply {
                progressBar.isVisible = loadState.source.refresh is LoadState.Loading
                recyclerView.isVisible = loadState.source.refresh is LoadState.NotLoading
                buttonRetry.isVisible = loadState.source.refresh is LoadState.Error
                textViewError.isVisible = loadState.source.refresh is LoadState.Error

                // empty view
                if (loadState.source.refresh is LoadState.NotLoading &&
                    loadState.append.endOfPaginationReached &&
                    adapter.itemCount < 1
                ) {
                    recyclerView.isVisible = false
                    textViewEmpty.isVisible = true
                } else {
                    textViewEmpty.isVisible = false
                }
            }
        }

        setHasOptionsMenu(true)
    }

    // Option Menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.menu_unsplash, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    binding.recyclerView.scrollToPosition(0)
                    viewModel.searchPhotos(query)
                    searchView.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun initDrawerLayout() {
        binding.apply {
            toggle = ActionBarDrawerToggle(
                requireActivity(),
                drawerLayout,
                R.string.open,
                R.string.close
            )
            drawerLayout.addDrawerListener(toggle)
            toggle.syncState()
            navView.setNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.item_all -> chooseCategory(getString(R.string.text_all))
                    R.id.item_animals -> chooseCategory(getString(R.string.text_animals))
                    R.id.item_nature -> chooseCategory(getString(R.string.text_nature))
                    R.id.item_people -> chooseCategory(getString(R.string.text_people))
                    R.id.item_wallpapers -> chooseCategory(getString(R.string.text_wallpapers))
                    R.id.item_technology -> chooseCategory(getString(R.string.text_technology))
                    R.id.item_new_skills -> chooseCategory(getString(R.string.text_new_skills))
                    R.id.item_current_events -> chooseCategory(getString(R.string.text_current_events))
                    R.id.item_architecture -> chooseCategory(getString(R.string.text_architecture))
                    R.id.item_fashion -> chooseCategory(getString(R.string.text_fashion))
                    R.id.item_business_Work -> chooseCategory(getString(R.string.text_business_amp_work))
                    R.id.item_experimental -> chooseCategory(getString(R.string.text_experimental))
                    R.id.item_film -> chooseCategory(getString(R.string.text_film))
                    R.id.item_health_wellness -> chooseCategory(getString(R.string.text_health_amp_wellness))
                    R.id.item_travel -> chooseCategory(getString(R.string.text_travel))
                    R.id.item_food_drink -> chooseCategory(getString(R.string.text_food_amp_drink))
                    R.id.item_arts_culture -> chooseCategory(getString(R.string.text_arts_amp_culture))
                    R.id.item_history -> chooseCategory(getString(R.string.text_history))
                    R.id.item_spirituality -> chooseCategory(getString(R.string.text_spirituality))
                    R.id.item_textures_patterns -> chooseCategory(getString(R.string.text_textures_amp_patterns))
                    R.id.item_street_photography -> chooseCategory(getString(R.string.text_street_photography))
                    R.id.item_athletics -> chooseCategory(getString(R.string.text_athletics))
                    R.id.item_interiors -> chooseCategory(getString(R.string.text_interiors))
                }
                true
            }
        }
    }

    private fun chooseCategory(query: String) {
        binding.apply {
            recyclerView.scrollToPosition(0)
            viewModel.searchPhotos(query)
            drawerLayout.closeDrawers()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(unsplashPhoto: UnsplashPhoto) {
        val action =
            UnsplashPhotosFragmentDirections.actionUnsplashPhotosFragmentToUnsplashPhotoDetailsFragment(
                unsplashPhoto
            )
        findNavController().navigate(action)
    }

}