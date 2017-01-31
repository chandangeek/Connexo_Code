package com.elster.protocolimpl.lis100.profile;

import com.energyict.mdc.upl.io.NestedIOException;

import com.elster.protocolimpl.lis100.Lis100ObjectFactory;
import com.elster.protocolimpl.lis100.ProtocolLink;
import com.energyict.dialer.connection.ConnectionException;

import java.io.IOException;

/**
 * class to read interval data as "stream".
 *
 * User: heuckeg
 * Date: 26.01.11
 * Time: 09:38
 */
public class IVDataStreamReader implements IIntervalDataStreamReader {

    private ProtocolLink link;
    private Lis100ObjectFactory objectFactory;

    private int valTotal = 0;
    private int valInLine = 0;
    private String valData;

    public IVDataStreamReader(Lis100ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
        link = objectFactory.getLink();
    }

    /**
     * Prepare interval data reading
     *
     * @throws IOException - in case of io errors
     * @throws ConnectionException - in case of io errors
     */
    public void prepareRead() throws IOException {
        int i = objectFactory.getMemorySizeObject().getIntValue();
        valTotal = ((i + i) / 3) - 200;
        valInLine = 12;
    }

    /**
     * get archive data as "stream".
     * reads telegrams as needed...
     *
     * @return one read value
     * @throws NestedIOException - in case of io errors
     * @throws com.energyict.dialer.connection.ConnectionException - in case of io errors
     */
    public int readWord() throws NestedIOException, ConnectionException {

        /* consumed a complete telegram? */
        if (valInLine > 10) {
            /* no read more than available... */
            if (valTotal-- <= 0) {
                return -1;
            }

            /* read a new telegram */
            valData = link.getLis100Connection().receiveTelegram((byte)'x');
            valInLine = 0;
        }

        int result = Integer.parseInt(valData.substring(valInLine, valInLine + 3), 16);
        valInLine += 3;
        return result;
    }

}
