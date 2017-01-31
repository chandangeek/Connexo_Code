/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Integer8;

import java.io.IOException;
import java.util.Date;

public class TransparantObjectAccessFactory {

    GenericHeader genericHeader;

    final GenericHeader getGenericHeader() {
        return genericHeader;
    }


    public final static int ATTRIBUTE_VALUE=2;

    public final static int GENERIC_PROFILE_ENTRIES_IN_USE=7;
    public final static int GENERIC_PROFILE_ENTRIES=8;

    private final AbstractDLMS abstractDLMS;

    TransparantObjectAccessFactory(AbstractDLMS abstractDLMS) {
        this.abstractDLMS = abstractDLMS;
    }

    private final void invoke(AbstractTransparentObjectAccess atoa) throws IOException {
        atoa.invoke();
        genericHeader = atoa.getGenericHeader();
    }

    public final AbstractDataType readObjectValue(ObisCode obisCode) throws IOException {
        ObjectEntry o = abstractDLMS.findObjectByObiscode(obisCode);
        TransparentGet tg = new TransparentGet(abstractDLMS, new ObjectInfo(ATTRIBUTE_VALUE, o.getClassId(),obisCode));
        invoke(tg);
        return tg.getDataType();
    }

    public final AbstractDataType readObjectAttribute(String description, int attribute) throws IOException {
        return readObjectAttributeRange(abstractDLMS.findEntryByDescription(description).getKey(),attribute,null);
    }

    public final AbstractDataType readObjectAttribute(ObisCode obisCode, int attribute) throws IOException {
        return readObjectAttributeRange(obisCode,attribute,null);
    }

    public final AbstractDataType readObjectAttributeRange(ObisCode obisCode, int attribute, Date fromDate) throws IOException {
        ObjectEntry o = abstractDLMS.findObjectByObiscode(obisCode);
        TransparentGet tg = new TransparentGet(abstractDLMS, new ObjectInfo(attribute, o.getClassId(),obisCode));
        tg.setFromDate(fromDate);
        invoke(tg);
        return tg.getDataType();
    }

    public final AbstractDataType readObjectAttributeRange(ObisCode obisCode, int attribute, Date fromDate, Date toDate) throws IOException {
        ObjectEntry o = abstractDLMS.findObjectByObiscode(obisCode);
        TransparentGet tg = new TransparentGet(abstractDLMS, new ObjectInfo(attribute, o.getClassId(),obisCode));
        tg.setFromDate(fromDate);
        tg.setToDate(toDate);
        invoke(tg);
        return tg.getDataType();
    }

    public final AbstractDataType readObjectAttributeEntry(ObisCode obisCode, int attribute, int fromEntry, int offset) throws IOException {
        ObjectEntry o = abstractDLMS.findObjectByObiscode(obisCode);
        TransparentGet tg = new TransparentGet(abstractDLMS, new ObjectInfo(attribute, o.getClassId(),obisCode));
        tg.setFromEntry(fromEntry);
        tg.setOffset(offset);
        invoke(tg);
        return tg.getDataType();
    }

    public final void writeObjectAttribute(ObisCode obisCode, int attribute, AbstractDataType dataType) throws IOException {
        ObjectEntry o = abstractDLMS.findObjectByObiscode(obisCode);
        TransparentSet ts = new TransparentSet(abstractDLMS, new ObjectInfo(attribute, o.getClassId(),obisCode));
        ts.setDataType(dataType);
        invoke(ts);
    }

    public final void executeObjectAction(ObisCode obisCode, int methodNumber) throws IOException {
        ObjectEntry disconnectControl = abstractDLMS.findObjectByObiscode(obisCode);
        TransparentAction action = new TransparentAction(abstractDLMS, new ObjectInfo(methodNumber, disconnectControl.getClassId(), obisCode));
        action.setDataType(new Integer8(0));
        invoke(action);
    }
}