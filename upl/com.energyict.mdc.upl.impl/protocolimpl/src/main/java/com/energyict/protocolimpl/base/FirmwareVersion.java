/*
 * FirmwareVersion.java
 *
 * Created on 15 november 2005, 8:51
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.base;

import java.io.*;
import java.util.*;
import java.text.*;

import com.energyict.protocol.ProtocolUtils;


/**
 *
 * @author Koen
 */
public class FirmwareVersion {
    
    private int version=-1;
    static private final int MAX_REVISIONS=10;
    private int[] revisions=new int[MAX_REVISIONS];
    private String versionString;
    
    /** Creates a new instance of FirmwareVersion */
    public FirmwareVersion(String versionString) throws IOException {
        this.setVersionString(versionString);
        StringTokenizer strTok = new StringTokenizer(versionString,".");
        if (strTok.countTokens()>(MAX_REVISIONS+1))
            throw new IOException("FirmwareVersion, too many revisions in firmwareversion string (max allowed is "+MAX_REVISIONS+")!");
            
        if (strTok.countTokens() > 0) {
            setVersion(Integer.parseInt(strTok.nextToken()));
            if (strTok.countTokens() > 1) {
               int i=0;
               while(strTok.hasMoreTokens()) {
                   revisions[i++]=Integer.parseInt(strTok.nextToken());
               }
            }
        }
    }
    
    public String toString() {
        return getVersionString();
    }
    
    public boolean before(FirmwareVersion fw) {
        if (getVersion()<fw.getVersion()) return true;
        if (getVersion()==fw.getVersion()) {
            // check revisions
            for (int i=0;i<fw.getRevisions().length;i++) {
                if (getRevisions()[i]>fw.getRevisions()[i]) return false;
                else if (getRevisions()[i]<fw.getRevisions()[i]) return true;
            }
        }
        return false;
    }
    
    public boolean after(FirmwareVersion fw) {
        if (getVersion()>fw.getVersion()) return true;
        if (getVersion()==fw.getVersion()) {
            // check revisions
            for (int i=0;i<fw.getRevisions().length;i++) {
                if (getRevisions()[i]<fw.getRevisions()[i]) return false;
                else if (getRevisions()[i]>fw.getRevisions()[i]) return true;
            }
        }
        return false;
    }

    public boolean equal(FirmwareVersion fw) {
        if (getVersion()!=fw.getVersion()) return false;
        // check revisions
        for (int i=0;i<fw.getRevisions().length;i++) {
            if (getRevisions()[i]!=fw.getRevisions()[i]) return false;
        }
        return true;
    }
    
    public static void main(String[] args) {
        try {
            String version2check = "5.2";
            FirmwareVersion fw = new FirmwareVersion("5.2");
            System.out.println(fw.before(new FirmwareVersion(version2check)));
            System.out.println(fw.after(new FirmwareVersion(version2check)));
            System.out.println(fw.equal(new FirmwareVersion(version2check)));
        }
        catch(IOException e) {
            e.printStackTrace();            
        }
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int[] getRevisions() {
        return revisions;
    }

    public void setRevisions(int[] revisions) {
        this.revisions = revisions;
    }

    public String getVersionString() {
        return versionString;
    }

    public void setVersionString(String versionString) {
        this.versionString = versionString;
    }
    
}
