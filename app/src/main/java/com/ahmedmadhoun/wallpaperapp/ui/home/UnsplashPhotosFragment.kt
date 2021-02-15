package com.ahmedmadhoun.wallpaperapp.ui.home

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.LayoutDirection
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.ahmedmadhoun.wallpaperapp.R
import com.ahmedmadhoun.wallpaperapp.databinding.FragmentUnsplashPhotosBinding
import com.ahmedmadhoun.wallpaperapp.model.UnsplashPhoto
import com.ahmedmadhoun.wallpaperapp.adapters.UnsplashPhotoLoadStateAdapter
import com.ahmedmadhoun.wallpaperapp.adapters.UnsplashPhotosAdapter
import com.ahmedmadhoun.wallpaperapp.application.WallpaperApplication
import com.ahmedmadhoun.wallpaperapp.viewmodel.UnsplashViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UnsplashPhotosFragment : Fragment(R.layout.fragment_unsplash_photos),
    UnsplashPhotosAdapter.OnItemClickListener {

    private val TAG = "AM"
    private val viewModel by viewModels<UnsplashViewModel>()
    private var _binding: FragmentUnsplashPhotosBinding? = null
    private val binding get() = _binding!!
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var mInterstitialAd: InterstitialAd

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentUnsplashPhotosBinding.bind(view)

        initDrawerLayout()

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

            cardViewSearch.setOnClickListener {
                val action =
                    UnsplashPhotosFragmentDirections.actionUnsplashPhotosFragmentToSearchFragment()
                findNavController().navigate(action)
            }
            cardViewMenu.setOnClickListener {
                if (drawerLayout.isDrawerOpen(GravityCompat.getAbsoluteGravity(Gravity.LEFT, LayoutDirection.LTR))) {
                    drawerLayout.closeDrawer(GravityCompat.getAbsoluteGravity(Gravity.LEFT, LayoutDirection.LTR))
                } else {
                    drawerLayout.openDrawer(GravityCompat.getAbsoluteGravity(Gravity.LEFT, LayoutDirection.LTR))
                }
            }
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
            }
        }

        setHasOptionsMenu(true)

    }

    // On Options Item Selected
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
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
            UnsplashPhotosFragmentDirections.actionUnsplashPhotosFragmentToUnsplashPhotoDetailsFragment(
                unsplashPhoto
            )
        findNavController().navigate(action)
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
                    R.id.item_architecture -> chooseCategory(getString(R.string.text_architecture))
                    R.id.item_fashion -> chooseCategory(getString(R.string.text_fashion))
                    R.id.item_business_Work -> chooseCategory(getString(R.string.text_business_and_work))
                    R.id.item_experimental -> chooseCategory(getString(R.string.text_experimental))
                    R.id.item_film -> chooseCategory(getString(R.string.text_film))
                    R.id.item_health_wellness -> chooseCategory(getString(R.string.text_health_and_wellness))
                    R.id.item_travel -> chooseCategory(getString(R.string.text_travel))
                    R.id.item_food_drink -> chooseCategory(getString(R.string.text_food_and_drink))
                    R.id.item_arts_culture -> chooseCategory(getString(R.string.text_arts_and_culture))
                    R.id.item_history -> chooseCategory(getString(R.string.text_history))
                    R.id.item_spirituality -> chooseCategory(getString(R.string.text_spirituality))
                    R.id.item_textures_patterns -> chooseCategory(getString(R.string.text_textures_and_patterns))
                    R.id.item_street_photography -> chooseCategory(getString(R.string.text_street_photography))
                    R.id.item_athletics -> chooseCategory(getString(R.string.text_athletics))
                    R.id.item_interiors -> chooseCategory(getString(R.string.text_interiors))
                    R.id.item_share_app -> shareApp()
                    R.id.item_rate_app -> rateApp()
                    R.id.item_owner_instagram -> ownerLink("https://www.instagram.com/ahmed_madhoun1/")
                    R.id.item_owner_linkedin -> ownerLink("https://www.linkedin.com/in/ahmed-madhoun-b3b9331ba/")
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

    // Initialize Mobile Ads
    private fun initializeMobileAds() {
        MobileAds.initialize(requireContext())
        val adRequest = AdRequest.Builder().build()
        binding.adView.apply {
            loadAd(adRequest)
        }
    }

    // Initialize Interstitial Ad
    private fun initializeInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            requireContext(),
            getString(R.string.interstitial_wallpapers_ad_mob_id),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                }
            })

    }

    private fun shareApp() {
        binding.drawerLayout.closeDrawers()
        val appURL =
            Uri.parse("play.google.com/store/apps/details?id=${requireActivity().packageName}")
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Hey, Check out this app: $appURL")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(intent, "Share To:"))
    }

    private fun rateApp() {
        binding.drawerLayout.closeDrawers()
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=${requireActivity().packageName}")
                )
            )
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=play.google.com/store/apps/details?id=${requireActivity().packageName}")
                )
            )
        }

    }

    private fun ownerLink(url: String) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        requireContext().startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}