package com.goldenowl.ecommerce.ui.global.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentSettingsBinding
import com.goldenowl.ecommerce.models.auth.UserManager
import com.goldenowl.ecommerce.models.auth.UserManager.Companion.TYPEEMAIL
import com.goldenowl.ecommerce.models.data.SettingsManager
import com.goldenowl.ecommerce.ui.global.BaseFragment
import com.goldenowl.ecommerce.utils.BaseLoadingStatus
import com.goldenowl.ecommerce.utils.Constants
import com.goldenowl.ecommerce.utils.FieldValidators
import com.goldenowl.ecommerce.utils.SimpleDateFormatHelper
import com.goldenowl.ecommerce.utils.Utils.getDateTime
import com.goldenowl.ecommerce.viewmodels.AuthViewModel
import com.goldenowl.ecommerce.viewmodels.TextInputViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import java.util.*

class SettingsFragment : BaseFragment<FragmentSettingsBinding>() {
    val TAG = "SettingsFragment"


    private lateinit var userManager: UserManager
    private lateinit var imageActivityResultLauncher: ActivityResultLauncher<Intent>
    private val textInputViewModel: TextInputViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun getViewBinding(): FragmentSettingsBinding {
        return FragmentSettingsBinding.inflate(layoutInflater)
    }

    override fun init() {
        super.init()
        userManager = UserManager.getInstance(requireContext())

        val callback = ActivityResultCallback<ActivityResult> { result ->
            if (result == null)
                return@ActivityResultCallback
            if (result.resultCode != Activity.RESULT_OK)
                return@ActivityResultCallback
            val data: Intent = result.data ?: return@ActivityResultCallback

            val file: Uri? = data.data
            file.let {
                if (it != null) {
                    Glide
                        .with(this@SettingsFragment).load(it)
                        .apply(ProfileFragment.options)
                        .into(binding.ivUserAvatar)
                }
            }
            authViewModel.updateAvatar(file)
        }


        imageActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult(), callback)
    }


    override fun setViews() {

        binding.layoutUserPassword.visibility =
            if (userManager.logType == TYPEEMAIL) View.VISIBLE else View.GONE
        val settingsManager = SettingsManager(requireContext())


        restoreViews(settingsManager)

        with(binding) {

            tvChangePassword.setOnClickListener {
                authViewModel.changePasswordStatus.value = BaseLoadingStatus.NONE
                openBottomSheet()
            }

            userManager.avatar.let {
                if (!it.isNullOrBlank()) {
                    Glide.with(this@SettingsFragment).load(it)
                        .apply(ProfileFragment.options)
                        .placeholder(R.drawable.ic_user)
                        .into(binding.ivUserAvatar)
                }
            }

            ivUserAvatar.setOnClickListener {
                changeAvatar()
            }

            edtDob.setOnClickListener {
                try {
                    if (childFragmentManager.findFragmentByTag("date_picker") != null) {
                        return@setOnClickListener
                    }
                    val datePicker =
                        MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Select date")
                            .build()

                    datePicker.addOnPositiveButtonClickListener { time ->
                        val diff = SimpleDateFormatHelper.countDate(Date(time), Date())
                        if (diff < Constants.MIN_AGE) {
                            textInputViewModel.errorDoB.value = getString(R.string.invalid_age)
                            return@addOnPositiveButtonClickListener
                        }
                        val date = getDateTime(time)
                        binding.edtDob.setText(date)
                    }
                    datePicker.show(childFragmentManager, "date_picker");
                } catch (e: Exception) {
                    Log.e(TAG, "setViews: datePicker", e)
                }
            }
        }
    }

    private fun restoreViews(
        settingsManager: SettingsManager,
    ) {
        val fullName: String = userManager.name
        val dob: String = userManager.dob

        val userSettings: Map<String, Boolean> = settingsManager.getUserSettings()
        with(binding) {
            edtName.setText(fullName)
            edtDob.setText(dob)
            swSale.isChecked = userSettings[SettingsManager.KEY_NOTIFICATION_SALE]!!
            swDeliStatus.isChecked = userSettings[SettingsManager.KEY_NOTIFICATION_DELIVERY_STATUS_CHANGE]!!
            swNewArrivals.isChecked = userSettings[SettingsManager.KEY_NOTIFICATION_ARRIVES]!!
        }
    }

    private fun changeAvatar() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        if (requireActivity().packageManager.resolveActivity(intent, 0) != null) {
            imageActivityResultLauncher.launch(intent)
        } else {
            Toast.makeText(activity, getString(R.string.no_image_picker), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openBottomSheet() {
        val bottomSheetChangePassword = BottomSheetChangePassword(userManager)
        bottomSheetChangePassword.enterTransition = View.GONE
        bottomSheetChangePassword.show(
            parentFragmentManager,
            BottomSheetChangePassword.TAG
        )
    }

    override fun setObservers() {
        with(textInputViewModel) {

            errorName.observe(viewLifecycleOwner) { errorName ->
                validName(errorName)
            }

            errorDoB.observe(viewLifecycleOwner) { errorDoB ->
                validDoB(errorDoB)
            }
        }

    }

    private fun validDoB(errorDoB: String?) {
        with(binding) {
            if (errorDoB != null) {
                inputLayoutDob.error = errorDoB
            } else {
                inputLayoutDob.isErrorEnabled = false
                inputLayoutDob.endIconMode =
                    TextInputLayout.END_ICON_CUSTOM
            }
        }
    }

    private fun validName(errorName: String?) {
        with(binding) {
            if (errorName != null) {
                inputLayoutName.error = errorName
            } else {
                inputLayoutName.isErrorEnabled = false
                inputLayoutName.endIconMode =
                    TextInputLayout.END_ICON_CUSTOM
            }
        }
    }

    override fun setUpListener() {
        super.setUpListener()
        with(binding) {
            edtName.addTextChangedListener(object : FieldValidators.TextChange {
                override fun onTextChanged(s: CharSequence?) {
                    textInputViewModel.checkName(edtName.text.toString())
                }
            })
            edtDob.addTextChangedListener(object : FieldValidators.TextChange {
                override fun onTextChanged(s: CharSequence?) {
                    textInputViewModel.checkDoB(edtDob.text.toString())
                }
            })
            edtPassword.addTextChangedListener(object : FieldValidators.TextChange {
                override fun onTextChanged(s: CharSequence?) {
                    textInputViewModel.checkPassword(edtPassword.text.toString(), 0)
                    textInputViewModel.setLoginFormValid()
                }
            })
        }
    }

    override fun setAppbar() {
        binding.topAppBar.collapsingToolbarLayout.title = getString(R.string.settings)
        binding.topAppBar.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.topAppBar.toolbar.setOnMenuItemClickListener {
            onMenuClick(it)
        }
    }

    private fun onMenuClick(item: MenuItem): Boolean {
        return if (item.itemId == R.id.ic_check) {

            if (binding.edtName.text.toString().isEmpty()) {
                textInputViewModel.errorName.value = "Required field"
                return false
            }
            if (binding.edtDob.text.toString().isEmpty()) {
                textInputViewModel.errorDoB.value = "Required field"
                return false
            }
            if (textInputViewModel.errorName.value.isNullOrEmpty()
                && textInputViewModel.errorDoB.value.isNullOrEmpty()
            ) {
                val settings = mapOf(
                    SettingsManager.KEY_NOTIFICATION_SALE to binding.swSale.isChecked,
                    SettingsManager.KEY_NOTIFICATION_ARRIVES to binding.swNewArrivals.isChecked,
                    SettingsManager.KEY_NOTIFICATION_DELIVERY_STATUS_CHANGE to binding.swDeliStatus.isChecked,
                )
                val user = userManager.getUser()
                user.name = binding.edtName.text.toString().trim()
                user.dob = binding.edtDob.text.toString().trim()
                user.settings = settings
                authViewModel.saveUserSettings(user)
                findNavController().navigateUp()
            } else {
                Log.d(TAG, "onOptionsItemSelected: input error")
            }
            false
        } else
            super.onOptionsItemSelected(item)
    }
}


