package com.energyict.protocolimpl.base;

import com.energyict.protocolimpl.utils.ProtocolTools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 5-okt-2010
 * Time: 16:53:17
 */
public abstract class AbstractProtocolProperties implements ProtocolProperties {

    private final Properties protocolProperties;

    public AbstractProtocolProperties(Properties properties) {
        this.protocolProperties = properties;
    }

    protected int getIntPropery(String propertyName, String defaultValue) {
        return Integer.parseInt(getStringValue(propertyName, defaultValue));
    }

    protected String getStringValue(String propertyName, String defaultValue) {
        return getProtocolProperties().getProperty(propertyName, defaultValue);
    }

    protected byte[] getByteValue(String propertyName, String defaultValue) {
        return ProtocolTools.getBytesFromHexString(getStringValue(propertyName, defaultValue), "");
    }

    public Properties getProtocolProperties() {
        return protocolProperties;
    }

    public void addProperties(Properties properties) {
        for (Object key : properties.keySet()) {
            if (key instanceof String) {
                String propertyName = (String) key;
                getProtocolProperties().setProperty(propertyName, properties.getProperty(propertyName));
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Method[] methods = getClass().getMethods();
        for (Method method : methods) {
            ProtocolProperty annotation = method.getAnnotation(ProtocolProperty.class);
            if (annotation != null) {
                Object obj = null;
                try {
                    obj = method.invoke(this, new Object[0]);
                    sb.append(method.getName().substring(3)).append(" = ").append(obj).append("\n");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (InvocationTargetException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
        return sb.toString();
    }

}
