package ru.rpuxa.kotlincalculatorandroid.cache;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public interface SuperSerializable {

    default ToSerialize serializable() {
        try {
            Class clazz = getClass();
            String name = clazz.getName();
            Map<String, Object> fields = new HashMap<>(clazz.getDeclaredFields().length);
            for (Field field : clazz.getDeclaredFields()) {
                if (!field.getDeclaringClass().getName().equals(getClass().getName()))
                    continue;
                field.setAccessible(true);
                int mod = field.getModifiers();
                final Object o = field.get(this);
                if (Modifier.isTransient(mod) || Modifier.isStatic(mod) || o == null)
                    continue;
                Class fieldClass = field.getType();
                if (Collection.class.isAssignableFrom(fieldClass)) {
                    Collection collection = (Collection) field.get(this);
                    if (!collection.isEmpty()) {
                        Object firstObject = collection.toArray(new Object[0])[0];
                        if (firstObject instanceof Serializable || firstObject.getClass().isPrimitive()) {
                            fields.put(field.getName(), collection);
                        } else if (firstObject instanceof SuperSerializable) {
                            Collection newCollection = (Collection) fieldClass.newInstance();
                            for (Object obj : collection)
                                newCollection.add(((SuperSerializable) obj).serializable());
                            fields.put(field.getName(), newCollection);
                        } else
                            throw new NoSerializableException("Class: " + fieldClass.getName() + " Field: " + field.getName());
                    } else {
                        fields.put(field.getName(), collection);
                    }
                } else if (Serializable.class.isAssignableFrom(fieldClass) || fieldClass.isPrimitive())
                    fields.put(field.getName(), field.get(this));
                else if (SuperSerializable.class.isAssignableFrom(fieldClass)) {
                    final SuperSerializable superSerializable = (SuperSerializable) field.get(this);
                    final ToSerialize serializable = superSerializable.serializable();
                    fields.put(field.getName(), serializable);
                } else
                    throw new NoSerializableException("Class: " + fieldClass.getName() + " Field: " + field.getName());
            }
            return new ToSerialize(name, fields);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
