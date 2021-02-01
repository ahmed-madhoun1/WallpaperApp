package com.ahmedmadhoun.wallpaperapp.ui.search

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.ahmedmadhoun.wallpaperapp.R
import com.ahmedmadhoun.wallpaperapp.adapters.UnsplashPhotoLoadStateAdapter
import com.ahmedmadhoun.wallpaperapp.adapters.UnsplashPhotosAdapter
import com.ahmedmadhoun.wallpaperapp.databinding.FragmentUnsplashPhotosSearchBinding
import com.ahmedmadhoun.wallpaperapp.model.UnsplashPhoto
import com.ahmedmadhoun.wallpaperapp.viewmodel.UnsplashViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UnsplashPhotosSearchFragment : Fragment(R.layout.fragment_unsplash_photos_search),
    UnsplashPhotosAdapter.OnItemClickListener {

    private val TAG = "AM"
    private val viewModel by viewModels<UnsplashViewModel>()
    private var _binding: FragmentUnsplashPhotosSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var mInterstitialAd: InterstitialAd

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentUnsplashPhotosSearchBinding.bind(view)

        // Initialize Search View
        initializeSearchView()

        // Initialize Mobile Ads
        initializeMobileAds()

        // Initialize Interstitial Ad
        initializeInterstitialAd()

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

        viewModel.searchPhotos("")

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

    }

    // Init Search View
    private fun initializeSearchView() {
        binding.apply {
            searchView.setIconifiedByDefault(true)
            searchView.isFocusable = true
            searchView.isIconified = false
            searchView.requestFocusFromTouch()
            searchView.setOnCloseListener {
                searchView.clearFocus()
                requireActivity().onBackPressed()
                true
            }
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
                    if (newText != null) {
                        binding.recyclerView.scrollToPosition(0)
                        viewModel.searchPhotos(newText)
                    }
                    return true
                }
            })
        }
    }

    // Initialize Mobile Ads
    private fun initializeMobileAds() {
        MobileAds.initialize(requireContext())
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
    }

    // Initialize Interstitial Ad
    private fun initializeInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            requireContext(),
            "ca-app-pub-3940256099942544/1033173712",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                }
            })

    }

    // On Recycler View Item Click Listener
    override fun onItemClick(unsplashPhoto: UnsplashPhoto) {
        // show Interstitial Ad
        if (::mInterstitialAd.isInitialized) {
            mInterstitialAd.show(requireActivity())
        } else {
            Log.d(TAG, "The interstitial ad wasn't ready yet.")
        }
        val action =
            UnsplashPhotosSearchFragmentDirections.actionSearchFragmentToUnsplashPhotoDetailsFragment(
                unsplashPhoto
            )
        findNavController().navigate(action)
    }

}