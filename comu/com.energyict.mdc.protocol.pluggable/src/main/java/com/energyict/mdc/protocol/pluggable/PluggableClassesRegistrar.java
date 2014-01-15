package com.energyict.mdc.protocol.pluggable;

import com.energyict.mdc.common.license.License;

/**
 * Models the behavior of a component that will register
 * the out of the box device protocols as DeviceProtocolPluggableClasses
 * based on the information found in the License provided @ startup time.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-10 (17:26)
 */
public interface PluggableClassesRegistrar {

    public void start (License license);

}