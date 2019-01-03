package ru.rpuxa.kotlincalculatorandroid.lists.func

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.item.view.*
import ru.rpuxa.kotlincalculatorandroid.R
import ru.rpuxa.kotlincalculatorandroid.parcer.Function

class FunctionsAdapter(private val inflater: LayoutInflater, val print: (String) -> Unit, val category: Int) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var view = convertView
        if (view == null)
            view = inflater.inflate(R.layout.item, parent, false)
        val func = getItem(position)
        val s = "${func.name}${func.argsString}"
        view!!.name.text = s
        view.description.text = func.description
        view.setOnClickListener {
            print(func.name)
        }

        return view
    }

    override fun getItem(position: Int) = Function.descriptionFunctions.filter{it.section == category}[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = Function.descriptionFunctions.filter{it.section == category}.size
}