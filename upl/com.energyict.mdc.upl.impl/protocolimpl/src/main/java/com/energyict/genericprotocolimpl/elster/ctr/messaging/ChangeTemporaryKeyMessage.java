package com.energyict.genericprotocolimpl.elster.ctr.messaging;

import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRObjectID;
import com.energyict.protocol.messaging.MessageSpec;

/**
 * Copyrights EnergyICT
 * Date: 4/03/11
 * Time: 8:40
 */
public class ChangeTemporaryKeyMessage extends AbstractChangeKeyMessage {

    public static final String KEY_NAME = "Temporary";
    public static final String CHANGE_KEYT_OBJECT_ID = "11.0.E";

    public ChangeTemporaryKeyMessage(MTU155MessageExecutor messageExecutor) {
        super(messageExecutor, KEY_NAME);
    }

    @Override
    protected void writeKey(String key) throws CTRException {
        getFactory().executeRequest(new CTRObjectID(CHANGE_KEYT_OBJECT_ID), key.getBytes());
    }

    public static MessageSpec getMessageSpec(boolean advanced) {
        return createMessageSpec(advanced, KEY_NAME);
    }

}
