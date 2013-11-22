package com.energyict.protocolimpl.edf.messages.usecases;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.Folder;
import com.energyict.mdw.core.FolderAction;
import com.energyict.protocolimpl.edf.messages.MessageContent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public abstract class AbstractFolderAction implements FolderAction {

    public abstract void execute(Folder folder) throws SQLException, BusinessException;

    void addMessage(Device rtu, String obis, int ordinal)
            throws Exception {

//        OldDeviceMessageShadow shadow = new OldDeviceMessageShadow(rtu.getId());
//        shadow.setReleaseDate(new Date());
//
//        MessageReadRegister mrr = new MessageReadRegister(obis);
//        mrr.setOrdinal(ordinal);
//
//        shadow.setContents(mrr.xmlEncode());

//        rtu.createOldMessage(shadow);

    }

    void createMessage(Device rtu, MessageContent content)
            throws Exception {

//        OldDeviceMessageShadow shadow = new OldDeviceMessageShadow(rtu.getId());
//        shadow.setReleaseDate(new Date());
//
//        shadow.setContents(content.xmlEncode());
////        rtu.createOldMessage(shadow);

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
