/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ToStringBuilder.java
 *
 * Created on 10 februari 2006, 16:09
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.base;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 *
 * @author koen
 */
public class ToStringBuilder {


    /** Creates a new instance of ToStringBuilder */
    private ToStringBuilder() {
    }


    public static String print(Object obj) {
        StringBuffer strBuff = new StringBuffer();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            String className=obj.getClass().getName();
            int index = className.lastIndexOf('.')+1;
            strBuff.append(className.substring(index)+":\n");
            PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
            for(int i=0;i<pds.length;i++) {
                Method method = pds[i].getReadMethod();
                if (pds[i].getName().compareTo("class")!=0) {
                    try {
                        strBuff.append("    "+pds[i].getName()+"="+method.invoke(obj,null)+"\n");
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch(IntrospectionException e) {
            e.printStackTrace();
        }
        return strBuff.toString();
    }


    public static String genCode(Object obj) {
        StringBuilder strBuff = new StringBuilder();
        try {
            strBuff.append("    public String toString() {\n");
            strBuff.append("        // Generated code by ToStringBuilder\n");
            strBuff.append("        StringBuffer strBuff = new StringBuffer();\n");
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            String className=obj.getClass().getName();
            int index = className.lastIndexOf('.')+1;
            strBuff.append("        strBuff.append(\"").append(className.substring(index)).append(":\\n\");\n");
            PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
            for(int i=0;i<pds.length;i++) {
                Method method = pds[i].getReadMethod();
                if (pds[i].getName().compareTo("class")!=0) {

                    if (method.getDeclaringClass()==obj.getClass()) {
                        try {
                            if (pds[i].getPropertyType().isArray()) {
                                strBuff.append("        for (int i=0;i<").append(method.getName()).append("().length;i++) {\n");
                                strBuff.append("            strBuff.append(\"       ").append(pds[i].getName()).append("[\"+i+\"]=\"+").append(method.getName()).append("()[i]+\"\\n\");\n");
                                strBuff.append("        }\n");
                            }
                            else {
                                strBuff.append("        strBuff.append(\"   ").append(pds[i].getName()).append("=\"+").append(method.getName()).append("()+\"\\n\");\n");
                            }
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            strBuff.append("        return strBuff.toString();\n");
            strBuff.append("    }\n");
        } catch(IntrospectionException e) {
            e.printStackTrace();
        }
        return strBuff.toString();
    }
}
