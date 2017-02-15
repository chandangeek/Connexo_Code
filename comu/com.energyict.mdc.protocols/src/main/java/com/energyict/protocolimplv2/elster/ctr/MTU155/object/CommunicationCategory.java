/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.object;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.AccessDescriptor;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.Qualifier;
import com.energyict.protocolimplv2.elster.ctr.MTU155.primitive.CTRPrimitiveParser;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 14:29:16
 */
public class CommunicationCategory<T extends CommunicationCategory> extends AbstractCTRObject<T> {

    //Parse the raw data & fill in the object's properties
    public T parse(byte[] rawData, int ptr, AttributeType type) {
        setType(type);
        CTRPrimitiveParser parser = new CTRPrimitiveParser();   //Not static

        if (type.hasIdentifier()) {
            ptr += CTRObjectID.LENGTH; //Skip the Id bytes
        }
        if (type.hasQualifier()) {
            Qualifier qlf = new Qualifier(parser.parseQlf(rawData, ptr));
            this.setQlf(qlf);
            if (qlf.isInvalid() && type.isRegisterQuery()) {
                return (T) this;   //If the QLF is 0xFF (invalid), return an empty object
            }
            ptr += qlf.getLength();
        }

        if (type.hasValueFields()) {
            int[] valueLength = this.getValueLengths(getId());
            this.setValue(parser.parseBCDValue(this, rawData, ptr, valueLength));
            ptr += sum(valueLength);  //There might be multiple value fields
        }

        if (type.hasAccessDescriptor()) {
            AccessDescriptor access = new AccessDescriptor(parser.parseAccess(rawData, ptr));
            this.setAccess(access);
            ptr += access.getLength();
        }

        if (type.hasDefaultValue()) {
            this.setDefault(parser.parseDefault(getId(), this.getValue()));
        }

        this.setSymbol(getSymbol(getId()));

        return (T) this;
    }

    public CommunicationCategory(CTRObjectID id) {
        this.setId(id);
    }

    public BigDecimal getOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        return null;
    }

    public int[] getValueLengths(CTRObjectID id) {
        int[] valueLength = null;
        switch (id.getY()) {
            case 0x00:
                valueLength = new int[]{1,2,1,1};
                break;
            case 0x02:
                valueLength = new int[]{1, 21};
                break;
            case 0x03:
                switch (id.getZ()) {
                    case 0x02:
                        valueLength = new int[]{1, 1, 4, 2, 7};
                        break;
                    default:
                        valueLength = new int[]{1, 14};
                        break;
                }
                break;
            case 0x04:
                valueLength = new int[]{1, 14};
                break;
            case 0x07:
                valueLength = new int[]{2, 2, 4, 9};
                break;
            case 0x0C:
                valueLength = new int[]{1};
                break;
            case 0x0E:
                valueLength = new int[]{1, 111};
                break;
            case 0x0D:
                valueLength = new int[]{4,4,4,4,4,4};
                break;
        }
        return valueLength;
    }


    public Unit getUnit(CTRObjectID id, int valueNumber) {
        Unit unit = Unit.get(BaseUnit.UNITLESS);
        switch (id.getY()) {
            case 0x0C:
                unit = Unit.get(BaseUnit.UNITLESS); break;
        }
        return unit;

    }


    protected String getSymbol(CTRObjectID id) {
        String symbol = "";

        switch (id.getY()) {
            case 0x02:
                symbol = "Ntlf_C_"; break;
            case 0x03:
                symbol = "Ntlf_S_"; break;
            case 0x07:
                symbol = "WU"; break;
            case 0x0C:
                symbol = "GSM"; break;
            case 0x0E:
                symbol = "GPRS_S"; break;
        }
        return symbol;
    }


}
