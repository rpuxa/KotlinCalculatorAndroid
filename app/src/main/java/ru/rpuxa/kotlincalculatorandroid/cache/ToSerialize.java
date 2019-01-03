package ru.rpuxa.kotlincalculatorandroid.cache;

import java.io.Serializable;
import java.util.Map;

public class ToSerialize implements Serializable {
    public String name;
    public Map<String, Object> fields;

    public ToSerialize(String name, Map<String, Object> fields) {
        this.name = name;
        this.fields = fields;
    }

    private static long serialVersionUID = -8980506436121272101L;
}
