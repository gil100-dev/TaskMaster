package com.example.taskmasterfinalproject.ai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.taskmasterfinalproject.databinding.FragmentAiCoachBottomSheetBinding
import com.example.taskmasterfinalproject.model.Task
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.fragment.app.viewModels

class AiCoachBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentAiCoachBottomSheetBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AiViewModel by viewModels()

    private var taskTitle: String = ""
    private var taskDesc: String = ""
    private var taskPriority: Int = 0

    companion object {
        const val TAG = "AiCoachBottomSheet"
        private const val ARG_TITLE = "arg_title"
        private const val ARG_DESC = "arg_desc"
        private const val ARG_PRIORITY = "arg_priority"
        private const val ARG_SUBTASKS = "arg_subtasks"
        private const val ARG_TASK_ID = "arg_task_id"

        fun newInstance(task: Task?, subtasks: List<String> = emptyList()): AiCoachBottomSheet {
            val fragment = AiCoachBottomSheet()
            val args = Bundle()
            if (task != null) {
                args.putString(ARG_TASK_ID, task.id)
                args.putString(ARG_TITLE, task.title)
                args.putString(ARG_DESC, task.description)
                args.putInt(ARG_PRIORITY, task.priority ?: 0)
                args.putStringArrayList(ARG_SUBTASKS, ArrayList(subtasks))
            } else {
                args.putString(ARG_TASK_ID, "")
                args.putString(ARG_TITLE, "General Guidance")
                args.putString(ARG_DESC, "I have no specific task selected.")
                args.putInt(ARG_PRIORITY, 0)
                args.putStringArrayList(ARG_SUBTASKS, ArrayList())
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            taskId = it.getString(ARG_TASK_ID, "")
            taskTitle = it.getString(ARG_TITLE, "")
            taskDesc = it.getString(ARG_DESC, "")
            taskPriority = it.getInt(ARG_PRIORITY, 0)
            subtaskList = it.getStringArrayList(ARG_SUBTASKS)?.toList() ?: emptyList()
        }
    }

    private var taskId: String = ""
    private var subtaskList: List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiCoachBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    var onApplySubtasks: ((List<AiSubtask>) -> Unit)? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val prefs = com.example.taskmasterfinalproject.data.PreferencesManager(requireContext())

        // UI Setup
        binding.btnClose.setOnClickListener { dismiss() }
        
        binding.btnRegenerate.setOnClickListener {
            viewModel.generateAdvice(taskTitle, taskDesc, taskPriority, subtaskList)
        }

        binding.btnGenerateSubtasks.setOnClickListener {
            viewModel.generateSubtasksPlan(taskTitle, taskDesc, taskPriority)
        }

        binding.btnDiscard.setOnClickListener {
             viewModel.clearPlan()
        }

        binding.btnApply.setOnClickListener {
            viewModel.subtaskPlan.value?.let { plan ->
                onApplySubtasks?.invoke(plan.subtasks)
                com.google.android.material.snackbar.Snackbar.make(binding.root, "Subtasks created!", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
                dismiss() 
            }
        }

        // Observe ViewModel
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBarAi.visibility = if (loading) View.VISIBLE else View.GONE
            if (loading) {
                binding.textAiResponse.visibility = View.GONE
                binding.layoutPlanPreview.visibility = View.GONE
            } else {
                binding.textAiResponse.visibility = View.VISIBLE
            }
            binding.btnRegenerate.isEnabled = !loading
            binding.btnGenerateSubtasks.isEnabled = !loading
        }

        viewModel.aiResponse.observe(viewLifecycleOwner) { response ->
            if (response.isNotBlank()) {
                binding.textAiResponse.text = androidx.core.text.HtmlCompat.fromHtml(
                    response, androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
                )
                // Save to Cache if it's a valid response (not loading message or error)
                // Simple heuristic: if length > 50 and not starting with "AI Error" or "Connecting"
                if (response.length > 50 && !response.startsWith("AI Error") && !response.startsWith("Connecting") && !response.startsWith("Thinking")) {
                    if (taskId.isNotEmpty()) {
                        prefs.saveAiAdvice(taskId, response)
                    }
                }
            }
        }
        
        viewModel.subtaskPlan.observe(viewLifecycleOwner) { plan ->
             if (plan != null) {
                 binding.layoutPlanPreview.visibility = View.VISIBLE
                 binding.textAiResponse.visibility = View.GONE 
                 
                 val summary = StringBuilder()
                 summary.append("Time Estimate: ${plan.totalMinutesRange}\n")
                 summary.append("Best Time: ${plan.bestTimeOfDay} (${plan.reason})\n\n")
                 
                 plan.subtasks.forEachIndexed { index, sub ->
                     summary.append("${index + 1}. ${sub.title} (${sub.minutes}m)\n")
                 }
                 binding.textPlanSummary.text = summary.toString()
             } else {
                 binding.layoutPlanPreview.visibility = View.GONE
                 if (binding.textAiResponse.visibility == View.GONE && !viewModel.isLoading.value!!) {
                     binding.textAiResponse.visibility = View.VISIBLE
                 }
             }
        }

        // Init Logic: Check Cache or Generate
        if (viewModel.aiResponse.value.isNullOrBlank() && viewModel.subtaskPlan.value == null) {
            val cached = if (taskId.isNotEmpty()) prefs.getAiAdvice(taskId) else null
            if (!cached.isNullOrBlank()) {
                 viewModel.setCachedResponse(cached)
            } else {
                 viewModel.generateAdvice(taskTitle, taskDesc, taskPriority, subtaskList)
            }
        }
    }
    
    override fun onStart() {
        super.onStart()
        // Set peek height to ~1/6 screen (Compact)
        val dialog = dialog as? BottomSheetDialog
        dialog?.behavior?.let { behavior ->
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            behavior.peekHeight = (resources.displayMetrics.heightPixels / 6)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
