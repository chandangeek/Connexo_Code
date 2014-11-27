package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

/**
 * Created by bvn on 10/22/14.
 */
public class MessageStatusAdapter extends MapBasedXmlAdapter<DeviceMessageStatus> {

    public static final String REVOKED = MessageSeeds.COMMAND_REVOKED.getKey();

    public MessageStatusAdapter() {
        register("",null);
        register(REVOKED,DeviceMessageStatus.REVOKED);
        register(MessageSeeds.COMMAND_CONFIRMED.getKey(),DeviceMessageStatus.CONFIRMED);
        register(MessageSeeds.COMMAND_FAILED.getKey(),DeviceMessageStatus.FAILED);
        register(MessageSeeds.COMMAND_IN_DOUBT.getKey(),DeviceMessageStatus.INDOUBT);
        register(MessageSeeds.COMMAND_PENDING.getKey(),DeviceMessageStatus.PENDING);
        register(MessageSeeds.COMMAND_SENT.getKey(),DeviceMessageStatus.SENT);
        register(MessageSeeds.COMMAND_WAITING.getKey(),DeviceMessageStatus.WAITING);
    }
}
