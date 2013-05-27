package com.energyict.protocolimpl.messaging.messages;

import com.energyict.protocolimpl.messaging.*;

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

    @RtuMessageAttribute(
            tag = AnnotatedFWUpdateMessageBuilder.ATTR_URL,
            required = false,
            defaultValue = ""
    )
    String getURL();

    @RtuMessageAttribute(
            tag = AnnotatedFWUpdateMessageBuilder.ATTR_USER_FILE_ID,
            required = false,
            defaultValue = "-1"
    )
    int getUserFileId();

    @RtuMessageAttribute(
            tag = AnnotatedFWUpdateMessageBuilder.ATTR_USER_FILE_CONTENT,
            required = false,
            defaultValue = ""
    )
    byte[] getUserFileContent();

}
