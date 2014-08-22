package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventorAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.energyict.mdc.device.data.EndDeviceEventReading;
import java.util.Date;

/**
 * Created by bvn on 8/21/14.
 */
public class EndDeviceEventReadingImpl implements EndDeviceEventReading {

    private Date eventDate;
    private Date readingDate;
    private Long eventLogId;
    private String code;
    private String deviceCode;
    private String message;
    private EndDeviceEventorAction eventOrAction;
    private EndDeviceSubDomain subDomain;
    private String cimCode;
    private String deviceType;
    private EndDeviceDomain domain;

    @Override
    public Date getEventDate() {
        return this.eventDate;
    }

    @Override
    public Date getReadingDate() {
        return this.readingDate;
    }

    @Override
    public Long getEventLogId() {
        return this.eventLogId;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getDeviceCode() {
        return this.deviceCode;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public String getCimCode() {
        return this.cimCode;
    }

    @Override
    public String getDeviceType() {
        return this.deviceType;
    }

    @Override
    public EndDeviceDomain getDomain() {
        return this.domain;
    }

    @Override
    public EndDeviceSubDomain getSubDomain() {
        return this.subDomain;
    }

    @Override
    public EndDeviceEventorAction getEventOrAction() {
        return this.eventOrAction;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public void setReadingDate(Date readingDate) {
        this.readingDate = readingDate;
    }

    public void setEventLogId(Long eventLogId) {
        this.eventLogId = eventLogId;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setEventOrAction(EndDeviceEventorAction eventOrAction) {
        this.eventOrAction = eventOrAction;
    }

    public void setSubDomain(EndDeviceSubDomain subDomain) {
        this.subDomain = subDomain;
    }

    public void setCimCode(String cimCode) {
        this.cimCode = cimCode;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public void setDomain(EndDeviceDomain domain) {
        this.domain = domain;
    }
}
