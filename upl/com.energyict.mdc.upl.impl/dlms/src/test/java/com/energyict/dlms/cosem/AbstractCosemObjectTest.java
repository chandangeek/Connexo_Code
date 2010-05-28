package com.energyict.dlms.cosem;

import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.mocks.MockDLMSConnection;
import com.energyict.dlms.mocks.MockProtocolLink;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

public class AbstractCosemObjectTest {

	@Test
	public void CheckCosemPDUResponseHeaderTest(){
		
		String iskraResponse = 			"100018c701C1000100091048bdb63e5df88885d67330f89ea444bd";
		String iskraResponseFailure = 	"100005c701c10b00";
		String z3Response = 			"010016c701c100091091ec1f928d8e6ace9823717c15c61271";
		String z3ResponseFailure =		"010005c701c10103";
		Data data = new Data(null, null);
		try {
			
			DLMSMeterConfig meterConfig = DLMSMeterConfig.getInstance("WKP");
			ProtocolLink protocolLink = new MockProtocolLink(new MockDLMSConnection(), meterConfig);
			data = new Data(protocolLink, null);
			assertArrayEquals(DLMSUtils.hexStringToByteArray("091048bdb63e5df88885d67330f89ea444bd"), data.CheckCosemPDUResponseHeader(DLMSUtils.hexStringToByteArray(iskraResponse)));
			
			try {
				data.CheckCosemPDUResponseHeader(DLMSUtils.hexStringToByteArray(iskraResponseFailure));
			} catch (DataAccessResultException e) {
				if(!e.getMessage().equalsIgnoreCase("Cosem Data-Access-Result exception Object unavailable")){
					e.printStackTrace();
					fail();
				}
			}
			
			meterConfig = DLMSMeterConfig.getInstance("WKP::OLD");
			protocolLink = new MockProtocolLink(new MockDLMSConnection(), meterConfig);
			data = new Data(protocolLink, null);
			assertArrayEquals(DLMSUtils.hexStringToByteArray("091091ec1f928d8e6ace9823717c15c61271"), data.CheckCosemPDUResponseHeader(DLMSUtils.hexStringToByteArray(z3Response)));
			
			
			try {
				data.CheckCosemPDUResponseHeader(DLMSUtils.hexStringToByteArray(z3ResponseFailure));
			} catch (DataAccessResultException e) {
				if(!e.getMessage().equalsIgnoreCase("Cosem Data-Access-Result exception R/W denied")){
					e.printStackTrace();
					fail();
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

    @Test
    public final void ConfirmedServiceErrorTest(){

        String confirmedServiceErrorResponseEncryptionFailed = "1000050E060006";

        Data data = new Data(null, null);

        DLMSMeterConfig meterConfig = DLMSMeterConfig.getInstance("WKP");
        ProtocolLink protocolLink = new MockProtocolLink(new MockDLMSConnection(), meterConfig);
        data = new Data(protocolLink, null);
        try {
            data.CheckCosemPDUResponseHeader(DLMSUtils.hexStringToByteArray(confirmedServiceErrorResponseEncryptionFailed));
        } catch (Exception e) {
            if (!e.getMessage().equalsIgnoreCase("Confirmed Service Error - 'Write error' - Reason: Application-reference - Error detected by the deciphering function")) {
                e.printStackTrace();
                fail();
            }
        }

    }
}
