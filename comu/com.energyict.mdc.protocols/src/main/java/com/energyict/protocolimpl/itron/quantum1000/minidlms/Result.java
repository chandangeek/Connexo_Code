/*
 * Result.java
 *
 * Created on 8 december 2006, 15:57
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Koen
 */
public class Result {

    static List resultCodes = new ArrayList();
    static List contexts = new ArrayList();

    static {
        resultCodes.add(new ResultCode(0,"RSL_OK"));
        resultCodes.add(new ResultCode(1,"RSL_NO_CONFIG"));
        resultCodes.add(new ResultCode(2,"RSL_CONFIG_ERR"));
        resultCodes.add(new ResultCode(3,"RSL_NO_MEMORY"));
        resultCodes.add(new ResultCode(4,"RSL_RESERVED"));
        resultCodes.add(new ResultCode(5,"RSL_ALREADY_OPEN"));
        resultCodes.add(new ResultCode(6,"RSL_NOT_OPEN"));
        resultCodes.add(new ResultCode(7,"RSL_ERROR"));
        resultCodes.add(new ResultCode(8,"RSL_NOT_CREATED"));
        resultCodes.add(new ResultCode(9,"RSL_DIR_FULL"));
        resultCodes.add(new ResultCode(10,"RSL_DISK_FULL"));
        resultCodes.add(new ResultCode(11,"RSL_DUPLICATE"));
        resultCodes.add(new ResultCode(12,"RSL_NOT_FOUND"));
        resultCodes.add(new ResultCode(13,"RSL_ALREADY_EXISTS"));
        resultCodes.add(new ResultCode(14,"RSL_POSITION_ERROR"));
        resultCodes.add(new ResultCode(15,"RSL_CORRUPT_FILE_SYSTEM"));
        resultCodes.add(new ResultCode(16,"RSL_INITIALIZE_ERROR"));
        resultCodes.add(new ResultCode(17,"RSL_ACCESS_ERROR"));
        resultCodes.add(new ResultCode(18,"RSL_TABLE_FULL"));
        resultCodes.add(new ResultCode(19,"RSL_NOT_IMPLEMENTED"));
        resultCodes.add(new ResultCode(20,"RSL_NULL_POINTER"));
        resultCodes.add(new ResultCode(21,"RSL_RANGE_ERROR"));
        resultCodes.add(new ResultCode(22,"RSL_CHECK_ERROR"));
        resultCodes.add(new ResultCode(23,"RSL_STREAM_GET_ERROR"));
        resultCodes.add(new ResultCode(24,"RSL_STREAM_PUT_ERROR"));
        resultCodes.add(new ResultCode(25,"RSL_CORRUPT_OBJECT"));
        resultCodes.add(new ResultCode(26,"RSL_BUG"));
        resultCodes.add(new ResultCode(27,"RSL_CONFIG_ERROR"));
        resultCodes.add(new ResultCode(28,"RSL_MUST_BE_INACTIVE"));
        resultCodes.add(new ResultCode(29,"RSL_CORRUPT_BACKUP"));
        resultCodes.add(new ResultCode(30,"RSL_NOT_ACTIVE"));
        resultCodes.add(new ResultCode(31,"RSL_CORRUPT_FILE"));
        resultCodes.add(new ResultCode(32,"RSL_OPEN_ERROR"));
        resultCodes.add(new ResultCode(33,"RSL_WRITE_ERROR"));
        resultCodes.add(new ResultCode(34,"RSL_BAD_PARAMS"));
        resultCodes.add(new ResultCode(35,"RSL_NO_ROOM"));
        resultCodes.add(new ResultCode(36,"RSL_NOT_POWERED_UP"));
        resultCodes.add(new ResultCode(37,"RSL_NO_SESSION"));
        resultCodes.add(new ResultCode(38,"RSL_BUSY"));
        resultCodes.add(new ResultCode(39,"RSL_EOF"));
        resultCodes.add(new ResultCode(40,"RSL_WRITE_DISABLED"));
        resultCodes.add(new ResultCode(41,"RSL_TOOKTOOLONG"));
        resultCodes.add(new ResultCode(42,"RSL_MUTEX_FAILED"));
        resultCodes.add(new ResultCode(43,"RSL_INVALID_OBJECT_TYPE"));
        contexts.add(new Context(0,"CTX_NONE"));
        contexts.add(new Context(1,"CTX_DEMANDSUMMARYFILE_OPEN"));
        contexts.add(new Context(2,"CTX_DEMANDSUMMARYFILE_REMOVE"));
        contexts.add(new Context(3,"CTX_DEMANDSUMMARYFILE_CREATE"));
        contexts.add(new Context(4,"CTX_DEMANDSUMMARYFILE_CHECKFILE"));
        contexts.add(new Context(5,"CTX_DEMANDSUMMARYFILE_GETSUMMARY"));
        contexts.add(new Context(6,"CTX_DEMANDSUMMARYFILE_PUTSUMMARY"));
        contexts.add(new Context(7,"CTX_DEMANDSUMMARYGROUP_CHECKALL"));
        contexts.add(new Context(8,"CTX_DEMANDSUMMARYGROUP_GETSUMMARY"));
        contexts.add(new Context(9,"CTX_DEMANDGROUP_GETPEAK"));
        contexts.add(new Context(10,"CTX_DEMANDGROUP_GETMINIMUM"));
        contexts.add(new Context(11,"CTX_DEMANDGROUP_GETSUMMARY"));
        contexts.add(new Context(12,"CTX_TOUSUMMARYGROUP_POWERUP"));
        contexts.add(new Context(13,"CTX_TOUSUMMARYGROUP_GETSUMMARY"));
        contexts.add(new Context(14,"CTX_TOUSUMMARYGROUP_CHECKALL"));
        contexts.add(new Context(15,"CTX_TOUSUMMARYGROUP_RESTART"));
        contexts.add(new Context(16,"CTX_MULTIPLEPEAKSANDMINIMUMSGROUP_GETSUMMARY"));
        contexts.add(new Context(17,"CTX_MULTIPLEPEAKSANDMINIMUMSGROUP_PUTSUMMARY"));
        contexts.add(new Context(18,"CTX_MULTIPLEPEAKSANDMINIMUMSGROUP_CHECKALL"));
        contexts.add(new Context(19,"CTX_MULTIPLEPEAKSORMINIMUMSFILE_CREATE"));
        contexts.add(new Context(20,"CTX_MULTIPLEPEAKSORMINIMUMSFILE_REMOVE"));
        contexts.add(new Context(21,"CTX_MULTIPLEPEAKSORMINIMUMSFILE_OPEN"));
        contexts.add(new Context(22,"CTX_MULTIPLEPEAKSORMINIMUMSFILE_CHECKFILE"));
        contexts.add(new Context(23,"CTX_MULTIPLEPEAKSORMINIMUMSFILE_GETSUMMARY"));
        contexts.add(new Context(24,"CTX_MULTIPLEPEAKSORMINIMUMSFILE_PUTSUMMARY"));
        contexts.add(new Context(25,"CTX_DEMANDCONFIG_POWERUP"));
        contexts.add(new Context(26,"CTX_DEMANDCONFIG_RECONFIGURE"));
        contexts.add(new Context(27,"CTX_DEMANDCONFIG_SAVETOFLASH"));
        contexts.add(new Context(28,"CTX_DEMANDCONFIG_RESTOREFROMFLASH"));
        contexts.add(new Context(29,"CTX_MULTIPLEPEAKSANDMINIMUMSGROUP_UPDATE"));
        contexts.add(new Context(30,"CTX_MULTIPLEPEAKSANDMINIMUMSGROUP_POWERUP"));
        contexts.add(new Context(31,"CTX_MULTIPLEPEAKSANDMINIMUMSGROUP_RESTART"));
        contexts.add(new Context(32,"CTX_MULTIPLEPEAKSANDMINIMUMSGROUP_REMOVE"));
        contexts.add(new Context(33,"CTX_MULTIPLEPEAKSANDMINIMUMSGROUP_DEMANDRESET"));
        contexts.add(new Context(34,"CTX_DEMANDMAN_POWERUP"));
        contexts.add(new Context(35,"CTX_DEMANDMAN_RECONFIG"));
        contexts.add(new Context(36,"CTX_DEMANDMAN_SAVE"));
        contexts.add(new Context(37,"CTX_DEMANDMAN_RESTORE"));
        contexts.add(new Context(38,"CTX_TOUSUMMARYGROUP_UPDATE"));
        contexts.add(new Context(39,"CTX_TOUSUMMARYGROUP_REMOVE"));
        contexts.add(new Context(40,"CTX_TOUSUMMARYGROUP_DEMANDRESET"));
        contexts.add(new Context(41,"CTX_DEMANDSUMMARYGROUP_UPDATE"));
        contexts.add(new Context(42,"CTX_DEMANDSUMMARYGROUP_POWERUP"));
        contexts.add(new Context(43,"CTX_DEMANDSUMMARYGROUP_DEMANDRESET"));
        contexts.add(new Context(44,"CTX_DEMANDMAN_RESTART"));
        contexts.add(new Context(50,"CTX_VFS_ADDFILE"));
        contexts.add(new Context(51,"CTX_VFS_FINDFILE"));
        contexts.add(new Context(52,"CTX_VFS_REMOVEFILE"));
        contexts.add(new Context(53,"CTX_VFS_GETFILELENGTH"));
        contexts.add(new Context(54,"CTX_VFS_LOCATEBYTE"));
        contexts.add(new Context(55,"CTX_VFS_CREATE"));
        contexts.add(new Context(56,"CTX_VFS_OPEN"));
        contexts.add(new Context(57,"CTX_VFS_CHECKFS"));
        contexts.add(new Context(58,"CTX_VFSH_LOAD"));
        contexts.add(new Context(59,"CTX_VFSDIR_OPEN"));
        contexts.add(new Context(60,"CTX_VFSDIR_CREATE"));
        contexts.add(new Context(61,"CTX_VFSDIR_CHECK"));
        contexts.add(new Context(62,"CTX_VFSDIR_GETENTRY"));
        contexts.add(new Context(63,"CTX_VFSDIR_CHECKENTRY"));
        contexts.add(new Context(64,"CTX_VFSDIR_PUTENTRY"));
        contexts.add(new Context(65,"CTX_VFSDIR_ADDENTRY"));
        contexts.add(new Context(66,"CTX_VFSDIR_FINDENTRY"));
        contexts.add(new Context(67,"CTX_VFSDIR_CHECKNAME"));
        contexts.add(new Context(68,"CTX_VFSFAT_GETHEADER"));
        contexts.add(new Context(69,"CTX_VFSFAT_PUTHEADER"));
        contexts.add(new Context(70,"CTX_VFSFAT_CREATE"));
        contexts.add(new Context(71,"CTX_VFSFAT_OPEN"));
        contexts.add(new Context(72,"CTX_VFSFAT_COUNTFREECHUNKS"));
        contexts.add(new Context(73,"CTX_VFSFAT_CHECKLENGTH"));
        contexts.add(new Context(74,"CTX_VFSFAT_ADD"));
        contexts.add(new Context(75,"CTX_VFSFAT_REMOVE"));
        contexts.add(new Context(76,"CTX_VD_CREATE"));
        contexts.add(new Context(77,"CTX_VD_OPEN"));
        contexts.add(new Context(78,"CTX_VFS_CHECKCONFIG"));
        contexts.add(new Context(79,"CTX_VFSFAT_CHECKSTORAGE"));
        contexts.add(new Context(80,"CTX_DIAGS_RECONFIG"));
        contexts.add(new Context(81,"CTX_DIAGS_OPEN"));
        contexts.add(new Context(82,"CTX_DIAGS_CREATE"));
        contexts.add(new Context(83,"CTX_DIAGS_POWERUP"));
        contexts.add(new Context(84,"CTX_DIAGS_LOADDATA"));
        contexts.add(new Context(85,"CTX_DIAGS_CREATEDATAFILE"));
        contexts.add(new Context(86,"CTX_DIAGS_POSTDATA"));
        contexts.add(new Context(87,"CTX_DIAGS_GETTIMEDACTION"));
        contexts.add(new Context(88,"CTX_DIAGS_CHECKRAM"));
        contexts.add(new Context(89,"CTX_INFOMANCFG_OPEN"));
        contexts.add(new Context(90,"CTX_INFOMAN_RECONFIG"));
        contexts.add(new Context(91,"CTX_INFOMAN_CREATE"));
        contexts.add(new Context(92,"CTX_INFOMAN_POWERUP"));
        contexts.add(new Context(93,"CTX_INFOMAN_LOADDATA"));
        contexts.add(new Context(94,"CTX_INFOMAN_CREATEDATAFILE"));
        contexts.add(new Context(95,"CTX_INFOMAN_POSTDATA"));
        contexts.add(new Context(96,"CTX_ENERGY_POWERUPCONFIG"));
        contexts.add(new Context(97,"CTX_ENERGY_POWERUPOPENDATAFILE"));
        contexts.add(new Context(98,"CTX_ENERGY_POWERUPOPENTOUFILE"));
        contexts.add(new Context(99,"CTX_ENERGY_POWERUPCREATEDATAFILE"));
        contexts.add(new Context(100,"CTX_ENERGY_POWERUPCREATETOUFILE"));
        contexts.add(new Context(101,"CTX_ENERGY_RECONFIGURE"));
        contexts.add(new Context(102,"CTX_ENERGY_RESTARTREMOVEDATAFILE"));
        contexts.add(new Context(103,"CTX_ENERGY_RESTARTREMOVETOUFILE"));
        contexts.add(new Context(104,"CTX_ENERGY_RESTARTCREATEDATAFILE"));
        contexts.add(new Context(105,"CTX_ENERGY_RESTARTCREATETOUFILE"));
        contexts.add(new Context(106,"CTX_ENERGY_SETENERGY"));
        contexts.add(new Context(107,"CTX_ENERGY_SETTOUENERGY"));
        contexts.add(new Context(108,"CTX_ENERGY_NEWSAMPLESLOADCURRENTRATE"));
        contexts.add(new Context(109,"CTX_ENERGY_NEWSAMPLESSAVEDATA"));
        contexts.add(new Context(110,"CTX_ENERGY_NEWSAMPLESSAVECURRENTRATE"));
        contexts.add(new Context(111,"CTX_ENERGY_RESTARTCONFIG"));
        contexts.add(new Context(112,"CTX_ENERGY_DEMANDRESETLOADCURRENTRATE"));
        contexts.add(new Context(113,"CTX_ENERGY_DEMANDRESETSAVECURRENTRATE"));
        contexts.add(new Context(114,"CTX_MM_POSTSTATUSFILE"));
        contexts.add(new Context(115,"CTX_MMF_CREATE"));
        contexts.add(new Context(116,"CTX_MMF_OPEN4RD"));
        contexts.add(new Context(117,"CTX_MMF_OPEN4WR"));
        contexts.add(new Context(118,"CTX_MMF_REMOVE"));
        contexts.add(new Context(119,"CTX_MMF_ADDINTERVAL"));
        contexts.add(new Context(120,"CTX_MMF_GETRECORD"));
        contexts.add(new Context(121,"CTX_MMF_GENERAL"));
        contexts.add(new Context(122,"CTX_MMF_GETINFO"));
        contexts.add(new Context(123,"CTX_MMC_OPEN"));
        contexts.add(new Context(124,"CTX_MMC_CREATE"));
        contexts.add(new Context(125,"CTX_MMC_GENERAL"));
        contexts.add(new Context(126,"CTX_MMC_RESTORE"));
        contexts.add(new Context(127,"CTX_MMC_SAVE"));
        contexts.add(new Context(128,"CTX_MM_CREATESTATUSFILE"));
        contexts.add(new Context(129,"CTX_MM_RECOVERSTATUSFILE"));
        contexts.add(new Context(130,"CTX_MM_RESTART"));
        contexts.add(new Context(131,"CTX_GEOISC_OPEN"));
        contexts.add(new Context(132,"CTX_GEOISC_CREATE"));
        contexts.add(new Context(133,"CTX_GEOISC_RESTORE"));
        contexts.add(new Context(134,"CTX_GEOISC_SAVE"));
        contexts.add(new Context(135,"CTX_GEOIS_RESTART"));
        contexts.add(new Context(136,"CTX_VST"));
        contexts.add(new Context(137,"CTX_VOF_SELECT"));
        contexts.add(new Context(138,"CTX_VOF_POST"));
        contexts.add(new Context(139,"CTX_VOF_CREATE"));
        contexts.add(new Context(140,"CTX_VOF_OPEN"));
        contexts.add(new Context(141,"CTX_VIEWDISP"));
        contexts.add(new Context(142,"CTX_VIEWDISP_SETDEFAULTVIEW"));
        contexts.add(new Context(143,"CTX_VIEWDISP_DEFAULTVIEW"));
        contexts.add(new Context(144,"CTX_VIEW_OPENFILE"));
        contexts.add(new Context(145,"CTX_VIEW_LOCK"));
        contexts.add(new Context(146,"CTX_VIEW_READRECORDNUM"));
        contexts.add(new Context(147,"CTX_VIEW_READNEXT"));
        contexts.add(new Context(148,"CTX_VIEW_SELECTRECPASTTIME"));
        contexts.add(new Context(149,"CTX_VIEW"));
        contexts.add(new Context(150,"CTX_VIEW_ADDRESSSOURCE"));
        contexts.add(new Context(151,"CTX_VIEW_OPEN"));
        contexts.add(new Context(152,"CTX_VIEW_STARTSESSION"));
        contexts.add(new Context(153,"CTX_VIEW_CONTINUESESSION"));
        contexts.add(new Context(154,"CTX_VQCONFIG_POWERUP"));
        contexts.add(new Context(155,"CTX_VQCONFIG_RECONFIGURE"));
        contexts.add(new Context(156,"CTX_VQFILE_CREATE"));
        contexts.add(new Context(157,"CTX_VQFILE_REMOVE"));
        contexts.add(new Context(158,"CTX_VQFILE_REMOVECLOSED"));
        contexts.add(new Context(159,"CTX_VQFILE_OPENFORWRITE"));
        contexts.add(new Context(160,"CTX_VQFILE_PUTRECORD"));
        contexts.add(new Context(161,"CTX_VQFILE_CHECKRECORD"));
        contexts.add(new Context(162,"CTX_VQFILE_GETRECORD"));
        contexts.add(new Context(163,"CTX_VQFILE_GETLASTRECORDWRITTEN"));
        contexts.add(new Context(164,"CTX_VQFILE_OPENFORREAD"));
        contexts.add(new Context(165,"CTX_VQINTERRUPTION_POSTSTATUS"));
        contexts.add(new Context(166,"CTX_VQINTERRUPTION_CREATESTATUSFILE"));
        contexts.add(new Context(167,"CTX_VQINTERRUPTION_OPENFILE"));
        contexts.add(new Context(168,"CTX_VQINTERRUPTION_CREATEFILE"));
        contexts.add(new Context(169,"CTX_VQINTERRUPTION_OPENSTATUSFILE"));
        contexts.add(new Context(170,"CTX_VQINTERRUPTION_LOADSTATUS"));
        contexts.add(new Context(171,"CTX_VQSAG_CREATEFILE"));
        contexts.add(new Context(172,"CTX_VQSWELL_CREATEFILE"));
        contexts.add(new Context(173,"CTX_VQSAG_OPENFILE"));
        contexts.add(new Context(174,"CTX_VQSWELL_OPENFILE"));
        contexts.add(new Context(175,"CTX_VQSAGSWELL_OPENSTATUS"));
        contexts.add(new Context(176,"CTX_VQSAGSWELL_LOADSTATUS"));
        contexts.add(new Context(177,"CTX_VQSAGSWELL_CREATESTATUS"));
        contexts.add(new Context(178,"CTX_VQSAGSWELL_RECORDEVENT"));
        contexts.add(new Context(179,"CTX_VQSAGSWELL_PROCESSEVENT"));
        contexts.add(new Context(180,"CTX_VQSAGSWELL_POSTSTATUS"));
        contexts.add(new Context(181,"CTX_VQIMBALANCE_OPENFILE"));
        contexts.add(new Context(182,"CTX_VQIMBALANCE_CREATEFILE"));
        contexts.add(new Context(183,"CTX_VQIMBALANCE_CREATESTATUS"));
        contexts.add(new Context(184,"CTX_VQIMBALANCE_OPENSTATUS"));
        contexts.add(new Context(185,"CTX_VQIMBALANCE_LOADSTATUS"));
        contexts.add(new Context(186,"CTX_VQIMBALANCE_RECORDIMBALANCE"));
        contexts.add(new Context(187,"CTX_VQIMBALANCE_POSTSTATUS"));
        contexts.add(new Context(188,"CTX_VQMAN_OPENSTATUS"));
        contexts.add(new Context(189,"CTX_VQMAN_LOADSTATUS"));
        contexts.add(new Context(190,"CTX_VQMAN_CREATESTATUS"));
        contexts.add(new Context(191,"CTX_VQMAN_POWERDOWN"));
        contexts.add(new Context(192,"CTX_VQMAN_RECONFIGURE"));
        contexts.add(new Context(193,"CTX_VQMAN_RESTART"));
        contexts.add(new Context(194,"CTX_VQMAN_SETACTIVE"));
        contexts.add(new Context(195,"CTX_VQMAN_POSTSTATUS"));
        contexts.add(new Context(196,"CTX_VQIMBALANCE_NEWRAWREGISTERS"));
        contexts.add(new Context(197,"CTX_VQSAGSWELL_NEWVQDATA"));
        contexts.add(new Context(198,"CTX_VFSFAT_ISTHEREENOUGHSPACE"));
        contexts.add(new Context(199,"CTX_VQCONFIG_RESTART"));
        contexts.add(new Context(200,"CTX_ENERGY_CLEARBILLING"));
        contexts.add(new Context(201,"CTX_TOUSUMMARYGROUP_LOADCURRENTRATE"));
        contexts.add(new Context(202,"CTX_WAITFORDSPBOOT"));
        contexts.add(new Context(203,"CTX_SYNCHTOSECONDCHANGE"));
        contexts.add(new Context(204,"CTX_CHECKFORFLASHWRITEPENDING"));
        contexts.add(new Context(205,"CTX_PDS_RESTART"));
        contexts.add(new Context(206,"CTX_PDS_RECONFIGURE"));
        contexts.add(new Context(207,"CTX_PDS_POWERUP"));
        contexts.add(new Context(208,"CTX_PDS_NEWSECOND"));
        contexts.add(new Context(209,"CTX_EMM_VIEWER"));
        contexts.add(new Context(210,"CTX_MK"));
        contexts.add(new Context(211,"CTX_MMF_REPLACERECORD"));
        contexts.add(new Context(212,"CTX_TOU_CHECKCONSISTENCY"));
        contexts.add(new Context(213,"CTX_TOU_POWERUP"));
        contexts.add(new Context(214,"CTX_TOU_RESTORE"));
        contexts.add(new Context(215,"CTX_TOU_RECOVERSTATUSFILE"));
        contexts.add(new Context(216,"CTX_TOU_CREATESTATUSFILE"));
        contexts.add(new Context(217,"CTX_TOU_POSTSTATUSFILE"));
        contexts.add(new Context(218,"CTX_GEOIS_LOGNEXTEOI"));
    }

    ResultCode resultCode; // Unsigned16,
    Context context; // Unsigned16,

    /** Creates a new instance of Result */
    public Result(byte[] data,int offset) throws IOException {
        resultCode = findResultCode(ProtocolUtils.getInt(data,offset, 2));
        offset+=2;
        context = findContext(ProtocolUtils.getInt(data,offset, 2));
        offset+=2;

    }

    static public int size() {
        return 4;
    }

    public String toString() {
        return "resultCode="+resultCode+", context="+context;
    }

    private ResultCode findResultCode(int id) throws IOException {
        Iterator it = resultCodes.iterator();
        while(it.hasNext()) {
            ResultCode rc = (ResultCode)it.next();
            if (rc.getId() == id)
                return rc;
        }
        throw new IOException("Result, invalid resultcode "+id);
    } // private ResultCode findResultCode(int id) throws IOException

    private Context findContext(int id) throws IOException {
        Iterator it = contexts.iterator();
        while(it.hasNext()) {
            Context c = (Context)it.next();
            if (c.getId() == id)
                return c;
        }
        throw new IOException("Result, invalid context "+id);
    } // private Context findContext(int id) throws IOException

}
