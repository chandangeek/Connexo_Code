package com.elster.protocolimpl.dlms.messaging;

import com.energyict.cbo.BusinessException;
import org.junit.Test;

/**
 * User: heuckeg
 * Date: 06.10.11
 * Time: 11:37
 */
public class TestWriteAutoConnectMessage {

    @Test
    public void destinationCheckOk1Test() throws BusinessException {

        WriteAutoConnectMessage wacm = new WriteAutoConnectMessage(null);
        wacm.checkDestination("hallo.com:1234", "test");
    }

    @Test
    public void destinationCheckOk2Test() throws BusinessException {

        WriteAutoConnectMessage wacm = new WriteAutoConnectMessage(null);
        wacm.checkDestination("hallo-2.a:1", "test");
    }
    @Test
    public void destinationCheckOk3Test() throws BusinessException {

        WriteAutoConnectMessage wacm = new WriteAutoConnectMessage(null);
        wacm.checkDestination("1.2.3.4:1234", "test");
    }
    @Test
    public void destinationCheckOk4Test() throws BusinessException {

        WriteAutoConnectMessage wacm = new WriteAutoConnectMessage(null);
        wacm.checkDestination("www.elster.com:0", "test");
    }

    @Test(expected = BusinessException.class)
    public void destinationCheckNotOk1Test() throws BusinessException {

        WriteAutoConnectMessage wacm = new WriteAutoConnectMessage(null);
        wacm.checkDestination("1.2.3.4", "test");
    }

    @Test(expected = BusinessException.class)
    public void destinationCheckNotOk2Test() throws BusinessException {

        WriteAutoConnectMessage wacm = new WriteAutoConnectMessage(null);
        wacm.checkDestination("hello", "test");
    }

    @Test(expected = BusinessException.class)
    public void destinationCheckNotOk3Test() throws BusinessException {

        WriteAutoConnectMessage wacm = new WriteAutoConnectMessage(null);
        wacm.checkDestination(".hello:1", "test");
    }

    @Test(expected = BusinessException.class)
    public void destinationCheckNotOk4Test() throws BusinessException {

        WriteAutoConnectMessage wacm = new WriteAutoConnectMessage(null);
        wacm.checkDestination("hel%lo:1", "test");
    }

    @Test(expected = BusinessException.class)
    public void destinationCheckNotOk5Test() throws BusinessException {

        WriteAutoConnectMessage wacm = new WriteAutoConnectMessage(null);
        wacm.checkDestination("hello.:1", "test");
    }

    @Test(expected = BusinessException.class)
    public void destinationCheckNotOk6Test() throws BusinessException {

        WriteAutoConnectMessage wacm = new WriteAutoConnectMessage(null);
        wacm.checkDestination("1.2.3:1", "test");
    }

    @Test(expected = BusinessException.class)
    public void destinationCheckNotOk7Test() throws BusinessException {

        WriteAutoConnectMessage wacm = new WriteAutoConnectMessage(null);
        wacm.checkDestination("1.2.3.:1", "test");
    }

    @Test(expected = BusinessException.class)
    public void destinationCheckNotOk8Test() throws BusinessException {

        WriteAutoConnectMessage wacm = new WriteAutoConnectMessage(null);
        wacm.checkDestination("1..3.4:1", "test");
    }
}
