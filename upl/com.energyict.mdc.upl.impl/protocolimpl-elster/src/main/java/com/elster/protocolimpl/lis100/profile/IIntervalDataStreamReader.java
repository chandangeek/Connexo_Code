package com.elster.protocolimpl.lis100.profile;

import java.io.IOException;

/**
 * Interface for reading interval data
 * (interface due to testing purposes)
 *
 * User: heuckeg
 * Date: 26.01.11
 * Time: 09:36
 */
public interface IIntervalDataStreamReader {

    public void prepareRead() throws IOException;
    public int readWord() throws IOException;
}
