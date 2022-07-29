package com.goldenowl.ecommerce.ui.global.shop

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentQrBinding
import com.goldenowl.ecommerce.ui.customview.MessageDialogFragment
import com.goldenowl.ecommerce.ui.global.profile.QrViewModel
import com.goldenowl.ecommerce.utils.Constants
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView


class QRFragment : Fragment(), ZXingScannerView.ResultHandler {

    private lateinit var mScannerView: ZXingScannerView
    private lateinit var binding: FragmentQrBinding
    private val qrViewModel: QrViewModel by activityViewModels()
    private var mFlash = false
    private var mAutoFocus = false
    private var permissionGranted = false
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                permissionGranted = true
            } else {
                requestPermission()
            }
        }

    private val settingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            checkPermission()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionGranted = checkPermission()

        if (!permissionGranted) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        val fragment: DialogFragment = MessageDialogFragment.newInstance(
            MessageDialogFragment.Companion.DialogType.OPTION,
            getString(R.string.access_camera),
            getString(R.string.camera_qr),
            object : MessageDialogFragment.MessageDialogListener {
                override fun onDialogPositiveClick(dialog: DialogFragment?) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", requireActivity().packageName, null)
                    intent.data = uri
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    settingLauncher.launch(intent)
                    dialog?.dismiss()
                }

                override fun onDialogNegativeClick(dialog: DialogFragment?) {
                    findNavController().navigateUp()
                }
            }
        )
        fragment.show(requireActivity().supportFragmentManager, "request_permission")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentQrBinding.inflate(inflater, container, false)
        mScannerView = binding.scannerView
        if (savedInstanceState != null) {
            mFlash = savedInstanceState.getBoolean(FLASH_STATE, false);
            mAutoFocus = savedInstanceState.getBoolean(AUTO_FOCUS_STATE, true);
        } else {
            mFlash = false;
            mAutoFocus = true;
        }
        setViews()
        setObservers()
        return binding.root
    }

    private fun setObservers() {
        qrViewModel.timeOut.observe(viewLifecycleOwner) {
            if (it) {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.timeout))
                    .setMessage(getString(R.string.ask_continue_scanning))
                    .setNegativeButton(R.string.cancel) { _, _ ->
                        findNavController().navigateUp()
                    }
                    .setPositiveButton(R.string.ok) { dialog, _ ->
                        dialog.dismiss()
                        qrViewModel.setTimeout()
                    }
                    .show()
            }
        }
    }


    private fun setViews() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        mScannerView.setSquareViewFinder(true)
        binding.ivFlash.setOnClickListener {
            if (!permissionGranted) {
                requestPermission()
                return@setOnClickListener
            }
            mFlash = !mFlash
            it.isSelected = mFlash
            mScannerView.flash = mFlash
        }

        val params = CoordinatorLayout.LayoutParams(
            CoordinatorLayout.LayoutParams.WRAP_CONTENT,
            CoordinatorLayout.LayoutParams.WRAP_CONTENT,
        )
        val display: Display = requireActivity().windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val width: Int = size.x
        val height: Int = size.y

        params.setMargins(0, -width * 7 / 8, 0, 0)
        params.gravity = Gravity.CENTER
        binding.tvStatus.apply {
            elevation = 50f
            layoutParams = params
        }
    }

    override fun onResume() {
        super.onResume()
        mScannerView.setResultHandler(this);
        mScannerView.startCamera()
        mScannerView.isActivated
        if (permissionGranted) qrViewModel.setTimeout()
        mScannerView.flash = mFlash;
        mScannerView.setAutoFocus(mAutoFocus)
    }

    override fun onPause() {
        super.onPause()
        mScannerView.stopCamera()
        qrViewModel.cancelTimeout()
        closeMessageDialog()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(FLASH_STATE, mFlash)
        outState.putBoolean(AUTO_FOCUS_STATE, mAutoFocus)
    }

    companion object {
        val TAG = "QRFragment"
        private const val FLASH_STATE = "FLASH_STATE"
        private const val AUTO_FOCUS_STATE = "AUTO_FOCUS_STATE"
    }

    override fun handleResult(rawResult: Result) {
        try {
            val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(context, notification)
            r.play()
            val res = rawResult.text
            if (validQR(res)) {
                val productId = res.split('=')[1]
                showMessageDialog(getString(R.string.product_found, productId), productId)
            } else {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.invalid_qr_code))
                    .setMessage(getString(R.string.ask_continue_scanning))
                    .setNegativeButton(R.string.cancel) { _, _ ->
                        findNavController().navigateUp()
                    }
                    .setPositiveButton(R.string.ok) { dialog, _ ->
                        dialog.dismiss()
                        onResume()
                        qrViewModel.setTimeout()
                    }
                    .show()
            }
        } catch (e: Exception) {
            showMessageDialog(getString(R.string.scan_error), null)
        }
    }

    private fun validQR(text: String?): Boolean {
        if (text.isNullOrBlank()) return false
        return text.indexOf("Product=") == 0
    }

    private fun showMessageDialog(message: String?, result: String?) {
        val fragment: DialogFragment = MessageDialogFragment.newInstance(
            MessageDialogFragment.Companion.DialogType.MESSAGE,
            "Scan Results",
            message,
            object : MessageDialogFragment.MessageDialogListener {
                override fun onDialogPositiveClick(dialog: DialogFragment?) {
                    if (result != null)
                        findNavController().navigate(
                            R.id.action_go_detail, bundleOf(Constants.KEY_PRODUCT_ID to result)
                        )
                    else
                        dialog?.dismiss()
                }

                override fun onDialogNegativeClick(dialog: DialogFragment?) {
                }
            }
        )
        fragment.show(requireActivity().supportFragmentManager, "scan_results")
    }

    private fun closeMessageDialog() {
        closeDialog("scan_results")
    }


    private fun closeDialog(dialogName: String?) {
        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        val fragment = fragmentManager.findFragmentByTag(dialogName) ?: return
        val dialog = fragment as DialogFragment
        if (dialog != null) {
            dialog.dismiss()
        }
    }
}
