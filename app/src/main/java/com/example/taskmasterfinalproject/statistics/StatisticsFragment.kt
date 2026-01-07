package com.example.taskmasterfinalproject.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.taskmasterfinalproject.databinding.FragmentStatisticsBinding

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StatisticsViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val db = com.example.taskmasterfinalproject.db.TaskDatabase.getInstance(requireContext())
                val repository = com.example.taskmasterfinalproject.data.TaskRepository(db.taskDao())
                @Suppress("UNCHECKED_CAST")
                return StatisticsViewModel(repository) as T
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.stats.observe(viewLifecycleOwner) { result ->
            binding.textTotalTasks.text = result.total.toString()
            binding.textCompletedTasks.text = result.completed.toString()
            binding.textActiveTasks.text = result.active.toString()
            
            // Update Custom View
            binding.ringCompletion.setProgress(result.completionRate)
            
            binding.textHighPriority.text = result.highPriorityPending.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
