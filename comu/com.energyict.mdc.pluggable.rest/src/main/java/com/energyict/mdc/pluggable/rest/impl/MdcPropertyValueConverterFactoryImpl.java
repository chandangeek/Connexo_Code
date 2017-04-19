package com.energyict.mdc.pluggable.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.pluggable.rest.MdcPropertyValueConverterFactory;
import com.energyict.mdc.pluggable.rest.impl.properties.CalendarPropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.ClockPropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.DatePropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.DeviceMessageFilePropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.Ean13PropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.Ean18PropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.FirmwareVersionPropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.HexStringPropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.LoadProfilePropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.LoadProfileTypePropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.LogbookPropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.ObisCodePropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.PasswordPropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.ReadingTypePropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.RegisterPropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.TimeDurationPropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.TimeOfDayPropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.TimeZoneInUsePropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.UsagePointPropertyValueConverter;

import org.osgi.service.component.annotations.Component;

import javax.ws.rs.core.Application;

/**
 * Copyrights EnergyICT
 * Date: 19/04/2017
 * Time: 15:22
 */
@Component(name = "com.energyict.mdc.pluggable.rest.propertyValueConvertorFactory", service = {MdcPropertyValueConverterFactory.class}, immediate = true)
public class MdcPropertyValueConverterFactoryImpl implements MdcPropertyValueConverterFactory {
    @Override
    public PropertyValueConverter getConverterFor(Class clazz) {
        switch (clazz.getSimpleName()){
            case "Calendar": return new CalendarPropertyValueConverter();
            case "DateAndTimeFactory": return new ClockPropertyValueConverter();
            case "Date": return new DatePropertyValueConverter();
            case "DeviceMessageFile": return new DeviceMessageFilePropertyValueConverter();
            case "Ean13": return new Ean13PropertyValueConverter();
            case "Ean18": return new Ean18PropertyValueConverter();
            case "BaseFirmwareVersion": return new FirmwareVersionPropertyValueConverter();
            case "HexString": return new HexStringPropertyValueConverter();
            case "LoadProfile": return new LoadProfilePropertyValueConverter();
            case "LoadProfileType": return new LoadProfileTypePropertyValueConverter();
            case "LogBook": return new LogbookPropertyValueConverter();
            case "ObisCode": return new ObisCodePropertyValueConverter();
            case "Password": return new PasswordPropertyValueConverter();
            case "ReadingType": return new ReadingTypePropertyValueConverter();
            case "Register": return new RegisterPropertyValueConverter();
            case "TimeDuration": return new TimeDurationPropertyValueConverter();
            case "TimeOfDay": return new TimeOfDayPropertyValueConverter();
            case "TimeZoneInUse": return new TimeZoneInUsePropertyValueConverter();
            case "UsagePoint": return new UsagePointPropertyValueConverter();
        }
        throw new UnsupportedOperationException("No converter found for class " + clazz.getSimpleName());
    }
}
