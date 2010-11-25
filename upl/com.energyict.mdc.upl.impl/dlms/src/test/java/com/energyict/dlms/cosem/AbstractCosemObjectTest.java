package com.energyict.dlms.cosem;

import com.energyict.dlms.*;
import com.energyict.dlms.mocks.MockDLMSConnection;
import com.energyict.dlms.mocks.MockProtocolLink;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.base.ParseUtils;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

public class AbstractCosemObjectTest {

    @Test
    public void CheckCosemPDUResponseHeaderTest() {

        String iskraResponse = "100018c701C1000100091048bdb63e5df88885d67330f89ea444bd";
        String iskraResponseFailure = "100005c701c10b00";
        String z3Response = "010016c701c100091091ec1f928d8e6ace9823717c15c61271";
        String z3ResponseFailure = "010005c701c10103";


        Data data = new Data(null, null);
        try {

            DLMSMeterConfig meterConfig = DLMSMeterConfig.getInstance("WKP");
            ProtocolLink protocolLink = new MockProtocolLink(new MockDLMSConnection(), meterConfig);
            data = new Data(protocolLink, null);
            assertArrayEquals(DLMSUtils.hexStringToByteArray("091048bdb63e5df88885d67330f89ea444bd"), data.CheckCosemPDUResponseHeader(DLMSUtils.hexStringToByteArray(iskraResponse)));

            try {
                data.CheckCosemPDUResponseHeader(DLMSUtils.hexStringToByteArray(iskraResponseFailure));
            } catch (DataAccessResultException e) {
                if (!e.getMessage().equalsIgnoreCase("Cosem Data-Access-Result exception Object unavailable")) {
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
                if (!e.getMessage().equalsIgnoreCase("Cosem Data-Access-Result exception R/W denied")) {
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
    public final void checkArrayOutOfBounds() {
        String response1 = "640400c402810000000000001570204090c07da071402071e0000ff8880110006007160f606000000be0204001100060071617806000000be020400110006007161c106000000be0204001100060071620906000000be0204001100060071624006000000be0204001100060071628506000000be020400110006007162e106000000be0204001100060071633406000000be0204001100060071641806000000be020400110006007164a506000000be0204001100060071653d06000000be020400110006007165ae06000000be0204001100060071664906000000be020400110006007166d106000000be0204001100060071672d06000000be0204001100060071676b06000000be0204001100060071682a06000000be0204001100060071691b06000000be02040011000600716a0006000000be02040011000600716ae806000000be02040011000600716bd306000000be02040011000600716cb106000000be02040011000600716d8b06000000be02040011000600716e7506000000be02040011000600716f6106000000be0204001100060071704c06000000be0204001100060071713706000000be0204001100060071722806000000be0204001100060071730606000000be0204001100060071741206000000be020400110006007174f006000000be020400110006007175cd06000000be020400110006007176aa06000000be0204001100060071779c06000000be0204001100060071788506000000be0204001100060071797606000000be02040011000600717a6406000000be02040011000600717b6506000000be02040011000600717c7406000000be02040011000600717e0f06000000be02040011000600717f0806000000be0204001100060071805306000000be020400110006007181bb06000000be020400110006007183e806000000be0204001100060071862306000000be020400110006007188af06000000be02040011000600718ab206000000be02040011000600718c9706000000be02040011000600718df106000000be0204001100060071900106000000be020400110006007191ef06000000be020400110006007193e506000000be020400110006007195c306000000be020400110006007196c106000000be020400110006007197c406000000be020400110006007198c606000000be020400110006007199dc06000000be02040011000600719afe06000000be02040011000600719c1006000000be02040011000600719cde06000000be02040011000600719ded06000000be02040011000600719f0f06000000be0204001100060071a03906000000be0204001100060071a15806000000be0204001100060071a1f406000000be0204001100060071a24906000000be0204001100060071a27506000000be0204001100060071a2bf06000000be0204001100060071a30906000000be0204001100060071a34506000000be0204001100060071a38906000000be0204001100060071a3da06000000be0204001100060071a41106000000be0204001100060071a43806000000be0204001100060071a48406000000be0204001100060071a4cd06000000be0204001100060071a50a06000000be0204001100060071a55006000000be0204001100060071a59d06000000be0204001100060071a5cf06000000be0204001100060071a5fb06000000be0204001100060071a65506000000be0204001100060071a69d06000000be0204001100060071a6d606000000be0204001100060071a71806000000be0204001100060071a75f06000000be0204001100060071a78b06000000be0071779c06000000be0204001100060071788506000000be0204001100060071797606000000be02040011000600717a6406000000be02040011000600717b6506000000be02040011000600717c7406000000be02040011000600717e0f06000000be02040011000600717f0806000000be0204001100060071805306000000be020400110006007181bb06000000be020400110006007183e806000000be0204001100060071862306000000be020400110006007188af06000000be02040011000600718ab206000000be02040011000600718c9706000000be02040011000600718df106000000be0204001100060071900106000000be020400110006007191ef06000000be020400110006007193e506000000be020400110006007195c306000000be020400110006007196c106000000be020400110006007197c406000000be020400110006007198c606000000be020400110006007199dc06000000be02040011000600719afe06000000be02040011000600719c1006000000be02040011000600719cde06000000be02040011000600719ded06000000be02040011000600719f0f06000000be0204001100060071a03906000000be0204001100060071a15806000000be0204001100060071a1f406000000be0204001100060071a24906000000be02040011000600";
        String response2 = "640140c4028101000000010082013471a27506000000be0204001100060071a2bf06000000be0204001100060071a30906000000be0204001100060071a34506000000be0204001100060071a38906000000be0204001100060071a3da06000000be0204001100060071a41106000000be0204001100060071a43806000000be0204001100060071a48406000000be0204001100060071a4cd06000000be0204001100060071a50a06000000be0204001100060071a55006000000be0204001100060071a59d06000000be0204001100060071a5cf06000000be0204001100060071a5fb06000000be0204001100060071a65506000000be0204001100060071a69d06000000be0204001100060071a6d606000000be0204001100060071a71806000000be0204001100060071a75f06000000be0204001100060071a78b06000000be";

        String emptyResponse = "64000ac4028101000000020000";

        String response3 = "640383c402810000000000008203770182499b0204090c07da01070401000000ffc40011000600160a1906000009e002040011000600160a2c06000009e002040011000600160a3806000009e002040011000600160a3b06000009e002040011000600160a4e06000009e002040011000600160a5a06000009e002040011000600160a6406000009e002040011000600160a7206000009e002040011000600160a7a06000009e002040011000600160a9106000009e002040011000600160a9406000009e002040011000600160a9806000009e002040011000600160ab406000009e002040011000600160ab706000009e002040011000600160ac106000009e002040011000600160ad306000009e002040011000600160ada06000009e002040011000600160aec06000009e002040011000600160af006000009e002040011000600160afc06000009e002040011000600160b1006000009e002040011000600160b1406000009e002040011000600160b1b06000009e002040011000600160b3306000009e002040011000600160b3706000009e002040011000600160b4606000009e002040011000600160b5706000009e002040011000600160b6a06000009e002040011000600160b7c06000009e002040011000600160b8b06000009e002040011000600160bd206000009e002040011000600160c1a06000009e002040011000600160c4806000009e002040011000600160c9506000009e002040011000600160cd806000009e002040011000600160ceb06000009e002040011000600160d0a06000009e002040011000600160d1a06000009e002040011000600160d3506000009e002040011000600160ddf06000009e002040011000600160e5206000009e002040011000600160eba06000009e002040011000600160fd506000009e0020400110006001610a806000009e0020400110006001610d106000009e0020400110006001610ea06000009e0020400110006001610fb06000009e00204001100060016113406000009e00204001100060016115e06000009e00204001100060016118706000009e0020400110006001611f006000009e00204001100060016121d06000009e00204001100060016123806000009e00204001100060016125606000009e00204001100060016126706000009e00204001100060016128906000009e0020400110006001612a006000009e0020400110006001612b906000009e0";

        Data data = new Data(null, null);
        DLMSMeterConfig meterConfig = DLMSMeterConfig.getInstance("WKP");
        MockDLMSConnection connection = new MockDLMSConnection();
        connection.setResponseByte(DLMSUtils.hexStringToByteArray(response2));
        ProtocolLink protocolLink;

        try {
            meterConfig = DLMSMeterConfig.getInstance("WKP");
            protocolLink = new MockProtocolLink(connection, meterConfig);
            data = new Data(protocolLink, null);
            data.CheckCosemPDUResponseHeader(DLMSUtils.hexStringToByteArray(response1));
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }

        connection = new MockDLMSConnection();
        protocolLink = new MockProtocolLink(connection, meterConfig);

        try {
            meterConfig = DLMSMeterConfig.getInstance("WKP");
            protocolLink = new MockProtocolLink(connection, meterConfig);
            data = new Data(protocolLink, null);
             DataContainer dataContainer = new DataContainer();
            dataContainer.parseObjectList(data.CheckCosemPDUResponseHeader(DLMSUtils.hexStringToByteArray(response3)), null);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public final void ConfirmedServiceErrorTest() {

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