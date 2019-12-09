/*
 * FilePersist.java
 *
 * Created on 15 oktober 2003, 14:33
 */

package com.energyict.mdc.engine.offline.persist;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.engine.offline.core.Helpers;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * @author Koen
 */
abstract public class FilePersist {

    abstract public String getDirectory();

    /**
     * Creates a new instance of FilePersist
     */
    public FilePersist() {
    }

    static Object getInstance(Class cls) {
        try {
            return Class.forName(cls.getName()).newInstance();
        } catch (InstantiationException e) {
            throw new ApplicationException("FilePersist, design error, default constructor missing in class " + cls.getName(), e);
        } catch (IllegalAccessException | ClassNotFoundException e) {
            throw new ApplicationException(e);
        }
    }

    /**
     * Getter for property fileName.
     *
     * @return Value of property fileName.
     */
    static public java.lang.String getFileName(Class cls) {
        try {
            String className = cls.getName().substring(cls.getPackage().getName().length() + 1);
            Method method = cls.getDeclaredMethod("getDirectory", null);
            String directory = (String) method.invoke(getInstance(cls), null);
            FilePersist.createDir(directory);

            if (cls.getSuperclass().isAssignableFrom(FilePersistData.class))
                return directory + "/" + className + ".dat";
            else
                throw new ApplicationException("FilePersist, design error, invalid superClass to base fileextention on (" + cls.getSuperclass() + "), correct first!");
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new ApplicationException(e);
        }

    }

    static public void createDir(String dir) {
        File directory;
        directory = new File(dir);
        if (!directory.exists()) directory.mkdirs();
    }

    static public void deleteFile(Class cls) {
        Helpers.delete(FilePersist.getFileName(cls));
    }

}
