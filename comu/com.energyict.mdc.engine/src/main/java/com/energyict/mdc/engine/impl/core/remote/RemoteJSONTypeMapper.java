package com.energyict.mdc.engine.impl.core.remote;

import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.util.geo.SpatialCoordinates;
import com.energyict.cim.EndDeviceEventType;
import com.energyict.mdc.channel.serial.SerialPortConfiguration;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.TimeOfDay;
import com.energyict.mdc.common.ean.Ean13;
import com.energyict.mdc.common.ean.Ean18;
import com.energyict.mdc.engine.impl.ObjectMapperServiceImpl;
import com.energyict.mdc.engine.impl.OfflineDeviceForComTaskGroup;
import com.energyict.mdc.engine.impl.core.offline.ComJobExecutionModel;
import com.energyict.mdc.engine.impl.core.offline.DeviceComTaskWrapper;
import com.energyict.mdc.engine.impl.core.offline.DeviceMessageInformationWrapper;
import com.energyict.mdc.pluggable.PluggableClassType;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessageAttribute;
import com.energyict.mdc.tasks.OfflineProtocolTask;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.identifiers.*;
import com.energyict.mdc.upl.offline.*;
import com.energyict.mdc.upl.properties.HexString;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.jaas.spi.UserInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 1/09/14
 * Time: 15:59
 */
public class RemoteJSONTypeMapper implements ObjectMapperServiceImpl.JSONTypeMapper {
    private static final Log logger = LogFactory.getLog(RemoteJSONTypeMapper.class);
    private static final String ENERGYICT_PACKAGE_PREFIX = "com.energyict.";
    private static final String MARSHALLABLE_HASHMAP_ATTRIBUTE = "marshallableMap";

    private final static List<Class> interfacesWhoNeedNoConversion = new ArrayList<Class>() {{
        add(OfflineDevice.class);
        add(OfflineRegister.class);
        add(OfflineLoadProfile.class);
        add(OfflineLogBook.class);
        add(OfflineDeviceMessage.class);
        add(OfflineDeviceMessageAttribute.class);
        add(OfflineLogBookSpec.class);
        add(OfflineLoadProfileChannel.class);
        add(OfflineProtocolTask.class);

        // Identifiers
        add(DeviceIdentifier.class);
        add(RegisterIdentifier.class);
        add(LoadProfileIdentifier.class);
        add(LogBookIdentifier.class);
        add(MessageIdentifier.class);

        // Collected data objects
        add(CollectedData.class);
        add(RegisterValue.class);
        add(MeterReadingData.class);
        add(IntervalData.class);
        add(IntervalValue.class);
        add(DeviceMessageStatus.class);

        // MDW objects
        add(OfflineDeviceForComTaskGroup.class);
        add(TypedProperties.class);
        add(DeviceOfflineFlags.class);
        add(UserInfo.class);
        add(HexString.class);
        add(EndDeviceEventType.class);
        add(SpatialCoordinates.class);
        add(Ean13.class);
        add(Ean18.class);
        add(ComWindow.class);
        add(PluggableClassType.class);
        add(RelativeDate.class);
        add(ObisCode.class);
        add(SerialPortConfiguration.class);
        add(DeviceMessageInformationWrapper.class);
        add(TimeOfDay.class);
        add(ChannelInfo.class);
        add(MeterProtocolEvent.class);
        add(Issue.class);

        // Others
        add(ComJobExecutionModel.class);
        add(DeviceComTaskWrapper.class);

        add(ConnectionType.class);
    }};

    @Override
    public void convertAllClassNamesFor(Object objectJSON) throws JSONException, ClassNotFoundException {
        if (objectJSON instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) objectJSON;
            Iterator it = jsonObject.keys();
            while (it.hasNext()) {
                String key = (String) it.next();
                Object innerObject = jsonObject.get(key);
                if (key.equals(TYPE_ATTRIBUTE) && jsonObjectContainsValidClassName(jsonObject, key)) {
                    jsonObject.put(TYPE_ATTRIBUTE, classForName((String) innerObject).getName());
                } else if (key.equals(MARSHALLABLE_HASHMAP_ATTRIBUTE)) {
                    innerObject = convertAllClassNamesEncodedInHashMap((JSONObject) innerObject);
                    jsonObject.put(MARSHALLABLE_HASHMAP_ATTRIBUTE, innerObject);
                }
                if (innerObject instanceof JSONObject || innerObject instanceof JSONArray) {
                    convertAllClassNamesFor(innerObject);
                }
            }
        } else if (objectJSON instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) objectJSON;
            for (int i = 0; i < jsonArray.length(); i++) {
                Object innerObject = jsonArray.get(i);
                convertAllClassNamesFor(innerObject);
            }
        }
    }

    private JSONObject convertAllClassNamesEncodedInHashMap(JSONObject jsonObject) throws JSONException, ClassNotFoundException {
        JSONObject result = new JSONObject();
        Iterator keys = jsonObject.keys();
        while (keys.hasNext()) {
            String encodedKey = (String) keys.next();
            String encodedValue = (String) jsonObject.get(encodedKey);

            Object object;
            if (encodedKey.startsWith("[")) {
                object = new JSONArray(encodedKey);
                convertAllClassNamesFor(object);
            } else if (encodedKey.startsWith("{")) {
                object = new JSONObject(encodedKey);
                convertAllClassNamesFor(object);
            } else {
                object = encodedKey;
            }
            encodedKey = object.toString();

            if (encodedValue.startsWith("[")) {
                object = new JSONArray(encodedValue);
                convertAllClassNamesFor(object);
            } else if (encodedValue.startsWith("{")) {
                object = new JSONObject(encodedValue);
                convertAllClassNamesFor(object);
            } else {
                object = encodedValue;
            }
            encodedValue = object.toString();
            result.put(encodedKey, encodedValue);
        }
        return result;
    }

    private boolean jsonObjectContainsValidClassName(JSONObject jsonObject, String key) {
        String className = "com.energyict.mdc.engine.config.impl.";
        try {
            className = className + jsonObject.getString(key);
        } catch (JSONException e) {
            logger.info("jsonObjectContainsValidClassName failed for key=" + key);
            return false;
        }
        try {
            Class.forName(className);   //Test if we can make a class with the given name
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public Class classForName(String className) throws ClassNotFoundException {
        className = "com.energyict.mdc.engine.config.impl." + className;
        if (noConversionNeeded(className) ||
                !className.startsWith(ENERGYICT_PACKAGE_PREFIX)) { // Do conversion only for our own classes, leave all others untouched
            return Class.forName(className);
        } else {
            return Class.forName(className);
        }
    }

    protected boolean noConversionNeeded(String className) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(className);
        for (Class interfaceClass : interfacesWhoNeedNoConversion) {
            if (interfaceClass.isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }
}