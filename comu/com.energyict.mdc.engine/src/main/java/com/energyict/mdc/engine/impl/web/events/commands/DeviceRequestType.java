package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.device.data.exceptions.CanNotFindForIdentifier;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import java.util.*;

/**
 * Provides an implementation for the {@link RequestType} interface
 * for {@link com.energyict.mdc.device.data.Device}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-15 (17:00)
 */
public class DeviceRequestType extends IdBusinessObjectRequestType {

    private final IdentificationService identificationService;

    public DeviceRequestType(IdentificationService identificationService) {
        this.identificationService = identificationService;
    }

    @Override
    protected String getBusinessObjectTypeName() {
        return "device";
    }

    @Override
    protected Request newRequestAccording(String parameterString) throws BusinessObjectIdParseException{
        try{
            return super.newRequestAccording(parameterString);
        }catch (BusinessObjectIdParseException e){
            //As the parameterString could not be parsed to a List of long,
            // We consider the parameterString being a comma separated list of MRID's
            StringTokenizer tokenizer = new StringTokenizer(parameterString, ", ", false);
            String[] mrids = new String[tokenizer.countTokens()];
            int i= 0;
            while (tokenizer.hasMoreTokens()) {
                mrids[i++] = tokenizer.nextToken();
            }
            return new DeviceRequest(identificationService, mrids);
        }catch (CanNotFindForIdentifier e) {
            throw new BusinessObjectIdParseException(parameterString, this.getBusinessObjectTypeName(), e);
        }
    }

    @Override
    protected Request newRequestForAll() {
        return new AllDevicesRequest();
    }

    @Override
    protected Request newRequestFor(Set<Long> ids) {
        return new DeviceRequest(identificationService, ids);
    }

}