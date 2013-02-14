package com.energyict.protocolimpl.edf.messages.usecases;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.*;
import com.energyict.mdw.core.*;
import com.energyict.mdw.shadow.DeviceMessageShadow;
import com.energyict.protocolimpl.edf.messages.MessageContent;
import com.energyict.protocolimpl.edf.messages.MessageReadRegister;

import java.sql.SQLException;
import java.util.*;

public abstract class AbstractFolderAction implements FolderAction {

    public abstract void execute(Folder folder) throws SQLException, BusinessException;

    void addMessage(Device rtu, String obis, int ordinal)
            throws Exception {

        DeviceMessageShadow shadow = new DeviceMessageShadow(rtu.getId());
        shadow.setReleaseDate(new Date());

        MessageReadRegister mrr = new MessageReadRegister(obis);
        mrr.setOrdinal(ordinal);

        shadow.setContents(mrr.xmlEncode());

        rtu.createOldMessage(shadow);

    }

    void createMessage(Device rtu, MessageContent content)
            throws Exception {

        DeviceMessageShadow shadow = new DeviceMessageShadow(rtu.getId());
        shadow.setReleaseDate(new Date());

        shadow.setContents(content.xmlEncode());
        rtu.createOldMessage(shadow);

    }

    public boolean isEnabled(Folder folder) {
        return true;
    }

    @Override
    public void addProperties(TypedProperties properties) {
        addProperties(properties.toStringProperties());
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return PropertySpecFactory.toPropertySpecs(getRequiredKeys());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return PropertySpecFactory.toPropertySpecs(getOptionalKeys());
    }

    public void addProperties(Properties properties) {
    }

    public List<String> getOptionalKeys() {
        return new ArrayList<String>();
    }

    public List<String> getRequiredKeys() {
        return new ArrayList<String>();
    }

}
