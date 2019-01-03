package ru.rpuxa.kotlincalculatorandroid.lists.func

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import ru.rpuxa.kotlincalculatorandroid.parcer.FunctionSections

class FunctionPageAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return FunctionPageFragment.create(position)
    }

    override fun getCount() = FunctionSections.NAMES.size
}