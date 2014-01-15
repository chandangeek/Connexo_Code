package com.energyict.mdc.protocol.api.device;

import com.energyict.mdc.common.ApplicationComponent;

import java.util.List;

/**
 * Defines the behavior of an {@link ApplicationComponent}
 * that is capable of finding {@link Register}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-08 (16:34)
 */
public interface RegisterFactory {

    public List<Register> findRegistersByDevice(Device device);

}