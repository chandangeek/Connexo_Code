package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.cbo.Sms;
import com.energyict.protocolimpl.utils.ProtocolTools;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 26-okt-2010
 * Time: 9:18:40
 */
public class SmsHandlerTest extends TestCase {

    @Test
    public void testProcessSms() throws Exception {
        byte[] bytes = ProtocolTools.getBytesFromHexString("0000007BB705284A49EA933D5EE5D869E47BD54A187DC5EEB052C3681E1D327D313F1CF9CA37CC9B838F2323BD673FE2B325D1BC28EF3F7612F4C765C24648146F4FB9FDA5E92E394A73A19EBD1F2FAB83034B3900A8168D303D0137F08D8F6D36953FD209E0DFF1D63360DF403CEC0A2374CF93D38103BBBB7FFCD6B8C3BB5CA646EDB364C7921AF8B070A7", "");
        byte[] bytes2 = ProtocolTools.getBytesFromHexString("0000007B9E115602BAC6294C67C05D72028291CF4288F71567CC311283E572F0804E1E9AFF50AAE30FFD61EC884AA2F60D88CE70748C2C5B84C389A76F1C42778202EDE7892D055BA72BF90A57307F4E910CE003432BEBD400D6FBD30034DF1F6CEB8EFCC3938AD4991525F06B50470B7A58388BA45BA798D9B574AC6CC36FAC664A499CCCF392DA560B1C95", "");
        byte[] bytes3 = ProtocolTools.getBytesFromHexString("0000007B2388EBF8E017931D62B84FDAD1718CC1031E249A12D117F85DBD06D8253246625D1F57EE5B83D6A1D4B64A110B688EBEF9389B37BCF90301DC6A2DB119691D5D76B94FA83D8B33C477676AF9B42FA9570D879FCF389FF6BCE5393793E03D480A9215B94C16E73E5CBCA1778FECD01D2CBDFB21E49750CD922F5F3BF6839D5EAA9934EED29FE75E42", "");
        byte[] bytes4 = ProtocolTools.getBytesFromHexString("0000007BB32B8859EAD71B31766D5927BF8E648FE5370ECCD61B5A297F5DE2874143465729109B70398840728901D2E62B5573F2CAD98D4FC21D632BFCBBA3B89C2D8EC6BF3BC131BE68F84649E5AD3DA8D1DE6F57F3419273C8CA0A05D06ED963319C791FEE3A15E0CCE33981D1D1E25C3682D0792381CA16428DA89F7386E96213AFEB74620EA4C4AE5F6B", "");
        byte[] bytes6 = ProtocolTools.getBytesFromHexString("0000007B5E74392FF5F6461E1BFD2D85F007BB94CAC614F4D3570302E4C9C9980318AD59BCFA79EBCBD2828DF4029394EE8AFCC52D734AFD7B1BB336ED53D83BA3429873EFFA8355DE4313FF64E7201B0E007D300404D40039B5C1BEE8FBA382DB2332659EA6F64D9DFB95AE24993EC01FC46786F33ADBE76A8F44D1143189967D89D0D42A98336D88E27CD6", "");
        byte[] bytes7 = ProtocolTools.getBytesFromHexString("0000007BA31EDC0AF8347FFA752CFDA32D017C390FBC1EBCFDA1F6282D7293FE3A0A06C51A6211C54C932C05EB421A23C58178A7B68538497881E17DC816DC61D3CA69A6D4BBE4C6CA1C063E03D6D981F875E64F6112CD702C5F2CC42B51442AE1AC3F1D590B85540700D2774E522714747769A81BAE09C7D9F29B1CE84A3B71C4D5222C6D383DE079A904C2", "");
        byte[] bytes5 = ProtocolTools.getBytesFromHexString("0000007B8642EC461C8D5AE5AEC60EDE914DB1446A57B6D461525B3D99571C34F74EF453A6AD67C95F4B08653897DADE6A5D0EE459C4F6AA51148867E3122118D8C4C4B82566F4CFED2C27EDF22D931298B88967E6DB910ACD43CA062345BCF076B347CD613D62A9BBC0CBF84C169ECC8B248B91A10A67A1626CE1943A2C6A37F71BC3D5396F6BDFA7221FB8", "");
        byte[] bytes8 = ProtocolTools.getBytesFromHexString("0000007B32370A9669CA167390CCEF61DE1709AB51CEC6ADBD6AFF8F5B19DE79B08EE74F8D8F7EA5A8FDEF6C694A85F5A75B8E45F63100F84DC874F0E56CFC94C4A550FD61805ED8BBA673625D6D4EAE9E83BCD285FA8521CBEB5A0682FF678B9C8D3D858915A32A4A9EAC5C6959C42F4CCAD5D22722F964B31DB7DCC038295A3D2A3F4EB2F13F667612B845", "");
        byte[] bytes9 = ProtocolTools.getBytesFromHexString("0000007B8DCB6FC58A7652B041E80ADD1163A144C4CC302C3DEFE6878BEDF411640F527897B4459EA03E88E308B17725DF00424ACCC960A1E0B9EF6D54BC2F89B35A0B1FFE961D3D61C5E34D6EC25F9EF64C54220990C112E44C4075EA1794DFD997F5220E88F815F3623158049044836CDA1A058D8F151DEFCA180975E0EBB0CEDBBA5684D20F6F854A61D1", "");
        byte[] bytes10 = ProtocolTools.getBytesFromHexString("0000005668EF366EAD706EFE9DF13D3C7B5EACD19466D764D8355B2C00150F3E892A6F071EDB31B21509EAC6DB141799D63F2CEDE9C5895333187B924BD14F4F4644B0826CEB7843FAB5537373A43CE2201CB73BD303058B6720974A2FCF0CAC91A562516096C06F88CC0B93F8A3511EB75F7847B727ECE0B06FF981C13F0CB157DB19984CCA39E656D766C2", "");
        byte[] bytes11 = ProtocolTools.getBytesFromHexString("0000007BC62EEA37483F4F67AE1C6086BA75376F2EA7C6C75BD813D2A7921EC7C8E4ACE0AA662DBF1E5B654D0AD717029D667980F611D359BD13263682523A95D045E438CB4FCE1C464D43A3540637E9DF4094F4479983BB11F352728B05B770822F44FA2670CF5AA92032D25D940348E33D35787972BDE9ED1A7506BC92A8CAACEAFDEA0180B2ABECCF7AFF", "");
        byte[] bytes12 = ProtocolTools.getBytesFromHexString("0000007B04ADE422F7FDEF2E6383FEF8E88060194CFB92F25C440F5A6EB8FBED16E7C41BC39E4715F2056B8D626FFEB8EFE54CE1263A4B2293F09016AF83DB000C3B2BB0163D131417ADED12F465755E6383FC57B70A99DDF7ABE7660EE983C750D4258E993B4CB005D044A46DCE1541168A55B0730E43672D061173E95E4F04BA32C5265ACFE989003C6DB7", "");
        byte[] bytes13 = ProtocolTools.getBytesFromHexString("0000007B373EE0529B3310F087FE2A9C8017E16BFE8709C4C01CF6766D3E2A814CE63BA1B1532128020A8309216BFE8C8D716791A38DD46A02F36E83A572AA2E8C97324E24E22573C83C9EF9553586CFAB1D5060C55B9228143AB8887FFF665D4557CE1034B29957D716F8D18DDE7EA4DA848A9DEBFC0F9E0323B2969B72D919E892F1495BA069EBC79C477D", "");
        byte[] bytes14 = ProtocolTools.getBytesFromHexString("0000007BC474E136A8A3A116F0A8D9F10134A932E3E6860776A04426D2826ED3E7269C8DB34A97DAAFC2CDEBB8F7576B20C62EB96CE9BC2E38AA2933855538BDCBECD693D98B837B39FB27F731FCBE435D67195DC0455CAD7703AE8A14CEAE45452AC0C5869195B0B7367F1AD1E816F00B3FF3B4F58DD541570EF664EB40D4AA7B5DB0655590CC241BBCE0AA", "");
        byte[] bytes15 = ProtocolTools.getBytesFromHexString("0000005668EF366EAD706EFE9DF13D3C7B5EACD19466D764D8355B2C00150F3E892A6F071EDB31B21509EAC6DB141799D63F2CEDE9C5895333187B924BD14F4F4644B0826CEB7843FAB5537373A43CE2201CB73BD303058B6720974A2FCF0CAC91A562516096C06F88CC0B93F8A3511EB75F7847B727ECE0B06FF981C13F0CB157DB19984CCA39E656D766C2", "");
        byte[] bytes16 = ProtocolTools.getBytesFromHexString("0000007BC474E136A8A3A116F0A8D9F10134A932E3E6860776A04426D2826ED3E7269C8DB34A97DAAFC2CDEBB8F7576B20C62EB96CE9BC2E38AA2933855538BDCBECD693D98B837B39FB27F731FCBE435D67195DC0455CAD7703AE8A14CEAE45452AC0C5869195B0B7367F1AD1E816F00B3FF3B4F58DD541570EF664EB40D4AA7B5DB0655590CC241BBCE0AA", "");
        byte[] bytes17 = ProtocolTools.getBytesFromHexString("0000007B2AAF2E9F53D93C882DAF555E39916981B596536AAA5B25D92138FA878566BF20006C1F837295082A358CDB3C9DF94BF282B6371194E75FF26CFD665223C4B4F4B045022CCF60E4A54A6DC650BA33165D42DEE8849D680FEF8EC16622536CA0E7E47C8D8AA223B10716D55D9041530B8561F78E72402BEBC579C9DAF630BAD0473F9B4DE065D46344", "");
        byte[] bytes18 = ProtocolTools.getBytesFromHexString("0000007B0398A9678B368C7073A842CCBD48A5DDC3CD5B3688687607BB114FF5045758AFC6E73B9BB7FDAF1265CE2989E02883C8FC0864B1F85E0E833CF977141D5C26329DA0D8E70314369EC4F57DF5EDE7F43C09C741C663336FC00C5EC5CE850E52616202E6E3ECD233CEE2EEDD07FDDDD156079A75DC869C69ADE07E848D6CB6F72BD7219FAC60514760", "");
        byte[] bytes19 = ProtocolTools.getBytesFromHexString("0000007B2485ED148BD991591CE5DA9982E921E96EC562CF45423D7C5FA7C82DE97A91BAECF2AE0878A62BED3A2D5462703293728D271BAE518CCF05B96AF22C1D65A19A363E4A9F71F03706E0C74BFBC69C69F5B8639141BB03F20F93A2E4AD21AE62C71A1666ECDA029B0F832828A09A02C3B0700F5C217452A2F81EE7A63E0265FFED0F6F1C236F9069B2", "");
        byte[] bytes20 = ProtocolTools.getBytesFromHexString("0000007BCBECB10275806E3A6DF2563C1F59DAB63AE1E393D297DFEE47A1A761B26E19AB7EA6A70898282760B2ACB375EC621A068622417991AD18F7D118C2A67C045B431C9DD153F4748F1A8E74AD789BAF370E9D125B719F17251CEC2F843F1BBB1605632A11B5C3093CC33F960809B022D6630BDF7DB7154BD981A7BA8E8F6F1224874831F19A19CE9167", "");
        byte[] bytes21 = ProtocolTools.getBytesFromHexString("0000007B9548C652C5BFF1FC3E673797B26E7B15E7FE67B57F2F5F1B0C3CD15EF798AD748905BEE505607350D003325822435D1BA34FED14F256654892480AE6637E503E7DDC44CE0DE22F7CA6722E0153851864513D92E4D07E3F6F055B93CF56C8627161C358DC6D409D0C0F57DB88DCF8D1EEA4DFDDF261FFDC1C8D95D6509E63CFBF41B0C218F700DE34", "");
        byte[] bytes22 = ProtocolTools.getBytesFromHexString("0000007BB7E83F685FE8D8735D707179747B825002A013191629F058EE6973A30D3B02BC1074C1DFA2E8865B9970EEB9AD20D61D091ED6CD5B899E41820AC061BDABB75E49C3E31D13811F2B735CEF77287EA7F31574C792DDC6E9D0A2A6F9A6A6160193ADD4F4F1A3D4A830EFBF21A0A4EAFB516A380996AF3430E09DA4297B9EC7F87DE4D77B191B38ACB5", "");
        byte[] bytes23 = ProtocolTools.getBytesFromHexString("0000007B3DA0CBF665AA31323B373AAD6F4D49EA2D93C793B5C98E95F54694695731CFB886B58A7A0BEBE0234810E4A7A6862F38E97C69DF010A1056CAB7D87E5693D0E72B7D51BE8790BF4BBA5902EA8A1611D950EE32E5737D7E68CD7C61A6AA713BA1FD3CB6BFEA58553FE8B79E567F20C8EB6D20245EB6EA4BD3CDBFA32AFED5744F76F521D58B862B82", "");
        byte[] bytes24 = ProtocolTools.getBytesFromHexString("0000007B337A48686D34FE2DE17878A170272154F0ACEDD871E3366EE940D94C661CFE0F57D762A596C79795B368A0ECD9BF40C852A9A97DD5BC0ED2C048A2EFD9EDC03ACBC292E74AADFD0463954002B2F02A86BC135C142E097126D53520315E5ED25E147E1224026E06D35C922F1E96EF7A28A39D6C891E5016BBC2B60B81D2849E66D26985DC08987658", "");
        byte[] bytes25 = ProtocolTools.getBytesFromHexString("0000007B80C2561D2BB6868DBB71014767BCE281A24418F2EFF4C36BFCD27E22970D29282EF35E795121C826A951AA1DAEEBA515D4EDD1FE57ECA4D5CCE4FA69EAE6255561DFD1684596AA7EC7BD8991273F459148C73C298C321A01FC6F92F99915957259AB6538D61C8C603E8238D5DAFA8F0FF63291B511735FC2D355E715CC66851B371D40D166F56942", "");
        byte[] bytes26 = ProtocolTools.getBytesFromHexString("0000007B1F59A8219388603B2BCCE7F6AFBDC5542E89A216082B398BCA9EA3F46C01DDF0C6C49922C83647899A69E8C8835EA53FDF64084868FF5A6BF1CB8885D381216C4C7AA25ABED70FCFE8C128A16573714B34958320F67CA74B4501BE2BDD4C0F9D432BC6DB493C36C6179375B3CDFD0FEC10BFB5BF8BE0AFB663D3BE991DC2190F5E9C8B351DE59BEE", "");
        byte[] bytes27 = ProtocolTools.getBytesFromHexString("0000007BA3FB559732317873A88F05BBA41D7B7F4CF867E43337D82B4D506F68E55F5A1AB6B0B0DF796EEF951C59AA5B54B2972F70FD6480151E4CA3A2899A81AF7A9FF2E79C39F3A787708B9F2A8EF0524D7445F4C5FE2F66771303EF83DB16B3957759C7CED4EB40EC60E642DCBAD1FD570A9C583AC8BBB48F74CBE9AA82DAC46AC8646EE86E015CA8DE4C", "");

        //3 Nov
        byte[] bytes28 = ProtocolTools.getBytesFromHexString("0000007BB3E15E4299701D90AF414635277068EB1EB59658E9F452B12350CDB55733E8B9ADF5EAD31B9DBAFB3AFDC055B0125F765AE63E401D66D27186DB3CB511A1FB3DDFCB569C4C6F09F96146F63D73AF35F4A285DB4FCE3C5C2A1793C9C0FD9840A522E44902025E9122DC1C1713DC30D3313B4B7591E8AFB52574E1390A396519C54037E50FD57BFC6A", "");
        byte[] bytes29 = ProtocolTools.getBytesFromHexString("0000007B441438B83FB1222261FEE4230E92C32CFCE5C164478FD28DEB779D633A7C5321591240244C775AB251D89BDAD676B8981F9A898ED75BEE4A9883E918E819A4B6C118CC95E459409872F679E6D1DD5D3888267A863D8598F53A8847ABB64262560E17B8B5B01CA738F2082257435C05D1487E794F6008FFAD5FBCF8816C71D659EC462921B178B056", "");
        byte[] bytes30 = ProtocolTools.getBytesFromHexString("0000007B4A7B388D0EF55DA71058ED1EE23C9394DA3570003A760A7A0BD8149F0021231FD87249FFA0C2EBF3BC5F5CB2AA194BC5251C27E100116F52AB3A36E32D6DAAED3E609B361136FE8CF9A4DC771535218333AD7C87B9EB8AD15A37CC0FF0B726B524A0DBAB2AA1616C9A0673C68DA443F58FD7EF93F14D1937661FC17D1A106E6D71E058971690E3BC", "");
        byte[] bytes31 = ProtocolTools.getBytesFromHexString("0000007B87331E39B42B2555AA60D487013DE5697AC2D623319C1E9F366C3A1BA8AFC5E67CC590D1B081F829B810944365D1F4182C17C944EAEDFA9B5BB50E2893A92DAD7C4724A617A67EE97627851DF972A03DBF942A9B3A870A79D7B6F0805FBA6E9CD2157227D98EB18A80F73720A52063B8E20AB9AEADBDF6A4F762953CC2546AB0F0F8658A511581B9", "");

        //4 Nov
        byte[] bytes32 = ProtocolTools.getBytesFromHexString("0000007BBCEADAB4BA8EFDCC31CE0C78933471BABB92A645C12DD0F7591D1922E21A6FF504981857E67FB944FDA8C9D9A466FB8DA13355A5E65037E780FA23A218C88295D92280B8103F25D8EC1954775D84B6B0C58CE039DA2FC51B0A8D54F547D9EB25A93B67E2A98E795AD6100E7C41194536B35AEC803839812AE8CCA433706CB724D22F39CC23C9665D", "");
        byte[] bytes33 = ProtocolTools.getBytesFromHexString("0000007BDADC9F9AA21D5B37F16BA27D526EA3876D3D5240C6178EB3CCC8E7D3A235DDC15B189A341257B815FADABF73C7CF685F5622C23834EEB78086E7171F3F88A8D99B3F951901FC34169F0713EE9C6E55B70EF18ED791DA957F957A34CAF2E05EBD1139C04FB12B9C05A09E956896D55A802C9831DD1E66528149C9653E66DD879BBF32EE4F8C758AA9", "");
        byte[] bytes34 = ProtocolTools.getBytesFromHexString("0000007B8FA2FF9513F0CC0D75DFA65093166E461513E58A81824D301A5828A3EC5EB42C4BCB7B3674C1A36CED5AA4F74CCD90D7EF579831919E0875B838DC522D18A2FB10C0D90636AB8E6116C0BF464C465D643A628AB6B1575B9178C3940167A3DC15155C112F90C008802F994C3DCC9D48F8D30B4C90DEF35DE04C338C80DBCA75009E48DB6D569B94AB", "");
        byte[] bytes35 = ProtocolTools.getBytesFromHexString("0000007BED03C522DA0B5AE5D4291F9CBBB74158FE6FEAE47F215A3DB26389A427538BB788FE9743BB1E3CCAD0B9B6F312C52CCFFF0F247AF95406D45610245AB4CA4A4AF4084C24F4F5461E4371AC0EE4E8487EDB2D9B62DF40500147112C876B1FDAAD742E4A28B4797AF7F91D40F88BFABEABD287CD0F89F2167E5561D7760E776E6744F90514F6B70F6E", "");

        //5 Nov
        byte[] bytes36 = ProtocolTools.getBytesFromHexString("0000007B3ABDAFF7D8E5D994B22C20E8DDC0287FEC85157CDBEF2F2B069882ABA7C4D53B6CF989EF0C3C82846495608C773B711348198066886C51857DE56D6B706D26464B4867BF458ACD2839E26439B449FEC7AE61AA65CC0043F4C12E0274F06B630D251C4C16725E12EFB061ECBA39A4D7A17B2C57FD5953C541975574DC5CCAEEB7A988ACC634183FE5", "");
        byte[] bytes37 = ProtocolTools.getBytesFromHexString("0000007B5BB438C635125459D268AEC1271EC5D7337F54C6C2867AACE916E3CFE6A5289DF598774F23BD24679EAEEC29B449B82978AD3FE68B1A3AC8A5143BD44EA67553625659A8C8B295FBF411AD587B4BC0B7D8CAC731FF4C3FDBDD6C7CA5974D1FA569C3F04107ED9C8B81322E11971D0AAC220FBCADFAB36205FB6F15AD028657379E76135373DD5EA5", "");
        byte[] bytes38 = ProtocolTools.getBytesFromHexString("0000007B779900F49E9156E83F60FD4533EEAA7C44E07084F98923D6AC41CB01B06F345DAF0B249FC72C2F400656CD1AA7BA04C148DB389020268DD44F0CA615CA3919313649E408228B0F9D1E586D754F94323BB9ED6DCE74FB9ECED04D4079475ABEA4A86E5598FE34F02DB0BF7EF297CEB0F267D1042AD1C1A356ABA7ECC1FD0BAEC715429DEBD3CFA685", "");
        byte[] bytes39 = ProtocolTools.getBytesFromHexString("0000007BAC42C3373086359B5870A0BF3D8BEAF3393B8D2B61A47EBBC140C9593F912B18B6E2397823189A19F1EC05BF391278D05773615F63032F0DAFB831A95301CF1120CC367C9FBF321CF31A265796C42E7C44C823633F009647D0FF52271AB6DE59FF5EC833ADB732B0E97E93F89FECC86544FD16EC68614A66971B3C17BAF79479DE301EA757A675EF", "");

        //not encrypted
        byte[] bytesDEC = ProtocolTools.getBytesFromHexString("0000003B3300123456789000010A0A0A0D260001000000010101010101010102010201020102010203040000010203040000010203040000010203040102030400000102000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", "");
        byte[] bytesDECF = ProtocolTools.getBytesFromHexString("0000003B34001234567890000105050505050101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010000000000000000000000000000000000000000000000000000000000", "");
        byte[] bytesEvents = ProtocolTools.getBytesFromHexString("0000003B5600123456789000010F00000002002000060A0A150E0A000101400F00000001000000010A0A150E0A0001013A0F00000001000000010A0A150E0A000101460F00000001000000010A0A0A0E0A000101350F00000001000000010A0A080E0A000101350F00000001000000010A0A070E0A00010135FF0000000100000001", "");
        byte[] bytesZero = ProtocolTools.getBytesFromHexString("0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", "");

        //For 7bit decoding tests
        byte[] bytesTest7bit = ProtocolTools.getBytesFromHexString("D4F29C0E", "");
        byte[] bytesTest7bit2 = ProtocolTools.getBytesFromHexString("CC309CFE8683C6EF36BC4E2FCB41F331BA2C6F83E87978D90DA2BFCBF479D92D7ECBC92071795D96D3D56510", "");

        Sms sms = new Sms("+3256356291", "20893210", new Date(), "proximus", "0001", 8, bytes36);
        Sms sms2 = new Sms("056356291", "20893210", new Date(), "proximus", "0001", 7, bytesTest7bit);
        Sms sms3 = new Sms("056356291", "20893210", new Date(), "proximus", "0001", 7, bytesTest7bit2);

        //Tests the 7bit decoder
        assertEquals("Test", sms2.getText());
        assertEquals("Laptop computer scherm typen toetsenbord bekertje ", sms3.getText());

        SmsHandler handler = new SmsHandler();
        //handler.processMessage(sms);


    }
}
