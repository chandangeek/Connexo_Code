package com.energyict.protocolimplv2.dlms.common.framecounter;

import com.energyict.mdc.upl.nls.MessageSeed;
import com.energyict.protocol.exceptions.ProtocolRuntimeException;

public class FrameCounterException extends ProtocolRuntimeException {

    public FrameCounterException(MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }

}
