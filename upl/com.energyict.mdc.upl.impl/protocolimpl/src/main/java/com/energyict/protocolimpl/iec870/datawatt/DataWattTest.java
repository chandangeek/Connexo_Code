/*
 * DataWatt.java
 *
 * Created on 18 juni 2003, 13:56
 */

package com.energyict.protocolimpl.iec870.datawatt;

import com.energyict.protocolimpl.iec870.IEC870Connection;
import com.energyict.protocolimpl.iec870.IEC870ConnectionException;
import com.energyict.protocolimpl.iec870.IEC870Frame;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.TimeZone;

/**
 *
 * @author  Koen
 */
public class DataWattTest {
    
    /** Creates a new instance of DataWatt */
    public DataWattTest() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        DataWattTest dw=new DataWattTest();
        dw.start();
    }
    
    private static final String fileName2="C:\\Documents and Settings\\koen\\My Documents\\meterprotocols\\DataWatt\\history.txt";
    private static final String fileNameWrite="C:\\Documents and Settings\\koen\\My Documents\\meterprotocols\\DataWatt\\historydecoded.txt";
    //byte[] data1 = {0x68,0x0c,0x0c,0x68,0x08,0x04,0x00,0x65,0x01,0x0a,0x2a,0x04,0x00,0x00,0x00,0x05,(byte)0xaf,0x16};
    //byte[] data2 = {0x68,0x21,0x21,0x68,0x28,0x04,0x00,0x01,0x08,0x14,0x29,0x04,0x00,0x05,0x00,0x00,0x06,0x00,0x00,0x07,0x00,0x00,0x08,0x00,0x00,0x09,0x00,0x00,0x0a,0x00,0x00,0x0b,0x00,0x00,0x0c,0x00,0x01,(byte)0xbb,0x16};
    //byte[] data3 = {(byte)0xE5};
    public void start() {
        try {
            IEC870Frame f;
            FileOutputStream fos = new FileOutputStream(fileNameWrite);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            
            /*f = new IEC870Frame(data1);
            System.out.println(f.toString(100));
            f = new IEC870Frame(data2);
            System.out.println(f.toString(101));
            f = new IEC870Frame(data3);
            System.out.println(f.toString(102));*/
            List frames = getFrames();
            for (int i=0;i<frames.size();i++) {
                byte[] data=null;
                try {
                    data = (byte[])frames.get(i);
                    f = new IEC870Frame(data);
                    
// *************** all data *****************                    
                    // print raw bytes...
                    //System.out.println(ProtocolUtils.getResponseData(data));
                    bw.write(ProtocolUtils.getResponseData(data)+"\r\n");
                    //System.out.print(f.toString(i)); // write to stdout
                    bw.write(f.toString(i,TimeZone.getTimeZone("GMT+1"))); // write to file
                    
// *************** sequence *****************
//                 System.out.println(f.getFrameInfo(i));
//                 bw.write(f.getFrameInfo(i)+"\r\n");
                    
                }
                catch(IEC870ConnectionException e) {
                    System.out.println("IEC870ConnectionException, "+e.getMessage());
                }
                catch(Exception e) { //IEC870ConnectionException e) {
                    //System.out.println(ProtocolUtils.getResponseData(rf.getData()));
                    System.out.println(ProtocolUtils.getResponseData(data));
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
            bw.close();
        }
        catch(IEC870ConnectionException e) {
            e.printStackTrace();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
    private List getFrames() throws IOException {
        List frames; // = new ArrayList();
        byte[] data = getByteArray();
        
        ByteArrayInputStream bai = new ByteArrayInputStream(data);
        IEC870Connection iec870Connection = new IEC870Connection(bai,null,0,0,0,0,TimeZone.getTimeZone("GMT+1"));
        frames = iec870Connection.parseFrames();
        
        return frames;
    }
    private int getValue(byte[] data,int offset) {
        StringBuffer strbuff = new StringBuffer();
        strbuff.append((char)data[offset]);
        strbuff.append((char)data[offset+1]);
        return Integer.parseInt(strbuff.toString(),16);
    }
    private byte[] getByteArray() throws IOException {
        try {
            File file = new File(fileName2);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] data = new byte[fis.available()];
            fis.read(data);
            
            for(int i=0;i<data.length;i+=2) {
                bos.write(getValue(data, i));
            }
            
            fis.close();
            return bos.toByteArray();
        }
        catch(FileNotFoundException e) {
            throw new IOException(e.getMessage());
        }
    }
}
