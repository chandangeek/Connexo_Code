/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.umi.ei4.requests;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.offline.OfflineDevice;

import com.energyict.protocolcommon.Pair;
import com.energyict.protocolimplv2.umi.ei4.EI4Umi;
import com.energyict.protocolimplv2.umi.ei4.structures.UmiGsmStdStatus;
import com.energyict.protocolimplv2.umi.ei4.structures.UmiwanConfiguration;
import com.energyict.protocolimplv2.umi.ei4.structures.UmiwanStdStatus;
import com.energyict.protocolimplv2.umi.packet.payload.ReadObjPartRspPayload;
import com.energyict.protocolimplv2.umi.packet.payload.ReadObjRspPayload;
import com.energyict.protocolimplv2.umi.types.ResultCode;
import com.energyict.protocolimplv2.umi.types.UmiObjectPart;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class EI4UmiCodesReader {

    private final EI4Umi protocol;
    private final IssueFactory issueFactory;
    private final OfflineDevice offlineDevice;

    public EI4UmiCodesReader(EI4Umi protocol, IssueFactory issueFactory, OfflineDevice offlineDevice) {
        this.protocol = protocol;
        this.issueFactory = issueFactory;
        this.offlineDevice = offlineDevice;
    }

    public UmiwanStdStatus getUmiwanStdStatus() throws IOException {
        try {
            Pair<ResultCode, ReadObjRspPayload> pair = protocol.getUmiSession().readObject(UmiwanStdStatus.UMIWAN_STD_STATUS_UMI_CODE);
            if (pair.getFirst() != ResultCode.OK) {
                protocol.journal(Level.WARNING, "Read umiwan std status operation failed. " + pair.getFirst().getDescription());
                throw new ProtocolException("Reading of umiwan std status " + UmiwanStdStatus.UMIWAN_STD_STATUS_UMI_CODE.getCode() + " failed. " + pair.getFirst().getDescription());
            } else {
                return new UmiwanStdStatus(pair.getLast().getValue());
            }
        } catch (GeneralSecurityException e) {
            // should not occur in EI4
        }
        return null;
    }

    public UmiGsmStdStatus getGsmStdStatus() throws IOException {
        try {
            byte[] raw = new byte[UmiGsmStdStatus.SIZE];
            int size = 0;
            for (int i = 0; i < 17; i++) {
                UmiObjectPart umiObjectPart = new UmiObjectPart("umi.1.1.194.2/" + i);
                Pair<ResultCode, ReadObjPartRspPayload> pairSN = protocol.getUmiSession().readObjectPart(umiObjectPart);
                if (pairSN.getFirst() != ResultCode.OK) {
                    protocol.journal(Level.WARNING, "Read gsm std status operation failed. " + pairSN.getFirst().getDescription());
                    throw new ProtocolException("Reading of gsm std status " + umiObjectPart.getUmiCode().getCode() + " failed. " + pairSN.getFirst().getDescription());
                } else {
                    byte[] receivedSN = pairSN.getLast().getValue();
                    for (int j = 0; j < receivedSN.length; j++) {
                        raw[size] = receivedSN[j];
                        size++;
                    }
                }
            }
            return new UmiGsmStdStatus(raw);
        } catch (GeneralSecurityException e) {
            // should not occur in EI4
        }
        return null;
    }
}
