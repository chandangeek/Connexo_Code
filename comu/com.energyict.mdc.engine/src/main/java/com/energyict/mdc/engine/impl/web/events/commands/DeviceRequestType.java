package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.exceptions.CanNotFindForIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import java.util.Set;
import java.util.StringTokenizer;

/**
 * Provides an implementation for the {@link RequestType} interface
 * for {@link com.energyict.mdc.device.data.Device}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-15 (17:00)
 */
class DeviceRequestType extends IdBusinessObjectRequestType {

    private final DeviceService deviceService;

    DeviceRequestType(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    protected String getBusinessObjectTypeName() {
        return "device";
    }

    @Override
    protected Request newRequestAccording(String parameterString) throws BusinessObjectParseException {
        try{
            parameterString = parameterString.replaceAll("\\s*,\\s*", ",");
            //As the parameterString could not be parsed to a List of long,
            // We consider the parameterString being a comma separated list of MRID's
            StringTokenizer tokenizer = new StringTokenizer(parameterString, ",", false);
            String[] mrids = new String[tokenizer.countTokens()];
            int i= 0;
            while (tokenizer.hasMoreTokens()) {
                mrids[i++] = tokenizer.nextToken();
            }
            if (mrids.length == 0) {
                return this.newRequestForAll();
            }
            return new DeviceRequest(deviceService, mrids);
        }catch (CanNotFindForIdentifier e) {
            DeviceIdentifier identifier = (DeviceIdentifier) e.getMessageArguments()[0];
            throw new BusinessObjectParseException(identifier.toString() + " could not be found", e);
        }
    }

    @Override
    protected Request newRequestForAll() {
        return new AllDevicesRequest();
    }

    @Override
    protected Request newRequestFor(Set<Long> ids) {
        return new DeviceRequest(deviceService, ids);
    }

}