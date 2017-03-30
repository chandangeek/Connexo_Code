/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Address;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Data;
import com.energyict.protocolimplv2.elster.garnet.frame.field.FunctionCode;
import com.energyict.protocolimplv2.elster.garnet.structure.field.DateTime;
import com.energyict.protocolimplv2.elster.garnet.structure.field.RepeaterDiagnostic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Clock;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

/**
 * @author sva
 * @since 27/05/2014 - 13:57
 */
public class DiscoverRepeatersResponseStructure extends Data<DiscoverRepeatersResponseStructure> {

    private static final int NR_OF_POSITIONS = 40;
    public static final FunctionCode FUNCTION_CODE = FunctionCode.DISCOVER_REPEATERS_RESPONSE;

    private DateTime dateTime;

    private HashMap<Address, RepeaterDiagnostic> repeaters;

    private final Clock clock;
    private final TimeZone timeZone;

    public DiscoverRepeatersResponseStructure(Clock clock, TimeZone timeZone) {
        super(FUNCTION_CODE);
        this.clock = clock;
        this.timeZone = timeZone;
        this.dateTime = new DateTime(clock, timeZone);
        this.repeaters = new HashMap<>(NR_OF_POSITIONS);
    }

    @Override
    public byte[] getBytes() {
        ByteArrayOutputStream repeaterByteStream = new ByteArrayOutputStream();
        try {
            int count = 0;
            Iterator<Map.Entry<Address, RepeaterDiagnostic>> it = repeaters.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Address, RepeaterDiagnostic> entry = it.next();
                repeaterByteStream.write(entry.getKey().getBytes());
                repeaterByteStream.write(entry.getValue().getBytes());
                count++;
            }

            while (count < NR_OF_POSITIONS) {   // If not all repeaters are specified, pad with additional zeros
                repeaterByteStream.write(new byte[3]);
                count++;
            }

            return ProtocolTools.concatByteArrays(
                    dateTime.getBytes(),
                    repeaterByteStream.toByteArray()
            );

        } catch (IOException e) {
            // Should never reach this point
            return new byte[0];
        }
    }

    @Override
    public DiscoverRepeatersResponseStructure parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        this.dateTime = new DateTime(this.clock, getTimeZone()).parse(rawData, ptr);
        ptr += dateTime.getLength();

        for (int i = 0; i < NR_OF_POSITIONS; i++) {
            Address address = new Address(true).parse(rawData, ptr);
            ptr += address.getLength();

            RepeaterDiagnostic diagnostic = new RepeaterDiagnostic().parse(rawData, ptr);
            ptr += diagnostic.getLength();

            if (address.getAddress() != 0) {     // in case of 0 = not used
                this.repeaters.put(address, diagnostic);
            }
        }

        return this;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public HashMap<Address, RepeaterDiagnostic> getRepeaterMap() {
        return repeaters;
    }

    public RepeaterDiagnostic getRepeaterDiagnostic(int repeaterDeviceId) {
        for (Map.Entry<Address, RepeaterDiagnostic> entry : repeaters.entrySet()) {
            if (entry.getKey().getAddress() == repeaterDeviceId) {
                return entry.getValue();
            }
        }
        return null;
    }
}