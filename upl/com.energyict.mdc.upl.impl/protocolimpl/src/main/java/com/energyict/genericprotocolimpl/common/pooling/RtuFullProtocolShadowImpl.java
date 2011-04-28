package com.energyict.genericprotocolimpl.common.pooling;

import com.energyict.cbo.SerialCommunicationSettings;
import com.energyict.mdw.shadow.RtuTypeShadow;

import java.util.*;

/**
 * Straightforward implementation of the <CODE>RtuFullProtocolShadow</CODE> interface
 */
public class RtuFullProtocolShadowImpl implements RtuFullProtocolShadow{

    private String name;
    private int rtuId;
    private int folderId;
    private String modemInit;
    private String phoneNumber;
    private String deviceId;
    private String password;
    private Date rtuLastReading;
    private Date rtuLastLogBook;
    private Properties rtuProperties;
    private TimeZone deviceTimeZone;
    private TimeZone timeZone;
    private String nodeAddress;
    private String serialNumber;
    private String postDialCommand;
    private String dialHomeId;
    private int rtuIntervalInSeconds;
    private boolean overruleCommunicationSettings;
    private SerialCommunicationSettings serialCommunicationSettings;
    private RtuTypeShadow rtuTypeShadow;
    private List<ChannelFullProtocolShadow> channelFullProtocolShadow;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getRtuId() {
        return rtuId;
    }

    public void setRtuId(final int rtuId) {
        this.rtuId = rtuId;
    }

    public String getModemInit() {
        return modemInit;
    }

    public void setModemInit(final String modemInit) {
        this.modemInit = modemInit;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(final String deviceId) {
        this.deviceId = deviceId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public Date getRtuLastReading() {
        return rtuLastReading;
    }

    public void setRtuLastReading(final Date rtuLastReading) {
        this.rtuLastReading = rtuLastReading;
    }

    public Date getRtuLastLogBook() {
        return rtuLastLogBook;
    }

    public void setRtuLastLogBook(final Date rtuLastLogBook) {
        this.rtuLastLogBook = rtuLastLogBook;
    }

    public Properties getRtuProperties() {
        return rtuProperties;
    }

    public void setRtuProperties(final Properties rtuProperties) {
        this.rtuProperties = rtuProperties;
    }

    public TimeZone getDeviceTimeZone() {
        return deviceTimeZone;
    }

    public void setDeviceTimeZone(final TimeZone deviceTimeZone) {
        this.deviceTimeZone = deviceTimeZone;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(final TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public String getNodeAddress() {
        return nodeAddress;
    }

    public void setNodeAddress(final String nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(final String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getPostDialCommand() {
        return postDialCommand;
    }

    public void setPostDialCommand(final String postDialCommand) {
        this.postDialCommand = postDialCommand;
    }

    public String getDialHomeId() {
        return dialHomeId;
    }

    public void setDialHomeId(final String dialHomeId) {
        this.dialHomeId = dialHomeId;
    }

    public int getRtuIntervalInSeconds() {
        return rtuIntervalInSeconds;
    }

    public void setRtuIntervalInSeconds(final int rtuIntervalInSeconds) {
        this.rtuIntervalInSeconds = rtuIntervalInSeconds;
    }

    public boolean isOverruleCommunicationSettings() {
        return overruleCommunicationSettings;
    }

    public void setOverruleCommunicationSettings(final boolean overruleCommunicationSettings) {
        this.overruleCommunicationSettings = overruleCommunicationSettings;
    }

    public SerialCommunicationSettings getSerialCommunicationSettings() {
        return serialCommunicationSettings;
    }

    public void setSerialCommunicationSettings(final SerialCommunicationSettings serialCommunicationSettings) {
        this.serialCommunicationSettings = serialCommunicationSettings;
    }

    public RtuTypeShadow getRtuTypeShadow() {
        return rtuTypeShadow;
    }

    public void setRtuTypeShadow(final RtuTypeShadow rtuTypeShadow) {
        this.rtuTypeShadow = rtuTypeShadow;
    }

    public List<ChannelFullProtocolShadow> getChannelFullProtocolShadow() {
        return channelFullProtocolShadow;
    }

    public void setChannelFullProtocolShadow(final List<ChannelFullProtocolShadow> channelFullProtocolShadow) {
        this.channelFullProtocolShadow = channelFullProtocolShadow;
    }

    public int getFolderId() {
        return folderId;
    }

    public void setFolderId(final int folderId) {
        this.folderId = folderId;
    }
}
