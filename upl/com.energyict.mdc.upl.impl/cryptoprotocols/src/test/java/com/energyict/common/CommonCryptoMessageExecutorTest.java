package com.energyict.common;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class CommonCryptoMessageExecutorTest{

    String label = "SSH";
    String wrappedKey = "76342D7061737373";
    String decodedPass = "v4-passs";
    String smartMeterKey = "1FEFD19AF2B25DC35F29B3FD47AD5385";
    String hsmFullKey = label+":"+wrappedKey+","+smartMeterKey;

    @Mock
    private AbstractDlmsProtocol protocol;

    @Mock
    private CollectedDataFactory dataFactory;

    @Mock
    private IssueFactory issueFactory;

    @Test
    public void testHSMLabelDecrypt() {
        CommonCryptoMessageExecutor executor = new CommonCryptoMessageExecutor(protocol, dataFactory, issueFactory);

        String[] parts1 = executor.extractHsmSmartMeterKey(hsmFullKey);
        assertEquals(label+":"+wrappedKey , parts1[0]);
        assertEquals(smartMeterKey , parts1[1]);

        String[] parts2 = executor.extractLabelAndWrappedKey(parts1[0]);
        assertEquals(label, parts2[0] );
        assertEquals(wrappedKey, parts2[1] );
    }


    @Test
    public void testHSMkeyWrapperExctactor(){
        CommonCryptoMessageExecutor executor = new CommonCryptoMessageExecutor(protocol, dataFactory, issueFactory);

        byte[] extractedWrappedKey = executor.extractWrappedKey(hsmFullKey);
        String pass = new String(extractedWrappedKey);
        assertEquals(decodedPass, pass);
        assertArrayEquals(decodedPass.getBytes(), extractedWrappedKey );
    }

}