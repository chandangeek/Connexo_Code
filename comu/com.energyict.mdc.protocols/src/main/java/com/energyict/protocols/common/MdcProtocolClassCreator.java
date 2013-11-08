package com.energyict.protocols.common;

import com.energyict.comserver.exceptions.CodingException;
import com.energyict.mdc.pluggable.InnerBundleClassCreator;
import com.energyict.mdw.core.PluggableClass;

/**
 * Copyrights EnergyICT
 * Date: 08/11/13
 * Time: 10:36
 */
public class MdcProtocolClassCreator implements InnerBundleClassCreator {

    @Override
    public Class createClassFromPluggable(PluggableClass pluggableClass) {
        return createClassFromJavaClassName(pluggableClass.getJavaClassName());
    }

    @Override
    public Class createClassFromJavaClassName(String javaClassName) {
        try {
            return Class.forName(javaClassName);
        } catch (ClassNotFoundException e) {
            throw CodingException.genericReflectionError(e, javaClassName);
        }
    }

}
