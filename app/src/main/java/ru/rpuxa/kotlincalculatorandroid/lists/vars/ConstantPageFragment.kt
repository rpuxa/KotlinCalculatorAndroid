package ru.rpuxa.kotlincalculatorandroid.lists.vars

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.constant_sections.view.*
import kotlinx.android.synthetic.main.page_constants.view.*
import ru.rpuxa.kotlincalculatorandroid.R
import ru.rpuxa.kotlincalculatorandroid.activities.ConstantActivity
import ru.rpuxa.kotlincalculatorandroid.lists.PageFragment
import ru.rpuxa.kotlincalculatorandroid.parcer.parts.step1.ConstantSections

class ConstantPageFragment : PageFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.page_constants, container, false)
        view.list_constants.adapter = ConstantAdapter(activity.layoutInflater, (activity as ConstantActivity).print, page)
        view.toolbar_constants.title = ConstantSections.NAMES[page]

        val pager = container as ViewPager

        view.constant_sections.math.setOnClickListener {
            gotoPage(ConstantSections.MATHEMATICAL, pager)
        }

        view.constant_sections.phys.setOnClickListener {
            gotoPage(ConstantSections.PHYSICAL, pager)
        }

        view.constant_sections.left_const.setOnClickListener {
            pager.arrowScroll(View.FOCUS_LEFT)
        }

        view.constant_sections.right_const.setOnClickListener {
            pager.arrowScroll(View.FOCUS_RIGHT)
        }

        return view
    }

    companion object {
        fun create(page: Int): ConstantPageFragment {
            val fragment = ConstantPageFragment()
            val args = Bundle()
            args.putInt("page", page)
            fragment.arguments = args
            return fragment
        }
    }
}