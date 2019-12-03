package com.energyict.mdc.engine.offline.gui.models;

import com.energyict.mdc.engine.offline.core.Utils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * User: gde
 * Date: 5/01/12
 */
public class DateDependantAspectTableModel<T> extends AspectTableModel<T> {

    private Class<T> beanType;
    private Date viewDate;
    private Map<String, String> displayNames;

    public DateDependantAspectTableModel(List<T> models, List<String> aspects, Date viewDate, Map<String, String> displayNames) {
        super(models);
        beanType = models.isEmpty() ? null : (Class<T>) models.get(0).getClass();
        setAspects(aspects);
        this.viewDate = viewDate;
        this.displayNames = displayNames;
    }

    public void setAspects(List<String> aspects) {
        if (beanType == null) {
            return;
        }
        for (String each : aspects) {
            PropertyDescriptor descriptor;
            try {
                descriptor = new PropertyDescriptor(each, beanType);
            } catch (IntrospectionException ex) {
                String getter = "get" + Utils.capitalize(each);
                // first check if there is a getter with a date attribute
                try {
                    Class paraTypes[] = new Class[1];
                    paraTypes[0] = Date.class;
                    Method getMethod = beanType.getMethod(getter, paraTypes);
                    descriptor = new PropertyDescriptor(each, null, null);
                } catch (NoSuchMethodException | IntrospectionException e) {
                    try {
                        descriptor = new PropertyDescriptor(each, beanType, getter, null);
                    } catch (IntrospectionException ex2) {
                        getter = "is" + Utils.capitalize(each);
                        try {
                            descriptor = new PropertyDescriptor(each, beanType, getter, null);
                        } catch (IntrospectionException ex3) {
                            getter = "get";
                            try {
                                // To be safe, first check if the beanType has a method get(String aspectName, Date viewDate)
                                Class paraTypes[] = new Class[2];
                                paraTypes[0] = String.class;
                                paraTypes[1] = Date.class;
                                Method getMethod;
                                try {
                                    getMethod = beanType.getMethod(getter, paraTypes);
                                    descriptor = new PropertyDescriptor(each, null, null);
                                } catch (NoSuchMethodException x) {
                                    descriptor = null;
                                }
                            } catch (IntrospectionException ex4) {
                                descriptor = null;
                            }
                        }
                    }
                }
            }
            if (descriptor != null) {
                super.addDescriptor(descriptor);
            }
        }
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        PropertyDescriptor descriptor = getDescriptor(columnIndex);
        if (descriptor.getReadMethod() != null) {
            return super.getColumnClass(columnIndex);
        } else {
            Method methodWithDateParameter = getMethodWithDateParameter(descriptor.getName());
            if (methodWithDateParameter != null) {
                return methodWithDateParameter.getReturnType();
            } else {
                return Object.class; // case get(String aspectName, Date viewDate)
            }
        }
    }

    private Method getMethodWithDateParameter(String aspectName) {
        try {
            return beanType.getMethod("get" + Utils.capitalize(aspectName), Date.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (getShowSequenceNumbers() && columnIndex == 0) {
            return rowIndex + 1;
        }
        T model = getModels().get(rowIndex);
        if (model == null) {
            return null;
        }
        Method getter = getDescriptor(columnIndex).getReadMethod();
        if (getter == null) {
            // find the method that takes a Date parameter, given the method name
            String aspectName = getDescriptor(columnIndex).getName();
            Method methodWithDateParameter = getMethodWithDateParameter(aspectName);
            if (methodWithDateParameter != null) {
                try {
                    return methodWithDateParameter.invoke(model, viewDate);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                // find the method get(aspectName, Date)
                Class paraTypes[] = new Class[2];
                paraTypes[0] = String.class;
                paraTypes[1] = Date.class;
                try {
                    getter = model.getClass().getMethod("get", paraTypes);
                    try {
                        return getter.invoke(model, new Object[]{aspectName, viewDate});
                    } catch (Exception x) {
                        throw new RuntimeException(x);
                    }
                } catch (NoSuchMethodException x) {
                    return null;
                }
            }

        } else {
            // invoke the found getter
            try {
                return getter.invoke(model, null);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }


    }

    @Override
    public String getColumnName(int index) {
        PropertyDescriptor descriptor = getDescriptor(index);
        if (descriptor.getReadMethod() != null) {
            return super.getColumnName(index);
        }
        // case get(String aspectName, Date viewDate)
        if (displayNames.containsKey(descriptor.getName())) {
            return displayNames.get(descriptor.getName());
        }
        return translator().getTranslation(descriptor.getName(), missingResourcesFlag);
    }
}
