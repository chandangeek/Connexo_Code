package com.energyict.mdc.protocol.api;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ConsumerType;

import java.io.InputStream;
import java.util.function.Consumer;

/**
 * Models a file that can be sent to a
 * {@link com.energyict.mdc.upl.meterdata.Device}
 * by means of a {@link com.energyict.mdc.protocol.api.device.messages.DeviceMessage}.
 * <p>
 * Note that implementation classes should not forget to close InputStreams that
 * are passed to the Consumer of the readWith method.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-11 (12:58)
 */
@ConsumerType
public interface DeviceMessageFile extends HasId, HasName, com.energyict.mdc.upl.properties.DeviceMessageFile {

    /**
     * Supports reading the contents of this DeviceMessageFile.
     * Note that the consumer is not responsible to close the InputStream.
     *
     * @param inputStreamConsumer Receives the InputStream that provides access to the contents of this DeviceMessageFile
     */
    void readWith(Consumer<InputStream> inputStreamConsumer);

}