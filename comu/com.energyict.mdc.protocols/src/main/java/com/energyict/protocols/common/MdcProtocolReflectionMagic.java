package com.energyict.protocols.common;

import com.energyict.comserver.exceptions.CodingException;
import com.energyict.mdc.pluggable.BundleReflectionMagic;
import com.energyict.mdw.core.PluggableClass;

/**
 * Copyrights EnergyICT
 * Date: 08/11/13
 * Time: 10:36
 */
public class MdcProtocolReflectionMagic implements BundleReflectionMagic {

    @Override
    public Class createClassFromPluggable(PluggableClass pluggableClass) {
        try {
            return Class.forName(pluggableClass.getJavaClassName());
        } catch (ClassNotFoundException e) {
            throw CodingException.genericReflectionError(e, pluggableClass.getClass());
        }
    }
}
