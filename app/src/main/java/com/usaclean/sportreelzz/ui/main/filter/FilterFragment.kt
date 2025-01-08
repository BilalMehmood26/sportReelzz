package com.usaclean.sportreelzz.ui.main.filter

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.skydoves.powerspinner.IconSpinnerAdapter
import com.skydoves.powerspinner.IconSpinnerItem
import com.usaclean.sportreelzz.R
import com.usaclean.sportreelzz.databinding.FragmentFilterBinding
import com.usaclean.sportreelzz.model.Cities
import com.usaclean.sportreelzz.model.Filter
import io.paperdb.Paper
import org.intellij.lang.annotations.Language

class FilterFragment(private val callBack: (String, String, List<Filter>, List<Filter>) -> Unit) :
    DialogFragment() {

    private lateinit var fragmentContext: Context

    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!

    private var filteredLanguageList: ArrayList<Filter> = ArrayList()
    private var filteredSportsList: ArrayList<Filter> = ArrayList()

    private lateinit var filterAdapter: FilterAdapter
    private lateinit var sportsFilterAdapter: SportsFilterAdapter

    private var languageFilterList = arrayListOf(
        Filter(title = "English", image = R.drawable.ic_english),
        Filter(title = "Dutch", image = R.drawable.ic_dutch)
    )

    private var sportsFilterList = arrayListOf(
        Filter(title = "Football", image = R.drawable.ic_football),
        Filter(title = "Padel", image = R.drawable.ic_padel),
        Filter(title = "Fitness", image = R.drawable.ic_fitness),
        Filter(title = "Tennis", image = R.drawable.ic_tennis),
        Filter(title = "Basketball", image = R.drawable.ic_basketball),
        Filter(title = "Golf", image = R.drawable.ic_golf),
        Filter(title = "Field Hockey", image = R.drawable.ic_hockey),
        Filter(title = "Volleyball", image = R.drawable.ic_volleyball),
        Filter(title = "Badminton", image = R.drawable.ic_badminton),
        Filter(title = "Handball", image = R.drawable.ic_handball),
        Filter(title = "Athletics", image = R.drawable.ic_athletics),
        Filter(title = "Cycling", image = R.drawable.ic_cycling),
        Filter(title = "Rugby", image = R.drawable.ic_rugby),
        Filter(title = "Skiing/Snowboarding", image = R.drawable.ic_snowboarding),
        Filter(title = "Yoga/Pilates", image = R.drawable.ic_yoga_pilates)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFilterBinding.inflate(inflater)

        Paper.init(fragmentContext)
        binding.apply {

            subjectEt.setText(Paper.book().read("subject"))
            questionEditText.setText(Paper.book().read("question"))

            val sportsList: List<Filter> = Paper.book().read("sportsList", emptyList())!!
            val languageList: List<Filter> = Paper.book().read("languageList", emptyList())!!

            languageFilterList.forEach {main ->
                languageList.forEach {
                    if (main.title == it.title){
                        main.isSelected = it.isSelected
                    }
                }
            }

            filterAdapter = FilterAdapter(fragmentContext, languageFilterList) {
                filteredLanguageList.clear()
                filteredLanguageList.addAll(it)
            }

            val layoutManagerOne = FlexboxLayoutManager(context)
            languageRv.layoutManager = layoutManagerOne
            languageRv.adapter = filterAdapter

            sportsFilterList.forEach {main ->
                sportsList.forEach {
                    if (main.title == it.title){
                        main.isSelected = it.isSelected
                    }
                }
            }
            sportsFilterAdapter = SportsFilterAdapter(fragmentContext, sportsFilterList) {
                    Log.d("LOGGER", "Filer Sports List: $it")
                    filteredSportsList.clear()
                    filteredSportsList.addAll(it)
                }

            val layoutManager = FlexboxLayoutManager(context)
            sportsRv.layoutManager = layoutManager
            sportsRv.adapter = sportsFilterAdapter

            showResultBtn.setOnClickListener {
                Log.d("LOGGER", "Filer Sports List: $filteredSportsList")
                val subject = subjectEt.text.toString()
                val question = questionEditText.text.toString()

                Paper.book().write("subject", subject)
                Paper.book().write("question", question)
                Paper.book().write("languageList", filteredLanguageList)
                Paper.book().write("sportsList", filteredSportsList)

                callBack.invoke(subject, question, filteredSportsList, filteredLanguageList)
                dismiss()
            }

            backIv.setOnClickListener {
                dismiss()
            }

        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

}