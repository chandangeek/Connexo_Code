package com.energyict.mdc.common;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Copyrights EnergyICT
 * Date: 15/01/13
 * Time: 14:25
 */
public interface EventBus {

    AtomicReference<EventBus> instance =new AtomicReference<>();

    void signalEvent(BusinessEvent event) throws BusinessException, SQLException;

}