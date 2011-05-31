package com.energyict.genericprotocolimpl.elster.ctr.tariff;

import com.energyict.cbo.ApplicationException;
import com.energyict.genericprotocolimpl.elster.ctr.tariff.objects.CodeObject;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.protocolimpl.utils.ProtocolTools;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.util.zip.GZIPOutputStream;

/**
 * Copyrights EnergyICT
 * Date: 29/03/11
 * Time: 10:19
 */
public class CodeTableBase64Builder {

    /**
     * @param codeTableId
     * @return
     */
    public static String getXmlStringFromCodeTable(int codeTableId) {
        return new String(getBase64FromCodeTable(mw().getCodeFactory().find(codeTableId))).replaceFirst("<[?]*(.*)[?]>", "");
    }

    /**
     * @param codeTableId
     * @return
     */
    public static byte[] getBase64FromCodeTable(int codeTableId) {
        return getBase64FromCodeTable(mw().getCodeFactory().find(codeTableId));
    }

    /**
     * @param codeTable
     * @return
     */
    public static byte[] getBase64FromCodeTable(Code codeTable) {
        try {
            if (codeTable == null) {
                throw new ApplicationException("Code table not found: null");
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(out));
            oos.writeObject(CodeObject.fromCode(codeTable));
            oos.flush();
            oos.close();

            return new BASE64Encoder().encode(out.toByteArray()).getBytes();
        } catch (Exception e) {
            throw new ApplicationException("Unable to get xml from code table: " + e.getMessage(), e);
        }
    }

    private static MeteringWarehouse mw() {
        MeteringWarehouse mw = MeteringWarehouse.getCurrent();
        if (mw == null) {
            MeteringWarehouse.createBatchContext();
            mw = MeteringWarehouse.getCurrent();
        }
        return mw;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(getXmlStringFromCodeTable(1));
        ProtocolTools.writeBytesToFile("D:\\repositorysvn\\protocols\\branches\\8.9\\protocolimpl\\test.xml", getXmlStringFromCodeTable(1).getBytes(), false);
    }

}
