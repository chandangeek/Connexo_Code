package com.energyict.protocolimplv2.dlms.common.obis;

import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

/**
 *
 * @param <M> is the return type of the final reading (CollectedRegister, CollectedLogBook...)
 * @param <N> is the type of reading input (OfflineRegister, LogBookReader...)
 * @param <L> is the type given by the matcher. The way we registered this reader (see Matcher), normal way it should be ObisCode or DlmsClassId type.
 */
public interface ObisReader<M, N, L> {

    M read(AbstractDlmsProtocol dlmsProtocol, N readingSpecs);

    boolean isApplicable(L l);


}
