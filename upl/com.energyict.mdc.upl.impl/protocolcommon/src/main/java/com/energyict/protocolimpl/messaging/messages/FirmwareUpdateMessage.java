package com.energyict.protocolimpl.messaging.messages;

import com.energyict.protocolimpl.messaging.AnnotatedMessage;
import com.energyict.protocolimpl.messaging.RtuMessageAttribute;
import com.energyict.protocolimpl.messaging.RtuMessageDescription;

/**
 * Copyrights EnergyICT
 * Date: 6/29/12
 * Time: 2:49 PM
 */
@RtuMessageDescription(
        category = "Miscellaneous",
        description = "Firmware update",
        tag = "FirmwareUpdate",
        advanced = true,
        visible = false
)
public interface FirmwareUpdateMessage extends AnnotatedMessage {

    String ATTR_USER_FILE_ID = "userFileID";
    String ATTR_USER_FILE_CONTENT = "userFileContent";
    String ATTR_URL = "url";

    @RtuMessageAttribute(
            tag = ATTR_URL,
            required = false,
            defaultValue = ""
    )
    String getURL();

    @RtuMessageAttribute(
            tag = ATTR_USER_FILE_ID,
            required = false,
            defaultValue = "-1"
    )
    int getUserFileId();

    @RtuMessageAttribute(
            tag = ATTR_USER_FILE_CONTENT,
            required = false,
            defaultValue = ""
    )
    byte[] getUserFileContent();

}
