/*
 * ObjectBuilder.java
 *
 * Created on 15 oktober 2003, 11:47
 */

package com.energyict.mdc.engine.offline.core;

import com.energyict.mdc.common.ApplicationException;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;


/*
 *  ObjectBuilder is a helperclass that creates an object of type instance.getClass() and
 *  invokes the setters using str.
 *  the format of str is setter=value;setter=value; ... \n
 *                       setter=value;setter=value; ... \n
 *  Each line reperesents an object.
 */
public class ObjectBuilder {

    String str;
    Object instance;
    PropertyDescriptor[] propertyDescriptors;

    public ObjectBuilder(String str, Class cls) {
        try {
            this.str = str;
            instance = Class.forName(cls.getName()).newInstance();
            propertyDescriptors = Helpers.getBeanInfo(instance).getPropertyDescriptors();
        } catch (InstantiationException e) {
            throw new ApplicationException("FilePersist, design error, default constructor missing in class " + cls.getName(), e);
        } catch (IllegalAccessException | ClassNotFoundException e) {
            throw new ApplicationException(e);
        }
    }


    private List getObjectDescriptors() {
        List<ObjectDescriptor> objectDescriptors = new ArrayList<>();
        StringTokenizer strtok = new StringTokenizer(str, ";"); // setter=value;setter=value;
        while (strtok.hasMoreTokens()) {
            String token = (String) strtok.nextElement(); // setter=value
            if ("".compareTo(token) != 0) {
                String[] strs = token.split("="); // strs[0] = setter, strs[1] = value
                Method method = getMethod(strs[0]);
                if (method != null) {
                    if (strs.length == 2)
                        objectDescriptors.add(new ObjectDescriptor(getMethod(strs[0]), strs[1]));
                    else
                        objectDescriptors.add(new ObjectDescriptor(getMethod(strs[0]), ""));
                }
            }
        }
        return objectDescriptors;
    }

    private Method getMethod(String str) {
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            Method method = propertyDescriptor.getWriteMethod();
            if (method != null) {
                if (method.getName().compareTo(str) == 0)
                    return method;
            }
        }
        return null;
    }

    public Object getObject() {
        List objectDescriptors = getObjectDescriptors();
        for (Object objectDescriptor : objectDescriptors) {
            ObjectDescriptor objDesc = (ObjectDescriptor) objectDescriptor;
            try {
                objDesc.getWriteMethod().invoke(instance, getArgs(objDesc.getWriteMethod().getParameterTypes()[0], objDesc.getValue()));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new ApplicationException(e);
            }
        }
        return instance;
    }

    private Object[] getArgs(Class c, String value) {
        Class cls = Helpers.translateClass(c);
        if (cls.isAssignableFrom(Integer.class)) {
            return new Object[]{new Integer(value)};
        } else if (cls.isAssignableFrom(Long.class)) {
            return new Object[]{new Long(value)};
        } else if (cls.isAssignableFrom(Byte.class)) {
            return new Object[]{new Byte(value)};
        } else if (cls.isAssignableFrom(Boolean.class)) {
            return new Object[]{new Boolean(value)};
        } else if (cls.isAssignableFrom(Short.class)) {
            return new Object[]{new Short(value)};
        } else if (cls.isAssignableFrom(String.class)) {
            return new Object[]{value};
        } else if (cls.isEnum()) {
            for (Object each : cls.getEnumConstants()) {
                if (each.toString().equals(value)) {
                    return new Object[]{each};
                }
            }
            throw new ApplicationException("ObjectBuilder.getArgs() - Argument '"+ value +"' is no value of the enum class '" + cls.getName() + "'. Correct first!");
        } else if (cls.isAssignableFrom(Date.class)) {
            DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss SSS");
            try {
                return new Object[]{format.parse(value)};
            } catch (ParseException e) {
                throw new ApplicationException(e);
            }
        } else
            throw new ApplicationException("ObjectBuilder, design error, getArgs, class " + cls.getName() + " not supported, correct first!");
    }

    public class ObjectDescriptor {
        Method writeMethod;
        String value;

        public ObjectDescriptor(Method writeMethod, String value) {
            this.writeMethod = writeMethod;
            this.value = value;
        }

        /**
         * Getter for property writeMethod.
         *
         * @return Value of property writeMethod.
         */
        public java.lang.reflect.Method getWriteMethod() {
            return writeMethod;
        }

        /**
         * Getter for property value.
         *
         * @return Value of property value.
         */
        public java.lang.String getValue() {
            return value;
        }
    }
}