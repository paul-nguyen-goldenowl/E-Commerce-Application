package com.goldenowl.ecommerce.ui.global.bottomsheet

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.models.data.Card
import com.goldenowl.ecommerce.viewmodels.InputViewModel
import com.goldenowl.ecommerce.viewmodels.ShopViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout.END_ICON_CUSTOM
import com.google.android.material.textfield.TextInputLayout.END_ICON_NONE


class BottomSheetInsertCard(private val viewModel: ShopViewModel) :
    BottomSheetDialogFragment() {

    private val inputViewModel = InputViewModel()

    private lateinit var binding: com.goldenowl.ecommerce.databinding.ModalBottomSheetInsertCardBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        com.goldenowl.ecommerce.databinding.ModalBottomSheetInsertCardBinding.inflate(layoutInflater, container, false)
            .apply {
                binding = this
            }.root

    @SuppressLint("LongLogTag")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViews()
        setObservers()
    }

    private fun setViews() {
        binding.edtCardName.doAfterTextChanged {
            inputViewModel.checkCardName(it.toString())
        }
        binding.edtCardNumber.doAfterTextChanged {
            val text = it.toString()
            inputViewModel.checkCardNumber(text)
            formatCardNumber(text)
        }

        binding.edtCardNumber.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val c = binding.edtCardNumber.text.toString()
                if (c.isEmpty()) return@setOnFocusChangeListener
                when (c[0]) {
                    '4' -> {
                        // mastercard
                        binding.inputLayoutCardNumber.endIconMode = END_ICON_CUSTOM
                        binding.inputLayoutCardNumber.setEndIconDrawable(R.drawable.ic_master_card)
                    }
                    '5' -> {
                        //visa
                        binding.inputLayoutCardNumber.endIconMode = END_ICON_CUSTOM
                        binding.inputLayoutCardNumber.setEndIconDrawable(R.drawable.ic_visa)
                    }
                    else -> {
                        binding.inputLayoutCardNumber.endIconMode = END_ICON_NONE
                    }
                }
            }
        }
        binding.edtCvv.doAfterTextChanged {
            val s = it.toString()
            inputViewModel.checkCardCvv(s)
        }
        binding.edtExpireDate.doAfterTextChanged {
            val text = it.toString()
            inputViewModel.checkCardExpireDate(text)
            formatCardExpireDate(text)
        }

        binding.btnAddCard.setOnClickListener {

            if (inputViewModel.newCardValid.value == true) {
                val card = Card(
                    binding.edtCardName.text.toString(),
                    binding.edtCardNumber.text.toString(),
                    binding.edtExpireDate.text.toString(),
                    binding.edtCvv.text.toString()
                )
                viewModel.insertCard(card)
                if (binding.checkDefault.isSelected) {
                    val defaultCard = viewModel.listCard.value?.size ?: 0
                    viewModel.setDefaultCard(defaultCard)
                }
                dismiss()
            } else {
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.invalid_info),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.checkDefault.setOnClickListener {
            it.isSelected = !it.isSelected
        }
        binding.inputLayoutCvv.setEndIconOnClickListener {
            Toast.makeText(context, "This is help message", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatCardNumber(cardNumber: String) {
        val space = ' '
        if (cardNumber.length > 19) return
        for (i in cardNumber.indices) {
            when (i) {
                4, 9, 14 -> {
                    if (cardNumber[i] != space) {
                        val sb = StringBuilder(cardNumber)
                        sb.insert(i, space)
                        val new = sb.toString()
                        binding.edtCardNumber.apply {
                            setText(new)
                            setSelection(new.length)
                        }
                    }
                }
            }
        }
    }

    private fun formatCardExpireDate(expireDate: String) {
        val space = '/'
        for (i in expireDate.indices) {
            if (i == 2) {
                if (expireDate[i] != space) {
                    val sb = StringBuilder(expireDate)
                    sb.insert(i, space)
                    val new = sb.toString()
                    binding.edtExpireDate.apply {
                        setText(new)
                        setSelection(new.length)
                    }
                }
            }
        }
    }

    private fun setObservers() {
        inputViewModel.errorCardName.observe(viewLifecycleOwner) {
            handleCardName(it)
        }
        inputViewModel.errorCardNumber.observe(viewLifecycleOwner) {
            handleCardNumber(it)
        }
        inputViewModel.errorCardCVV.observe(viewLifecycleOwner) {
            handleCardCvv(it)
        }
        inputViewModel.errorCardExpireDate.observe(viewLifecycleOwner) {
            handleExpireDate(it)
        }
        inputViewModel.newCardValid.observe(viewLifecycleOwner) {
            handleCardValid(it)
        }
    }

    private fun handleCardValid(valid: Boolean) {
        binding.btnAddCard.isEnabled = valid
    }

    private fun handleCardName(error: String?) {
        if (error.isNullOrEmpty()) {
            binding.inputLayoutCardName.isErrorEnabled = false
        } else {
            binding.inputLayoutCardName.error = error
        }
    }

    private fun handleCardNumber(error: String?) {
        if (error.isNullOrEmpty()) {
            binding.inputLayoutCardNumber.isErrorEnabled = false
        } else {
            binding.inputLayoutCardNumber.error = error
        }
    }

    private fun handleCardCvv(error: String?) {
        if (error.isNullOrEmpty()) {
            binding.inputLayoutCvv.isErrorEnabled = false
        } else {
            binding.inputLayoutCvv.error = error
        }
    }

    private fun handleExpireDate(error: String?) {
        if (error.isNullOrEmpty()) {
            binding.inputLayoutExpireDate.isErrorEnabled = false
        } else {
            binding.inputLayoutExpireDate.error = error
        }
    }

    companion object {
        const val TAG = "BottomSheetInsertCard"
    }
}