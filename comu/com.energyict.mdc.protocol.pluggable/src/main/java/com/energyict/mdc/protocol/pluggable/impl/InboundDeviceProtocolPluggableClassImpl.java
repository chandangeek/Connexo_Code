package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;

import javax.inject.Inject;
import java.util.List;

/**
 * Defines a PluggableClass based on a {@link InboundDeviceProtocol}.
 * <p/>
 * Copyrights EnergyICT
 * Date: 5/11/12
 * Time: 10:15
 */
public final class InboundDeviceProtocolPluggableClassImpl extends PluggableClassWrapper<InboundDeviceProtocol> implements InboundDeviceProtocolPluggableClass {

    @Inject
    private InboundDeviceProtocolService inboundDeviceProtocolService;

    static InboundDeviceProtocolPluggableClassImpl from (DataModel dataModel, PluggableClass pluggableClass) {
        return dataModel.getInstance(InboundDeviceProtocolPluggableClassImpl.class).initializeFrom(pluggableClass);
    }

    InboundDeviceProtocolPluggableClassImpl initializeFrom (PluggableClass pluggableClass) {
        this.setPluggableClass(pluggableClass);
        return this;
    }

    @Override
    protected Discriminator discriminator() {
        return Discriminator.DEVICEPROTOCOL;
    }

    @Override
    protected void validateLicense() {
        // Nothing to validate for inbound device protocols
    }

    @Override
    protected InboundDeviceProtocol newInstance(PluggableClass pluggableClass) {
        return this.inboundDeviceProtocolService.createInboundDeviceProtocolFor(pluggableClass);
    }

    @Override
    public TypedProperties getProperties(List<PropertySpec> propertySpecs) {
        return super.getProperties(propertySpecs);
    }

    @Override
    public InboundDeviceProtocol getInboundDeviceProtocol () {
        InboundDeviceProtocol inboundDeviceProtocol = this.newInstance();
        inboundDeviceProtocol.copyProperties(this.getProperties(inboundDeviceProtocol.getPropertySpecs()));
        return inboundDeviceProtocol;
    }

    @Override
    public void notifyDelete() {
//        List<InboundComPortPool> inboundComPortPools = ManagerFactory.getCurrent().getComPortPoolFactory().findByDiscoveryProtocol(this);
//        if (!inboundComPortPools.isEmpty()) {
//            throw new BusinessException(
//                    "discoveryProtocolXIsStillUsedByInboundComPortPoolsY",
//                    "The discovery protocol pluggable class {0} is still in use by the following inbound com port pools {1}",
//                    this.getName(),
//                    this.toSeparatedList(inboundComPortPools));
//        }
        // Todo: throw event that will allow the ComPortPool factory to check if this protocol is still used or not
        //       until then, this method is marked as unsupported
        throw new UnsupportedOperationException("InboundDeviceProtocolPluggableClassImpl#notifyDelete");
    }

}