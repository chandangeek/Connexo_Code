package com.energyict.protocolimplv2.dlms.common.readers;

import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.obis.ObisReader;

import java.util.List;
import java.util.Optional;

/**
 * @param <M> is the return type of the reader (e.g CollectedRegister)
 * @param <N> is the type of parameter that specifies what to read (e.g OfflineRegister)
 * @param <L> is the matcher type, what is binding the reader. We can have multiple implementation but usually this will be ObisCode or DlmsClassId.
 */
public class ReaderRegistry<M, N, L, K extends AbstractDlmsProtocol> {

    private final List<ObisReader<M, N, L, K>> readers;

    public ReaderRegistry(List<ObisReader<M, N, L, K>> readerList) {
        this.readers = readerList;
    }

    public Optional<ObisReader<M, N, L, K>> from(L o) {
        for (ObisReader<M, N, L, K> reader : readers) {
            if (reader.isApplicable(o)) {
                return Optional.of(reader);
            }
        }
        return Optional.empty();
    }
}
