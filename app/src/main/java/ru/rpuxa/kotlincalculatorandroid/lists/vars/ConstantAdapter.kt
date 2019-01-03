package ru.rpuxa.kotlincalculatorandroid.lists.vars

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.item.view.*
import ru.rpuxa.kotlincalculatorandroid.R
import ru.rpuxa.kotlincalculatorandroid.parcer.parts.step1.Constant
import java.math.RoundingMode

class ConstantAdapter(private val inflater: LayoutInflater, val print: (String) -> Unit, val category: Int) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if (view == null)
            view = inflater.inflate(R.layout.item, parent, false)
        val c = Constant.constants.filter { it.sections == category }[position]
        val s = "${c.name} = ${c.value.setScale(6, RoundingMode.HALF_EVEN).toDouble()}"
        view!!.name.text = s
        view.description.text = c.description

        view.setOnClickListener {
            print(c.name)
        }

        return view
    }


    override fun getItem(position: Int) = Constant.constants.filter { it.sections == category }[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = Constant.constants.filter { it.sections == category }.size


}