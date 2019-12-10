/*
 * FilePersist.java
 *
 * Created on 14 oktober 2003, 13:06
 *
 *
 */

package com.energyict.mdc.engine.offline.persist;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.engine.offline.core.Helpers;
import com.energyict.mdc.engine.offline.core.ObjectBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * @author Koen
 *         This abstract class extend java bean confoorm classes with file persisting functionality.
 *         When subclassing FilePersist, a default constructor for the subclass and an implementation for
 *         getDirectory() must be implemented.
 *         The subclass is persist with the classname as filename and extention .bin
 */
public abstract class FilePersistData extends FilePersist {

    private static final Log logger = LogFactory.getLog(FilePersistData.class);

    public void add() {
        doSave(true);
    }

    synchronized private void doSave(boolean append) {
        try {
            File file = new File(getFileName(this.getClass()));
            FileOutputStream fos = new FileOutputStream(file, append);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            PropertyDescriptor[] propertyDescriptors = Helpers.getBeanInfo(this).getPropertyDescriptors();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                Method setMethod = propertyDescriptor.getWriteMethod();
                if (setMethod != null) {
                    Object obj = propertyDescriptor.getReadMethod().invoke(this, null);
                    if (obj != null) {
                        bw.write(setMethod.getName());
                        bw.write("=");
                        bw.write(getObjectString(obj));
                        bw.write(";");
                    }
                }
            }
            bw.newLine();
            bw.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private String getObjectString(Object obj) throws IOException {
        Class cls = Helpers.translateClass(obj.getClass());
        if (cls.isAssignableFrom(Integer.class)) {
            return ((Integer) obj).toString();
        } else if (cls.isAssignableFrom(String.class)) {
            return (String) obj;
        } else if (cls.isAssignableFrom(Date.class)) {
            DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss SSS");
            return format.format((Date) obj);
        } else {
            return obj.toString();
        }
    }

    static public List load(Class cls) {
        List<Object> list = new ArrayList<>();
        try {
            FileInputStream fis = new FileInputStream(new File(getFileName(cls)));
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String str = null;
            while (true) {
                str = br.readLine();
                if (str != null) {
                    ObjectBuilder obb = new ObjectBuilder(str, cls);
                    list.add(obb.getObject());
                } else {
                    break;
                }
            }
            br.close();
        } catch (FileNotFoundException e) {
            // absorb
        } catch (IOException e) {
            throw new ApplicationException("Error while reading in task transaction file: " + e.getMessage(), e);
        }
        return list;
    }
}