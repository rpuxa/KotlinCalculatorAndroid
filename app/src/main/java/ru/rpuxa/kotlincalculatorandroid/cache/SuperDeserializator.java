package ru.rpuxa.kotlincalculatorandroid.cache;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Objects;

public class SuperDeserializator {

    public static Object deserialize(File files, String fileName) {
        ToSerialize serialize = (ToSerialize) SerializeKt.load(files, fileName);
        if (serialize == null)
            return null;
        return deserialize(serialize);
    }

    public static Object deserialize(ToSerialize serialize) {
        try {
            Object obj = Class.forName(serialize.name).newInstance();
            Field[] fields = obj.getClass().getDeclaredFields();
            for (String field : serialize.fields.keySet()) {
                for (Field objField : fields)
                    if (Objects.equals(objField.getName(), field)) {
                        objField.setAccessible(true);
                        try {
                            Object f = serialize.fields.get(field);
                            if (f instanceof ToSerialize)
                                objField.set(obj, deserialize((ToSerialize) f));
                            else if (f instanceof Collection) {
                                Collection collection = (Collection) f;
                                if (!collection.isEmpty()) {
                                    Object firstObject = collection.toArray(new Object[0])[0];
                                    if (firstObject instanceof ToSerialize) {
                                        Collection newCollection = (Collection) f.getClass().newInstance();
                                        for (Object o : collection) {
                                            newCollection.add(deserialize((ToSerialize) o));
                                        }
                                        objField.set(obj, newCollection);
                                    } else if (firstObject instanceof Serializable || firstObject.getClass().isPrimitive()) {
                                        objField.set(obj, f);
                                    } else
                                        throw new NoSerializableException("Class: " + f.getClass().getName() + " Field: " + f.getClass().getName());
                                }
                            } else if (f instanceof Serializable || f.getClass().isPrimitive())
                                objField.set(obj, f);
                        } catch (IllegalArgumentException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
            }
            return obj;
        } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
}
