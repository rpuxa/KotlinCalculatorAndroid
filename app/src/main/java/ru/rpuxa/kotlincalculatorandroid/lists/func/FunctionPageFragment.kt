package ru.rpuxa.kotlincalculatorandroid.lists.func

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.func_sections.view.*
import kotlinx.android.synthetic.main.page_functions.view.*
import ru.rpuxa.kotlincalculatorandroid.R
import ru.rpuxa.kotlincalculatorandroid.activities.FunctionsActivity
import ru.rpuxa.kotlincalculatorandroid.lists.PageFragment
import ru.rpuxa.kotlincalculatorandroid.parcer.FunctionSections

class FunctionPageFragment : PageFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.page_functions, container, false)
        view.list_functions.adapter = FunctionsAdapter(activity.layoutInflater, (activity as FunctionsActivity).print, page)
        view.toolbar_functions.title = FunctionSections.NAMES[page]
        val pager = container as ViewPager
        view.func_sections.radicals.setOnClickListener {
            gotoPage(FunctionSections.RADICAL_LOGARITHM, pager)
        }

        view.func_sections.trigonometry.setOnClickListener {
            gotoPage(FunctionSections.TRIGONOMETRY, pager)
        }

        view.func_sections.diffs.setOnClickListener {
            gotoPage(FunctionSections.DIFFERENTIAL, pager)
        }

        view.func_sections.hyperbolic.setOnClickListener {
            gotoPage(FunctionSections.HYPERBOLIC, pager)
        }

        view.func_sections.other_func.setOnClickListener {
            gotoPage(FunctionSections.OTHER, pager)
        }

        view.func_sections.left_func.setOnClickListener {
            pager.arrowScroll(View.FOCUS_LEFT)
        }

        view.func_sections.right_func.setOnClickListener {
            pager.arrowScroll(View.FOCUS_RIGHT)
        }

        return view
    }

    companion object {
        fun create(page: Int): FunctionPageFragment {
            val fragment = FunctionPageFragment()
            val args = Bundle()
            args.putInt("page", page)
            fragment.arguments = args
            return fragment
        }
    }
}