package com.energyict.protocolimplv2.dlms.common.readers;

import com.energyict.protocolimplv2.dlms.common.obis.ObisReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @param <M> is the return type of the reader (e.g CollectedRegister)
 * @param <N> is the type of parameter that specifies what to read (e.g OfflineRegister)
 * @param <L> is the matcher type, what is binding the reader. We can have multiple implementation but usually this will be ObisCode or DlmsClassId.
 */
public class DLMSReaderRegistry<M, N, L> {

    public final List<ObisReader<M, N, L>> readers = new ArrayList<>();

    public void add(ObisReader<M, N, L> reader){
        this.readers.add(reader);
    }

    public void addAll(List<ObisReader<M, N, L>> readers) {
        this.readers.addAll(readers);
    }

    public Optional<ObisReader<M, N, L>> from(L o){
        for (ObisReader<M, N, L> reader : readers) {
            if (reader.isApplicable(o)) {
                return Optional.of(reader);
            }
        }
        return Optional.empty();
    }
}
