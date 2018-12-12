/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.cim;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: jbr
 * Date: 16-aug-2011
 * Time: 14:48:13
 */
@XmlRootElement
public class EndDeviceEventType {

    private String code;

    private EndDeviceType type;
    private int typeId;
    private EndDeviceDomain domain;
    private int domainId;
    private EndDeviceSubdomain subdomain;
    private int subDomainId;
    private EndDeviceEventOrAction eventOrAction;
    private int eventOrActionId;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    private EndDeviceEventType() {
    }

    public EndDeviceEventType(String code) {
        this.code = code;
    }

    public EndDeviceEventType(EndDeviceType type, EndDeviceDomain domain, EndDeviceSubdomain subdomain, EndDeviceEventOrAction eventOrAction) {
        this.type = type;
        this.typeId=type.getValue();
        this.domain = domain;
        this.domainId=domain.getValue();
        this.subdomain = subdomain;
        this.subDomainId=subdomain.getValue();
        this.eventOrAction = eventOrAction;
        this.eventOrActionId= eventOrAction.getValue();
        this.code = type.getValue() + "." + domain.getValue() + "." + subdomain.getValue() + "." + eventOrAction.getValue();
    }

    @XmlAttribute
    public String getCode() {
        return code;
    }

    public EndDeviceType getType() {
        if (type == null) {
            parseCode();
        }
        return type;
    }

    public EndDeviceDomain getDomain() {
        if (domain == null) {
            parseCode();
        }
        return domain;
    }

    public EndDeviceSubdomain getSubdomain() {
        if (subdomain == null) {
            parseCode();
        }
        return subdomain;
    }

    public EndDeviceEventOrAction getEventOrAction() {
        if (eventOrAction == null) {
            parseCode();
        }
        return eventOrAction;
    }

    public String toString() {
        return code;
    }

    private void parseCode() {
        StringTokenizer tok = new StringTokenizer(code, ".");
        this.typeId = Integer.parseInt(tok.nextToken());
        this.type = EndDeviceType.fromValue(this.typeId);
        this.domainId = Integer.parseInt(tok.nextToken());
        this.domain = EndDeviceDomain.fromValue(this.domainId);
        this.subDomainId = Integer.parseInt(tok.nextToken());
        this.subdomain = EndDeviceSubdomain.fromValue(this.subDomainId);
        this.eventOrActionId = Integer.parseInt(tok.nextToken());
        this.eventOrAction = EndDeviceEventOrAction.fromValue(this.eventOrActionId);
    }

    public int getTypeId() {
        return typeId;
    }

    public int getDomainId() {
        return domainId;
    }

    public int getSubDomainId() {
        return subDomainId;
    }

    public int getEventOrActionId() {
        return eventOrActionId;
    }
    public static EndDeviceEventType fromString(String codeString) {
        StringTokenizer tokenizer = new StringTokenizer(codeString, ".");
        if (tokenizer.countTokens() != 4) {
            throw new IllegalArgumentException(codeString);
        }
        try {
            EndDeviceType type = EndDeviceType.fromValue(Integer.parseInt(tokenizer.nextToken()));
            EndDeviceDomain domain = EndDeviceDomain.fromValue(Integer.parseInt(tokenizer.nextToken()));
            EndDeviceSubdomain subdomain = EndDeviceSubdomain.fromValue(Integer.parseInt(tokenizer.nextToken()));
            EndDeviceEventOrAction eventOrAction = EndDeviceEventOrAction.fromValue(Integer.parseInt(tokenizer.nextToken()));
            if (type == null || domain == null || subdomain == null || eventOrAction == null) {
                throw new IllegalArgumentException(codeString);
            }
            return new EndDeviceEventType(type, domain, subdomain, eventOrAction);
        } catch (NumberFormatException x) {
            throw new IllegalArgumentException(codeString);
        }
    }

    /* Methods for comparing. */
    @Override
    public int hashCode() {
        return getCode().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return getCode().equals(((EndDeviceEventType) o).getCode());
    }
}

