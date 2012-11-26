package com.energyict.genericprotocolimpl.common.pooling;

import com.energyict.cbo.SerialCommunicationSettings;
import com.energyict.mdw.shadow.RtuTypeShadow;

import java.util.*;

/**
 * Provides logical information about an {@link com.energyict.mdw.core.Device}
 */
public interface RtuFullProtocolShadow {


    String getName();

    void setName(String name);

    int getRtuId();

    void setRtuId(int id);

    String getModemInit();

    void setModemInit(String modemInit);

    String getPhoneNumber();

    void setPhoneNumber(String phoneNumber);

    String getDeviceId();

    void setDeviceId(String deviceId);

    String getPassword();

    void setPassword(String password);

    Date getRtuLastReading();

    void setRtuLastReading(Date lastReading);

    Date getRtuLastLogBook();

    void setRtuLastLogBook(Date lastLogBook);

    /**
     * @return all the Custom properties defined on the {@link com.energyict.mdw.core.Device}, and those defined on the used {@link com.energyict.mdw.core.CommunicationProtocol}
     */
    Properties getRtuProperties();

    void setRtuProperties(Properties properties);

    TimeZone getDeviceTimeZone();

    void setDeviceTimeZone(TimeZone deviceTimeZone);

    TimeZone getTimeZone();

    void setTimeZone(TimeZone timeZone);

    String getNodeAddress();

    void setNodeAddress(String nodeAddress);

    String getSerialNumber();

    void setSerialNumber(String serialNumber);

    String getPostDialCommand();

    void setPostDialCommand(String postDialCommand);

    String getDialHomeId();

    void setDialHomeId(String dialHomeId);

    int getRtuIntervalInSeconds();

    void setRtuIntervalInSeconds(int intervalInSeconds);

    boolean isOverruleCommunicationSettings();

    void setOverruleCommunicationSettings(boolean overrule);

    SerialCommunicationSettings getSerialCommunicationSettings();

    void setSerialCommunicationSettings(SerialCommunicationSettings serialCommunicationSettings);

    RtuTypeShadow getRtuTypeShadow();

    void setRtuTypeShadow(RtuTypeShadow rtuTypeShadow);

    List<ChannelFullProtocolShadow> getChannelFullProtocolShadow();

    void setChannelFullProtocolShadow(List<ChannelFullProtocolShadow> channelFullProtocolShadow);

    int getFolderId();

    void setFolderId(int folderId);
}
