/*
 * testClass.java
 *
 * Created on 18 oktober 2005, 11:34
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12;

import com.energyict.protocolimpl.iec1107.abba1140.Calculate;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Calendar;
/**
 *
 * @author Koen
 */
public class TestClass {
    
    /** Creates a new instance of testClass */
    public TestClass() {
    }
    
    
    private void start1() {
       byte[] data  = {(byte)0x47,(byte)0x45,(byte)0x20,(byte)0x20,(byte)0x6b,(byte)0x56,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x05,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x32,(byte)0x31,(byte)0x32,(byte)0x38,(byte)0x38,(byte)0x37,(byte)0x38,(byte)0x37,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20}; //,(byte)0xf8
       int check = 0;
       for (int i=0;i<data.length;i++) {
           check += ((int)data[i]&0xFF);
       }    
       check ^= 0xFF;
       check += 1;
       check &= 0xFF;
       //checksum= ((checksum&0xFF)); //^0xFF);
       System.out.println(Integer.toHexString(check));
    }
    
    private void start2() {
        Calendar cal = ProtocolUtils.getCleanGMTCalendar();
        System.out.println(cal.getTime());
    }
    
    private void start3() {
        byte[] data={1,2,3,4,0,0,5,4};
        Number number = Calculate.convertIEEE32fp2NumberLE(data, 0);
        System.out.println(number);
        
        try {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data, 0,8));
            number = new BigDecimal((double)dis.readFloat());
            System.out.println(number);
            
            
            BigDecimal bd = new BigDecimal(new BigInteger("1234567"),4);
            System.out.println(bd);
            
            
            
            
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    private byte[] signExtend2Long(byte[] data, int offset, int length) {
        byte[] extendeddata = new byte[8];
        System.arraycopy(data,0,extendeddata,8-length,length);
        if ((data[0]&0x80)==0x80) {
            for (int i=0;i<(8-length);i++) {
                extendeddata[i]=(byte)0xFF;
            }
        }
        return extendeddata;
    }
    
    
    
    private void start4() {
        byte[] data={(byte)0x7F,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0x80,(byte)0xff};//,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xfe};
        try {
            //DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data, 0,5));
            //int val = dis.readInt();
            //long val = ProtocolUtils.getLong(signExtend2Long(data, 0, 4),0,8);
            long val = ProtocolUtils.getExtendedLong(data, 0, 2);
            System.out.println(val);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    private void start5() {
        BigDecimal bd = BigDecimal.valueOf(832);
        bd = bd.multiply(BigDecimal.valueOf(600));
        bd = bd.multiply(BigDecimal.valueOf(100));
        bd = bd.movePointLeft(9); // u -> k
        bd = bd.setScale(4, BigDecimal.ROUND_HALF_UP);
        System.out.println(bd);
    }
    
    private void start6() {
        String str = "A6A6A6A6";
        
        byte[] password=new byte[str.length()/2];
        byte[] data = new byte[2];
        for (int i=0;i<str.length()/2;i++) {
            data[0] = (byte)str.charAt(i*2);
            data[1] = (byte)str.charAt(i*2+1);
            password[i] = (byte)Integer.parseInt(new String(data),16);
        }
        System.out.println(ProtocolUtils.getResponseData(password));
        try {
        System.out.println(ProtocolUtils.getResponseData(ProtocolUtils.convert2ascii(str.getBytes())));
        }
        catch(IOException e) {
            
        }
        
    }
    private void start7() {
    
        //System.out.println(TimeZone.getTimeZone("PST").getRawOffset());
        
        double rawval = 14926370701312L;
        double scaler = 64000;
        
        double val = rawval*scaler*1E-6/(1E6*255);
        
        System.out.println(val);
        
        BigDecimal bd = new BigDecimal("3000.6789");
        bd = bd.setScale(0,BigDecimal.ROUND_HALF_UP);
        System.out.println(bd);
    }
    
    private int getProfileInterval() {
        return 900;
    }
    
    private void start8() {
        Calendar cal = Calendar.getInstance();
//        int protocolInterval = 900;
//        cal.set(Calendar.SECOND,1);
//        cal.set(Calendar.MINUTE,00);
//        if (((cal.getTime().getTime()/1000)%protocolInterval) != 0)
//            System.out.println("calendar = "+cal.getTime()+", is not on interval boundary, ERROR!!!");
//        else
//            System.out.println("calendar = "+cal.getTime()+", is on interval boundary");
//        
        
        int restMinutes = (getProfileInterval()/60) - (cal.get(Calendar.MINUTE)%(getProfileInterval()/60));        
        System.out.println(restMinutes);
    }
    
    private void start9() { 
        try {  
            
            // Create key specification with the password
            KeySpec keySpec = new DESKeySpec((new String("13726687")).getBytes());
            // Create key using DES provider
            SecretKey secretKey = SecretKeyFactory.getInstance("DES").generateSecret(keySpec);
            System.out.println("Key format: " + secretKey.getFormat());
            System.out.println("Key algorithm: " + secretKey.getAlgorithm());
            Cipher cipher = Cipher.getInstance("DES");
            System.out.println("Cipher provider: " + cipher.getProvider());
            System.out.println("Cipher algorithm: " + cipher.getAlgorithm());
            byte[] ticket = new byte[]{(byte)0xe1,(byte)0x4c,(byte)0xa5,(byte)0x7e,(byte)0xd9,(byte)0x13,(byte)0x01,(byte)0xdc};
            System.out.println("key="+ProtocolUtils.outputHexString(secretKey.getEncoded()));
            System.out.println("ticket data: " + ProtocolUtils.outputHexString(ticket));           
 
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] result = cipher.doFinal(ticket);
            System.out.println("Encrypted data: " + ProtocolUtils.outputHexString(result));
 
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] original = cipher.doFinal(result);
            System.out.println("Decrypted data: " + ProtocolUtils.outputHexString(original));
        
            
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedResult = cipher.doFinal(result);
            System.out.println("Encrypted encrypted data: " + ProtocolUtils.outputHexString(encryptedResult));
            
        }
        catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        catch(NoSuchPaddingException e) {
            e.printStackTrace();
        }
        catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        catch (IllegalStateException e) {
            e.printStackTrace();
        }
        catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        catch (BadPaddingException e) {
            e.printStackTrace();
        }
        catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        
    }    
    
    private void start10() {
        BigDecimal bd = new BigDecimal("10000");
        
        
        bd = bd.movePointLeft(4);
        System.out.println(bd);
        
        bd = bd.divide(BigDecimal.valueOf(1),BigDecimal.ROUND_HALF_UP);
        
        System.out.println(bd);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        TestClass tc = new TestClass();
        tc.start3();
    }
    
}
