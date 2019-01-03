package ru.rpuxa.kotlincalculatorandroid.lists

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.View

abstract class PageFragment : Fragment() {

    var page = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        page = arguments!!.getInt("page")
    }

    fun gotoPage(pos: Int, pager: ViewPager) {
        val times = pos - page
        if (times > 0) {
            repeat(times) {
                pager.arrowScroll(View.FOCUS_RIGHT)
            }
        } else if (times < 0) {
            repeat(-times) {
                pager.arrowScroll(View.FOCUS_LEFT)
            }
        }
    }

}