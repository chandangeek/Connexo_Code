package com.energyict.protocolimplv2.elster.ctr.MTU155.tariff;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.protocol.api.codetables.Code;

import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects.CodeObject;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.zip.GZIPOutputStream;


/**
 * Copyrights EnergyICT
 * Date: 29/03/11
 * Time: 10:19
 */
public class CodeTableBase64Builder {

    /**
     * @param codeTable the {@link Code} for which the XML string should be formed
     * @return
     */
    public static String getXmlStringFromCodeTable(Code codeTable) {
        return new String(getBase64FromCodeTable(codeTable)).replaceFirst("<[?]*(.*)[?]>", "");
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

            return Base64.getEncoder().encode(out.toByteArray());
        } catch (Exception e) {
            throw new ApplicationException("Unable to get xml from code table: " + e.getMessage(), e);
        }
    }
}
