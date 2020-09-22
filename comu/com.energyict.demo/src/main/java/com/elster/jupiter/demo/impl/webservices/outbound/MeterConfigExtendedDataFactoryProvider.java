package com.elster.jupiter.demo.impl.webservices.outbound;

import com.energyict.mdc.cim.webservices.outbound.soap.MeterConfigExtendedDataFactory;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.upl.TypedProperties;

import ch.iec.tc57._2011.meterconfig.MeterConfig;
import com.elster.connexo._2017.schema.customattributes.Attribute;
import com.elster.connexo._2017.schema.customattributes.CustomAttributeSet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component(name = "com.elster.jupiter.demo.impl.webservices.outbound.meterconfig.extendeddata.provider",
        service = {MeterConfigExtendedDataFactory.class},
        immediate = true,
        property = {"name=" + MeterConfigExtendedDataFactory.NAME})
public class MeterConfigExtendedDataFactoryProvider implements MeterConfigExtendedDataFactory {

    private final List<MdcPropertyUtils> mdcPropertyUtils = new ArrayList<>();

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addMdcPropertyUtils(MdcPropertyUtils mdcPropertyUtils) {
        this.mdcPropertyUtils.add(mdcPropertyUtils);
    }

    public void removeMdcPropertyUtils(MdcPropertyUtils mdcPropertyUtils) {
        this.mdcPropertyUtils.remove(mdcPropertyUtils);
    }

    public List<MdcPropertyUtils> getMdcPropertyUtils() {
        return Collections.unmodifiableList(this.mdcPropertyUtils);
    }

    @Override
    public MeterConfig extendData(Collection<Device> fromDevices, MeterConfig toMeterConfig) {
        toMeterConfig.getMeter().forEach(meter -> {
            fromDevices.stream()
                    .filter(fromDevice -> fromDevice.getmRID().equals(meter.getMRID()))
                    .findFirst()
                    .ifPresent(device -> {
                        TypedProperties deviceProperties = device.getDeviceProtocolProperties();
                        device.getDeviceType()
                                .getDeviceProtocolPluggableClass()
                                .ifPresent(deviceProtocolPluggableClass -> {
                                    getMdcPropertyUtils().stream().findAny().ifPresent(utils -> {
                                        utils.convertPropertySpecsToPropertyInfos(deviceProtocolPluggableClass.getDeviceProtocol()
                                                .getPropertySpecs(), deviceProperties, device)
                                                .stream()
                                                .filter(propertyInfo -> propertyInfo.key.equals("Manufacturer"))
                                                .findAny()
                                                .ifPresent(property -> {
                                                    Attribute attribute = new Attribute();
                                                    attribute.setName("Manufacturer");
                                                    attribute.setValue(property.getPropertyValueInfo().value == null
                                                            ? (String) property.getPropertyValueInfo().defaultValue
                                                            : (String) property.getPropertyValueInfo().value);
                                                    CustomAttributeSet customAttributeSet = new CustomAttributeSet();
                                                    customAttributeSet.getAttribute().add(attribute);
                                                    meter.getMeterCustomAttributeSet().add(customAttributeSet);
                                                });
                                    });
                                });
                    });
        });
        return toMeterConfig;
    }
}