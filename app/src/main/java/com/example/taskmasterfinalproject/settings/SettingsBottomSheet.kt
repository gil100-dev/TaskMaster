package com.example.taskmasterfinalproject.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import com.example.taskmasterfinalproject.R
import com.example.taskmasterfinalproject.data.PreferencesManager
import com.example.taskmasterfinalproject.databinding.FragmentSettingsBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SettingsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentSettingsBottomSheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferencesManager(requireContext())

        // Set initial state
        when (prefs.getThemeMode()) {
            AppCompatDelegate.MODE_NIGHT_NO -> binding.radioLight.isChecked = true
            AppCompatDelegate.MODE_NIGHT_YES -> binding.radioDark.isChecked = true
            else -> binding.radioSystem.isChecked = true
        }

        when (prefs.getAccentColor()) {
            "Green" -> binding.chipGreen.isChecked = true
            "Orange" -> binding.chipOrange.isChecked = true
            "Purple" -> binding.chipPurple.isChecked = true
            else -> binding.chipIndigo.isChecked = true
        }

        // Listeners
        binding.radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.radioLight -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.radioDark -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            if (prefs.getThemeMode() != mode) {
                prefs.saveThemeMode(mode)
                AppCompatDelegate.setDefaultNightMode(mode) // This often recreates activity
                // If it doesn't recreate automatically (depends on manifest config), we might need to.
                // But usually it does.
                dismiss() // Close sheet to let transition happen cleanly
            }
        }

        binding.chipGroupColors.setOnCheckedChangeListener { group, checkedId ->
            val color = when (checkedId) {
                R.id.chipGreen -> "Green"
                R.id.chipOrange -> "Orange"
                R.id.chipPurple -> "Purple"
                else -> "Indigo" // Default
            }

            if (prefs.getAccentColor() != color) {
                prefs.saveAccentColor(color)
                // We MUST recreate the activity to apply the new Theme Style
                requireActivity().recreate()
                dismiss()
            }
        }

        binding.btnCloseSettings.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "SettingsBottomSheet"
        fun newInstance() = SettingsBottomSheet()
    }
}
