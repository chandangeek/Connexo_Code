package com.energyict.mdc.protocol.api.device;

import com.energyict.mdc.common.ApplicationComponent;
import com.energyict.mdc.common.ObisCode;

/**
 * Defines the behavior of an {@link ApplicationComponent}
 * that is capable of finding {@link BaseLogBook}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-08 (16:34)
 */
public interface LogBookFactory {

    /**
     * This is the <i>generic</i> ObisCode that will be used for migrating <i>old</i> devices.
     */
    public static final ObisCode GENERIC_LOGBOOK_TYPE_OBISCODE = ObisCode.fromString("0.0.99.98.0.255");

    public BaseLogBook findLogBooksByDeviceAndDeviceObisCode(BaseDevice device, ObisCode obisCode);

    public BaseLogBook findGenericLogBook(BaseDevice device);

    public BaseLogBook findLogBook(int logBookId);
}