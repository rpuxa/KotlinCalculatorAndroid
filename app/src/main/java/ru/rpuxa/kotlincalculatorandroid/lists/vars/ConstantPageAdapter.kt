package ru.rpuxa.kotlincalculatorandroid.lists.vars

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import ru.rpuxa.kotlincalculatorandroid.parcer.parts.step1.ConstantSections

class ConstantPageAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return ConstantPageFragment.create(position)
    }

    override fun getCount() = ConstantSections.NAMES.size
}