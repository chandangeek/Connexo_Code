package com.energyict.mdc.engine.impl.core.offline.adapters;

import com.elster.jupiter.metering.readings.MeterReading;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapDeviceIdentifierMeterReadingAdapter extends XmlAdapter<MapDeviceIdentifierMeterReadingAdapter.AdaptedMap, Map<DeviceIdentifier, MeterReading>> {

    public static class AdaptedMap {
        public List<DeviceIdentifierMeterReadingWrapper> wrappers = new ArrayList<DeviceIdentifierMeterReadingWrapper>();
    }


    @Override
    public Map<DeviceIdentifier, MeterReading> unmarshal(AdaptedMap adaptedMap) throws Exception {
        Map<DeviceIdentifier, MeterReading> map = new HashMap<DeviceIdentifier, MeterReading>();
        for(DeviceIdentifierMeterReadingWrapper entry : adaptedMap.wrappers) {
            map.put(entry.getDeviceIdentifier(), entry.getMeterReading());
        }
        return map;
    }

    @Override
    public AdaptedMap marshal(Map<DeviceIdentifier, MeterReading> map) throws Exception {
        AdaptedMap adaptedMap = new AdaptedMap();
        for(Map.Entry<DeviceIdentifier, MeterReading> mapEntry : map.entrySet()) {
            DeviceIdentifierMeterReadingWrapper entry = new DeviceIdentifierMeterReadingWrapper();
            entry.setDeviceIdentifier(mapEntry.getKey());
            entry.setMeterReading(mapEntry.getValue());
            adaptedMap.wrappers.add(entry);
        }
        return adaptedMap;
    }

}

