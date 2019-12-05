/*
 * AbstractRowData.java
 *
 * Created on 25 september 2003, 13:49
 */

package com.energyict.mdc.engine.offline.gui.rows;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;


/**
 * @author Koen
 */
abstract public class AbstractRowData {

    private static final Log logger = LogFactory.getLog(AbstractRowData.class);

    protected abstract String[] getSelectedColumnProperties();
    protected abstract String[] getSelectedColumnTranslationKeys();

    protected abstract Object[] getSelectedColumnWidthObjects();

    private BeanInfo beanInfo = null;

    private String[] columnGetters = null;
    private String[] columnSetters = null;
    private String[] columnProperties = null;
    private String[] columnTranslationKeys = null;

    /**
     * Creates a new instance of AbstractRowData
     */
    public AbstractRowData() {
    }

    public String[] getColumnProperties() {
        if (columnProperties == null) {
            columnProperties = getSelectedColumnProperties();
            if (columnProperties == null) {
                columnProperties = getAllColumnProperties();
            }
        }
        return columnProperties;
    }

    public String[] getColumnTranslationKeys() {
        if (columnTranslationKeys == null) {
            columnTranslationKeys = getSelectedColumnTranslationKeys();
        }
        return columnTranslationKeys;
    }

    public String[] getColumnSetters() {
        if (columnSetters == null) {
            columnSetters = new String[getColumnProperties().length];
            PropertyDescriptor[] pds = getBeanInfo().getPropertyDescriptors();
            for (int t = 0; t < getColumnProperties().length; t++) {
                for (PropertyDescriptor pd : pds) {
                    if (getColumnProperties()[t].toLowerCase().compareTo(pd.getName().toLowerCase()) == 0) {
                        Method method = pd.getWriteMethod();
                        if (method == null) {
                            columnSetters[t] = null;
                        } else {
                            columnSetters[t] = method.getName();
                        }
                    }
                }
            }
        }
        return columnSetters;
    }

    public String[] getColumnGetters() {
        if (columnGetters == null) {
            int count = 0;
            columnGetters = new String[getColumnProperties().length];
            PropertyDescriptor[] pds = getBeanInfo().getPropertyDescriptors();
            for (int t = 0; t < getColumnProperties().length; t++) {
                for (PropertyDescriptor pd : pds) {
                    if (getColumnProperties()[t].toLowerCase().compareTo(pd.getName().toLowerCase()) == 0) {
                        Method method = pd.getReadMethod();
                        columnGetters[t] = method.getName();
                        count++;
                        break;
                    }
                }
            }
            if (count < getColumnProperties().length) {
                throw new RuntimeException("AbstractRowData, getColumnGetters, Error int ColumnProperties initialization, not all properties found in PropertyDescriptor array, program cannot continue...");
            }
        }
        return columnGetters;
    }

    public int getColumnCount() {
        return getColumnProperties().length;
    }

    public Class getColumnClass(int index) {
        try {
            return this.getClass().getMethod(getColumnGetters()[index], null).getReturnType();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Error in rowdata, method " + getColumnProperties()[index] + " does not exist, exit program!");
        }
    }

    public Class getColumnClass(String method) {
        try {
            return this.getClass().getMethod(method, null).getReturnType();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Error in rowdata, method " + method + " does not exist, exit program!");
        }
    }

    // ************************** private methods ****************************

    private String[] getAllColumnProperties() {
        PropertyDescriptor[] pds = getBeanInfo().getPropertyDescriptors();
        String[] allProperties = new String[pds.length];
        for (int i = 0; i < allProperties.length; i++) {
            allProperties[i] = pds[i].getName();
        }
        return allProperties;
    }

    private BeanInfo getBeanInfo() {
        try {
            if (beanInfo == null) {
                beanInfo = Introspector.getBeanInfo(this.getClass(), AbstractRowData.class);
            }
        } catch (IntrospectionException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("AbstractRowData, getBeanInfo, " + e.getMessage());
        }
        return beanInfo;
    }

    public Object getColumnWidthObject(int index) {
        if (getSelectedColumnWidthObjects() == null) {
            return null;
        } else {
            return getSelectedColumnWidthObjects()[index];
        }
    }
}