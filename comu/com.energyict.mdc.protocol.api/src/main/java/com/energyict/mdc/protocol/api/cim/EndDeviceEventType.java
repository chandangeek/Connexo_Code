package com.energyict.mdc.protocol.api.cim;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventorAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;

import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: jbr
 * Date: 16-aug-2011
 * Time: 14:48:13
 */
public class EndDeviceEventType {

    private String code;

    private EndDeviceType type;
    private int typeId;
    private EndDeviceDomain domain;
    private int domainId;
    private EndDeviceSubDomain subdomain;
    private int subDomainId;
    private EndDeviceEventorAction eventOrAction;
    private int eventOrActionId;

    public EndDeviceEventType(String code) {
        this.code = code;
    }

    public EndDeviceEventType(EndDeviceType type, EndDeviceDomain domain, EndDeviceSubDomain subdomain, EndDeviceEventorAction eventOrAction) {
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

    public EndDeviceSubDomain getSubdomain() {
        if (subdomain == null) {
            parseCode();
        }
        return subdomain;
    }

    public EndDeviceEventorAction getEventOrAction() {
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
        this.type = EndDeviceType.get(this.typeId);
        this.domainId = Integer.parseInt(tok.nextToken());
        this.domain = EndDeviceDomain.get(this.domainId);
        this.subDomainId = Integer.parseInt(tok.nextToken());
        this.subdomain = EndDeviceSubDomain.get(this.subDomainId);
        this.eventOrActionId = Integer.parseInt(tok.nextToken());
        this.eventOrAction = EndDeviceEventorAction.get(this.eventOrActionId);
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
            EndDeviceType type = EndDeviceType.get(Integer.parseInt(tokenizer.nextToken()));
            EndDeviceDomain domain = EndDeviceDomain.get(Integer.parseInt(tokenizer.nextToken()));
            EndDeviceSubDomain subdomain = EndDeviceSubDomain.get(Integer.parseInt(tokenizer.nextToken()));
            EndDeviceEventorAction eventOrAction = EndDeviceEventorAction.get(Integer.parseInt(tokenizer.nextToken()));
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

