package com.example.taskmasterfinalproject.ai

// מעביר מידע בין רכיבים
import android.os.Bundle
// משמש לניפוח (Inflate) קבצי XML לתצוגה
import android.view.LayoutInflater
// רכיבי תצוגה
import android.view.View
import android.view.ViewGroup
// קישוריות לתצוגה (ViewBinding)
import com.example.taskmasterfinalproject.databinding.FragmentAiCoachBottomSheetBinding
// מודל המשימה
import com.example.taskmasterfinalproject.model.Task
// התנהגות החלונית התחתונה
import com.google.android.material.bottomsheet.BottomSheetBehavior
// דיאלוג החלונית התחתונה
import com.google.android.material.bottomsheet.BottomSheetDialog
// מחלקת בסיס לחלונית תחתונה (Bottom Sheet)
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
// האצלת יצירת ViewModel
import androidx.fragment.app.viewModels

// חלונית תחתונה המציגה את עצות ה-AI עבור משימה
class AiCoachBottomSheet : BottomSheetDialogFragment() {

    // _binding – nullable כדי למנוע דליפות זיכרון (מתאפס ב-onDestroyView)
    private var _binding: FragmentAiCoachBottomSheetBinding? = null
    // גישה נוחה ל-Binding (!! כי משתמשים רק כשהתצוגה קיימת)
    private val binding get() = _binding!!
    // ViewModel שמנהל את הלוגיקה – by viewModels() = השמדה אוטומטית כשהפרגמנט נהרס
    private val viewModel: AiViewModel by viewModels()

    private var taskTitle: String = ""
    private var taskDesc: String = ""
    private var taskPriority: Int = 0

    // אובייקט נלווה (Companion Object) – מכיל קבועים וFactory Method.
    // קבועים אלו משמשים כמפתחות להעברת נתונים ב-Bundle (דפוס Arguments)
    companion object {
        const val TAG = "AiCoachBottomSheet"        // תגית לזיהוי ב-FragmentManager
        private const val ARG_TITLE = "arg_title"   // מפתח לכותרת המשימה
        private const val ARG_DESC = "arg_desc"     // מפתח לתיאור המשימה
        private const val ARG_PRIORITY = "arg_priority"  // מפתח לעדיפות
        private const val ARG_SUBTASKS = "arg_subtasks"  // מפתח לרשימת תת-משימות
        private const val ARG_TASK_ID = "arg_task_id"    // מפתח למזהה המשימה

        // יוצר מופע חדש של החלונית עם נתונים התחלתיים
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

    // אתחול ראשוני וקריאת ארגומנטים
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

    // טעינת הממשק הגרפי (Layout)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiCoachBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Callback חיצוני – מאפשר ל-Activity שפתח את החלונית לטפל בהוספת תת-משימות
    var onApplySubtasks: ((List<AiSubtask>) -> Unit)? = null

    // הגדרת לוגיקה ומאזינים לאחר יצירת התצוגה
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val prefs = com.example.taskmasterfinalproject.data.PreferencesManager(requireContext())

        // הגדרת ממשק המשתמש ומאזינים
        binding.btnClose.setOnClickListener { dismiss() }  // dismiss() – סגירת החלונית
        
        // כפתור יצירת עצה חדשה – מנקה את המטמון כדי לכפות תוכן חדש
        binding.btnRegenerate.setOnClickListener {
            // ניקוי המטמון כדי לאפשר עצה חדשה עם תתי-משימות מעודכנות
            if (taskId.isNotEmpty()) {
                prefs.saveAiAdvice(taskId, "")
            }
            viewModel.generateAdvice(taskTitle, taskDesc, taskPriority, subtaskList)
        }

        // כפתור יצירת תוכנית תת-משימות באמצעות AI
        binding.btnGenerateSubtasks.setOnClickListener {
            viewModel.generateSubtasksPlan(taskTitle, taskDesc, taskPriority)
        }

        // כפתור ביטול – מאפס את התוכנית שנוצרה
        binding.btnDiscard.setOnClickListener {
             viewModel.clearPlan()
        }

        // כפתור אישור – מחיל את תת-המשימות שנוצרו על המשימה המקורית
        binding.btnApply.setOnClickListener {
            viewModel.subtaskPlan.value?.let { plan ->
                onApplySubtasks?.invoke(plan.subtasks)
                com.google.android.material.snackbar.Snackbar.make(binding.root, "Subtasks created!", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
                dismiss() 
            }
        }

        // דפוס Observer – האזנה לשינויים ב-LiveData מתוך ה-ViewModel.
        // observe() מחובר ל-viewLifecycleOwner ולכן מתבטל אוטומטית כשהתצוגה נהרסת
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
                // לוגיקת שמירה למטמון (Caching): שומר תשובה אם היא "אמיתית" (לא הודעת טעינה)
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

        // לוגיקת אתחול: בדיקת מטמון לפני פנייה ל-AI (חוסך זמן ותעבורת רשת)
        if (viewModel.aiResponse.value.isNullOrBlank() && viewModel.subtaskPlan.value == null) {
            val cached = if (taskId.isNotEmpty()) prefs.getAiAdvice(taskId) else null
            if (!cached.isNullOrBlank()) {
                 viewModel.setCachedResponse(cached)  // טעינה מהמטמון
            } else {
                 viewModel.generateAdvice(taskTitle, taskDesc, taskPriority, subtaskList)  // פנייה ל-AI
            }
        }
    }
    
    // הגדרת גובה החלונית במצב מכווץ
    override fun onStart() {
        super.onStart()
        // Set peek height to ~1/6 screen (Compact)
        val dialog = dialog as? BottomSheetDialog
        dialog?.behavior?.let { behavior ->
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            behavior.peekHeight = (resources.displayMetrics.heightPixels / 6)
        }
    }

    // ניקוי הפניות למניעת דליפות זיכרון
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
