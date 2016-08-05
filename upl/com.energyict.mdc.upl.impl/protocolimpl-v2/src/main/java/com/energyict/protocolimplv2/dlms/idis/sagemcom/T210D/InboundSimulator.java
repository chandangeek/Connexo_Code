package com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;

/**
 * Created by cisac on 7/12/2016.
 */
public class InboundSimulator {

    public static final String interval1 = "00010001006600bddb0853414767755d4d1381b13000004f6ecc6feb05b13417ec572ac060f281ad27b280f14ab979eb1d60b67f8de4eb04b5ea01e4d6b471112d77d00eaafc2431c1827095282c9c28af9e5d916fff344b87edbf17ff7152e598e32728425ce40dbe6aabfd163ddeeda065878f2cf20ac0d6de06783e92ba25c4767c4b7415a1651d60933fe8086e4ae25e77b8542e6acf12bbf93eb42f72833f077c5e27d5955dafd68fda86e149639c35fb8a02fedea401ecb9fbcd7a3f95c7b764755c";
    public static final String pushOnConectivity = "00010001006600bcdb0853414767755d4d1381b0300000481e661e1b66a966444f34800a94d9b5a7bf4fc24b68eb17e031f5856dbd939a17d1413024a2c8ee563db0e4daf40f83c41935a6f237c4b37a8a39389f9ff1961a5bc230ad0acd8c874904bcca292162d9c9278eb7d1c94ff4f48eecd153a262b6b792dce904c20d1aed9de06278327fdfae6b2643da7e82146ddb418318fa9d85ae0798e4e570bd0a2a21fdf36e6ac8f5bafc45dc54d93c156945074cd38fb1508138fd88fed48718b6c75b05";
    public static final String pushOnInstallation = "00010000006600d0db0853414767755d4d1381c4300000473ad388dddc10e4dabfb3abe0425683f16e121a43ada0ce15a2907e114de09b1004853ffcf525963dcb2e529df5ef8eea1987578a39732f2691308466259679dbf37d406ceccfd69d078f9d5f280b8fa43256c08698c362250bcbb0a52000a762b8a26df815e7ab36728edef351c7fed0f28711d7a6f533ee4ed769452be1facc067de25169b8ff4900979e4eda1eccf9f9ec9079b8b11a5816b931487154bc4d1d5320859fe48568de1f48120c73536d02827b7f8c6eb9566db66608a5f2bde6";
    public static final String pushOnAlarm = "00010000006600c2db0853414767755d4d1381b630000048e23f37c0ebabff212b50d306f68ec67ad4e96f31da8cd9f2c846e66f40f763b3b34fc4c7faf340e9a2e1c8e56935be36e70173f35bdde6ed43bf68b7b8576942d9d4b0019b15b23e0fb2392e383fc2d33ee3c4224c7e105c7cfc042d93032f24be77bdb65011764b2b87d00a8963c813ad92e62eb3941e56f94abb4f1b5643860330bb76a240a3263d3f758f20fc69b460b9810f42f5e6f2ab5e9926d3c8d4ad3a5f7ca4caf8df729dc762709ff6238d81e5";
    public static final String tag48 = "00010001006600d630000030000048bc26af2f81d3e7422097412633216df8a21dd0dc6f5476f66877a2188b2f0c37ebff4989b874e4bbabf54e7d0552afd1b46c8b4cbbc162f7ea090185001446550d8c737bde74bf9f9495fc4705accd22ce3c7a3ce9a0d78f5a7e9699261ece0f6605370b2aa8d5e958402febabdc7ff22684833a4de833bb4cc5b4a79e23eb6d8b68c9922880a5290920f842326d0880415ccd49adf0d5a2b8aa136d8e8ec91c0431e6ff5fc4c2f12f0ea1563ae3c9f23fb1bdf60b0ee428507143289008e85033666afde975b4412eac800a093f4e";
    //    public static final String gbt1 = "000100000066010de00000010000820104db0853414767755d4d1382018d30000050bc60d88773da56c4aa8decb9ce80a54b8563331adabdcbbf107974c604ef3b5d4d13638fb4fdab55b96235901198b44a06296c768c3cc96263933f12b818846c63f338f559087ecd3482fe82da5730841f38a94c7d4326dab09ff696c54e9a119a4af3a3925715bd318fca0dbea3543579071aa130ca31454c3a3ba10d798c7fe8b2821d8d4085ecfce3e86f89697847f7d7e257538a1381006b72414385c18ea5522cd5c5897425ea540a433ee5b73bb623589b10de2bde2bb9eea05caf633225820dec0993a58d8fe6b24f889dfe47a44b035b533fb1296463d08c74f5ce62678eadba8d459bf748f61360f5757b1045b75f"; //gbt frame 1
    //    public static final String gbt2 = "0001000000660092e08000020000818adb0853414767755d4d1382018d30000050bc60d88773da56c4aa8decb9ce80a54b8563331adabdcbbf107974c604ef3b5d4d13638fb4fdab55b96235901198b44a06296c768c3cc96263933f12b818846c63f338f559087ecd3482fe82da5730841f38a94c7d4326dab09ff696c54e9a119a4af3a3925715bd318fca0dbea3543579071aa130ca31454c"; //gbt frame 2
//    public static final String gbt1 = "000100010066010de00000010000820104db0853414767755d4d1382018d30000069241fbdbf6ca84d9ddaca53e5f51802f28b7dabbe87ed1cd51ae727edbc0a0c9951615462e70329f991c559192d05cd3754490ca19bdb3150db3cb2486bdc5d6a1ee4687ecad19130904b9aa47d8d68a4a411e8005c11ee1981f3d5b89d1c1498b4e49fe4ad15c2d247d56ec8e544501030b413a74f89787c3c27f726e74c5a9221f960ecdc65b1bd3ece37f5098d0f34f04deab3246212fa41bad345b36e6c45337833e2bc3cc0fc012718d892331993fa40245743153d98408cbe5a03faafa9813750855269327ab9f48511276caef2be1257823205e2fa0f8a46cc6f2ca1e33fd551cbb1ddc03105b758f1c09bd9143e31ed"; //gbt frame 1
//    public static final String gbt2 = "0001000100660092e08000020000818adb0853414767755d4d1382018d30000069241fbdbf6ca84d9ddaca53e5f51802f28b7dabbe87ed1cd51ae727edbc0a0c9951615462e70329f991c559192d05cd3754490ca19bdb3150db3cb2486bdc5d6a1ee4687ecad19130904b9aa47d8d68a4a411e8005c11ee1981f3d5b89d1c1498b4e49fe4ad15c2d247d56ec8e544501030b413a74f89787c3c"; //gbt frame 2
    public static final String gbt1 = "00010001006600e2e0000001000081dadb0853414767755d4d138201ab3000006b93003b1f587f3ef56e725631c1ca6817e0ddea4a6e215b36801304692f0c62983cf88cca6968f5c2536df60dcc361b3a0e87644bbca75cebce258c0756157cf7200a0ec64e738a3cd46387373109b06d142812c3dbc16a4cf0e147a9c1afe5312c04fa4254821929cfa30bba0c531022306aa04ff4bec70b6f49d38935275a6527a7678b5257c4a1018ebe9e59d995b4320e9994b106b28a40bc6d933403a20b34ecd53c15cd7f865048d8b250b99968c129512b0dbf051a2ed04a517fe66eea4ccbec399fc7d469cd"; //gbt frame 1
    public static final String gbt2 = "00010001006600cee0800002000081c6db0853414767755d4d138201ab3000006b93003b1f587f3ef56e725631c1ca6817e0ddea4a6e215b36801304692f0c62983cf88cca6968f5c2536df60dcc361b3a0e87644bbca75cebce258c0756157cf7200a0ec64e738a3cd46387373109b06d142812c3dbc16a4cf0e147a9c1afe5312c04fa4254821929cfa30bba0c531022306aa04ff4bec70b6f49d38935275a6527a7678b5257c4a1018ebe9e59d995b4320e9994b106b28a40bc6d933403a20b34ecd53c15cd7f865048d8b250b99968c129512b0d"; //gbt frame 2
    public static final String hostNameOfComserver = "localhost";
    public static final int portNumberOfComserver = 4060;

    public static void main(String[] args) throws Exception {

//            simulatePushData(hostNameOfComserver, portNumberOfComserver, interval1);
//            simulatePushData(hostNameOfComserver, portNumberOfComserver, pushOnConectivity);
//            simulatePushData(hostNameOfComserver, portNumberOfComserver, pushOnInstallation);
//            simulatePushData(hostNameOfComserver, portNumberOfComserver, pushOnAlarm);
//            simulatePushData(hostNameOfComserver, portNumberOfComserver, tag48);
            InboundSimulator.simulateGBT(hostNameOfComserver, portNumberOfComserver, gbt1, gbt2);

        }

    public static void simulatePushData(String hostNameOfComserver, int portNumberOfComserver, String data) throws IOException, InterruptedException {
        Socket clientSocket = new Socket(hostNameOfComserver, portNumberOfComserver);
        clientSocket.setSoTimeout(60000);
        OutputStream outToServer = clientSocket.getOutputStream();
        outToServer.write(hexStringToByteArray(data));
        clientSocket.close();
    }

    public static void simulateGBT(String hostNameOfComserver, int portNumberOfComserver, String ... gbtBlock) throws IOException, InterruptedException {

        Socket clientSocket = new Socket(hostNameOfComserver, portNumberOfComserver);
        clientSocket.setSoTimeout(60000);
        OutputStream outToServer = clientSocket.getOutputStream();

        for(String block: gbtBlock){
            outToServer.write(hexStringToByteArray(block));
        }
        clientSocket.close();

     }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }
        return data;
    }

}
