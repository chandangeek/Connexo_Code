package com.energyict.mdc.engine.impl.events.registration;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.engine.events.EventRegistrationRequestInitiator;
import com.energyict.mdc.engine.model.ComServer;

/**
 * Provides the default implementation for the {@link EventRegistrationRequestInitiator} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (13:44)
 */
public class EventRegistrationRequestInitiatorImpl implements EventRegistrationRequestInitiator {

    @Override
    public String getRegistrationURL (String comServerName) throws BusinessException {
        ComServer comServer = ManagerFactory.getCurrent().getComServerFactory().findBySystemName(comServerName);
        if (comServer == null) {
            throw new BusinessException("ComServerXByNameDoesNotExist", "The Comserver by the name of {0} does not exist");
        }
        else {
            return this.getRegistrationURL(comServer);
        }
    }

    @Override
    public String getRegistrationURL (ComServer comServer) throws BusinessException {
        comServer.getEventRegistrationUriIfSupported();
        return comServer.getEventRegistrationUriIfSupported();
    }

}