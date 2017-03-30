/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * FileTransfer.java
 *
 * Created on 31 maart 2004, 16:31
 */

package com.energyict.protocolimpl.pact.core.common;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
/**
 *
 * @author  Koen
 */
public class FileTransfer {




    /** Creates a new instance of FileTransfer */
    public FileTransfer() {
        deleteFile();
    }

    /*
     * Deletes file if exist.
     * Creates File.
     */
    public void deleteFile() {
        File file = null;
        file = new File(getFileName());
        if (file.exists()) {
            file.delete();
        }
        file = new File(getDecryptedFileName());
        if (file.exists()) {
            file.delete();
        }
    }

    public void appendData(byte[] data) throws NestedIOException {
        try {
            File file = null;
            FileOutputStream fos=null;
            file = new File(getFileName());
            fos = new FileOutputStream(file,true);
            fos.write(data);
            fos.close();
        }
        catch(IOException e) {
            throw new NestedIOException(e);
        }
    } // public void appendData(byte[] data) throws NestedIOException

    public String getFileName() {
        return  hashCode()+".bin";
    } // public String getFileName()

    public String getDecryptedFileName() {
        return  getFileName()+"_decrypted";
    } // public String getDecryptedFileName()

    public byte[] getDecryptedData() throws NestedIOException {
        try {
            File file = new File(getDecryptedFileName());
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int)file.length()];
            fis.read(data);
            return data;
        }
        catch(IOException e) {
            throw new NestedIOException(e);
        }
    } // public byte[] getDecryptedData()

    public byte[] getDecryptedReadingsData() throws IOException {
        byte[] data = getDecryptedData();
        for (int i = 0; i<data.length; i+=8) {
            if (data[i] == '#')
                return ProtocolUtils.getSubArray(data,0,i+7);
        }
        return null;
    } // public byte[] getDecryptedReadingsData() throws IOException

    public byte[] getDecryptedSurveyData() throws IOException {
        byte[] data = getDecryptedData();
        for (int i = 0; i<data.length; i+=8) {
            if (data[i] == '#')
                return ProtocolUtils.getSubArray(data,i+8,data.length-1);
        }
        return null;
    } // public byte[] getDecryptedSurveyData() throws IOException

} // public class FileTransfer
