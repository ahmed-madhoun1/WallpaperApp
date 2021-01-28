package com.ahmedmadhoun.wallpaperapp.ui.details

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.ahmedmadhoun.wallpaperapp.R
import com.ahmedmadhoun.wallpaperapp.databinding.FragmentUnsplashPhotoDetailsBinding
import com.ahmedmadhoun.wallpaperapp.utils.ConnectionType
import com.ahmedmadhoun.wallpaperapp.utils.NetworkMonitor
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_unsplash_photo_details.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class UnsplashPhotoDetailsFragment : Fragment(R.layout.fragment_unsplash_photo_details) {

    companion object {
        private const val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1
    }

    private lateinit var mContext: Context
    private val TAG = "AM"
    private val args by navArgs<UnsplashPhotoDetailsFragmentArgs>()
    private var _binding: FragmentUnsplashPhotoDetailsBinding? = null
    private val binding get() = _binding!!
    var msg: String? = ""
    var lastMsg = ""
    private lateinit var mRewardedAd: RewardedAd

    @Inject
    lateinit var job: Job

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentUnsplashPhotoDetailsBinding.bind(view)

        // Initialize Rewarded Ad
        initializeRewardedAd()

        setPhotoToImageView()


        binding.apply {
            btnDownloadPhoto.setOnClickListener {
                // Show Rewarded Ad
                if (::mRewardedAd.isInitialized) {
                    mRewardedAd.show(requireActivity()) {}
                } else {
                    Log.d(TAG, "The interstitial ad wasn't ready yet.")
                }
                askPermission()
            }
        }
    }

    private fun downloadPhoto(url: String) {
        val directoryPictures = File(Environment.DIRECTORY_PICTURES)
        if (!directoryPictures.exists()) {
            directoryPictures.mkdirs()
        }
        val downloadManager =
            requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadUri = Uri.parse(url)
        val request = DownloadManager.Request(downloadUri).apply {
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE).apply {
                setAllowedOverRoaming(false)
                setTitle(url.substring(url.lastIndexOf("/") + 1))
                setDescription("")
                setDestinationInExternalPublicDir(
                    directoryPictures.toString(),
                    url.substring(url.lastIndexOf("/") + 1)
                )
            }
        }

        val downloadId = downloadManager.enqueue(request)
        val query = DownloadManager.Query().setFilterById(downloadId)

        job = CoroutineScope(Default).launch {
            var downloading = true
            while (downloading) {
                val cursor: Cursor = downloadManager.query(query)
                cursor.moveToFirst()
                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false
                }
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                msg = statusMessage(status)
                if (msg != lastMsg) {
                    try {
                        job = CoroutineScope(Main).launch {
                            Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("AM", "downloadPhoto: ${e.message}")
                    }
                    lastMsg = msg ?: ""
                }
                cursor.close()
            }
        }
    }

    private fun statusMessage(status: Int): String {
        return when (status) {
            DownloadManager.STATUS_FAILED -> "Download has been failed, please try again"
            DownloadManager.STATUS_PAUSED -> "Paused"
            DownloadManager.STATUS_PENDING -> "Pending"
            DownloadManager.STATUS_RUNNING -> "Downloading..."
            DownloadManager.STATUS_SUCCESSFUL -> "Image downloaded successfully"
            else -> "There's nothing to download"
        }
    }

    private fun askPermission() {
        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    downloadPhoto(args.unsplashPhoto.urls.full)
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    alertDialogForDownloadPhotoPermission()
                }

                override fun onPermissionRationaleShouldBeShown(
                    request: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }

    private fun alertDialogForDownloadPhotoPermission() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission required")
            .setMessage("Permission required to save photos.")
            .setPositiveButton("Accept") { dialog, id ->
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
                )
                dialog.dismiss()
            }
            .setNegativeButton("Deny") { dialog, id -> dialog.cancel() }
            .show()
    }

    private fun setPhotoToImageView() {
        binding.apply {
            Glide.with(this@UnsplashPhotoDetailsFragment)
                .load(args.unsplashPhoto.urls.full)
                .error(R.drawable.close)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.isVisible = false
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.isVisible = false
                        btnDownloadPhoto.isVisible = true
                        return false
                    }

                })
                .into(image_view_details)
        }
    }

    // Initialize Rewarded Ad
    private fun initializeRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            requireContext(),
            "ca-app-pub-3940256099942544/5224354917",
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(
                        TAG,
                        "UnsplashPhotoDetailsFragment -> onAdFailedToLoad -> ${adError.message}"
                    )
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    mRewardedAd = rewardedAd
                }
            })
    }

    private fun checkInternetConnection() {
        networkMonitor.result = { isAvailable, type ->
            job = CoroutineScope(Main).launch {
                when (isAvailable) {
                    true -> {
                        when (type) {
                            ConnectionType.Wifi -> {
                                internetAvailable()
                            }
                            ConnectionType.Cellular -> {
                                internetAvailable()
                            }
                            else -> {
                            }
                        }
                    }
                    false -> {
                        internetUnavailable()
                    }
                }
            }

        }

    }

    private fun internetUnavailable() {
        networkMonitor.snackbar.show()
        binding.btnDownloadPhoto.isVisible = false
    }

    private fun internetAvailable() {
        networkMonitor.snackbar.dismiss()
        binding.btnDownloadPhoto.isVisible = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Check Internet Connection
        checkInternetConnection()
    }

    override fun onResume() {
        super.onResume()
        networkMonitor.register()
    }

    override fun onStop() {
        super.onStop()
        networkMonitor.unregister()
        internetAvailable()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::job.isInitialized) {
            job.cancel()
        }
    }


}