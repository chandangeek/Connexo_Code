/*
 * crcGenerator.java
 *
 * Created on 23 april 2003, 17:05
 */

package com.energyict.protocolimpl.base;

import com.energyict.protocolimpl.utils.ProtocolUtils;


/**
 *
 * @author  Koen
 */
public class CRCGenerator {
/*
   CCITT-32:   0x04C11DB7  =  x32 + x26 +  x23 + x22 + x16 + x12 +
                              x11 + x10 + x8 + x7 + x5 + x4 + x2 + x + 1
   CRC-16:     0x8005      =  x16 + x15 + x2 + 1
   CRC-CCITT:  0x1021      =  x16 + x12 + x5 + 1
   CRC-XMODEM: 0x8408      =  x16 + x15 + x10 + x3
   12bit-CRC:  0x80f       =  x12 + x11 + x3 + x2 + x + 1
   10bit-CRC:  0x233       =  x10 + x9  + x5  + x4  + x  + 1
   8bit-CRC:   0x07        =  x8  + x2  + x + 1
 */
    
    
//static private final byte[] REVERSE={(byte)0x0,(byte)0x80,(byte)0x40,(byte)0xc0,(byte)0x20,(byte)0xa0,(byte)0x60,(byte)0xe0,
//(byte)0x10,(byte)0x90,(byte)0x50,(byte)0xd0,(byte)0x30,(byte)0xb0,(byte)0x70,(byte)0xf0,
//(byte)0x8,(byte)0x88,(byte)0x48,(byte)0xc8,(byte)0x28,(byte)0xa8,(byte)0x68,(byte)0xe8,
//(byte)0x18,(byte)0x98,(byte)0x58,(byte)0xd8,(byte)0x38,(byte)0xb8,(byte)0x78,(byte)0xf8,
//(byte)0x4,(byte)0x84,(byte)0x44,(byte)0xc4,(byte)0x24,(byte)0xa4,(byte)0x64,(byte)0xe4,
//(byte)0x14,(byte)0x94,(byte)0x54,(byte)0xd4,(byte)0x34,(byte)0xb4,(byte)0x74,(byte)0xf4,
//(byte)0xc,(byte)0x8c,(byte)0x4c,(byte)0xcc,(byte)0x2c,(byte)0xac,(byte)0x6c,(byte)0xec,
//(byte)0x1c,(byte)0x9c,(byte)0x5c,(byte)0xdc,(byte)0x3c,(byte)0xbc,(byte)0x7c,(byte)0xfc,
//(byte)0x2,(byte)0x82,(byte)0x42,(byte)0xc2,(byte)0x22,(byte)0xa2,(byte)0x62,(byte)0xe2,
//(byte)0x12,(byte)0x92,(byte)0x52,(byte)0xd2,(byte)0x32,(byte)0xb2,(byte)0x72,(byte)0xf2,
//(byte)0xa,(byte)0x8a,(byte)0x4a,(byte)0xca,(byte)0x2a,(byte)0xaa,(byte)0x6a,(byte)0xea,
//(byte)0x1a,(byte)0x9a,(byte)0x5a,(byte)0xda,(byte)0x3a,(byte)0xba,(byte)0x7a,(byte)0xfa,
//(byte)0x6,(byte)0x86,(byte)0x46,(byte)0xc6,(byte)0x26,(byte)0xa6,(byte)0x66,(byte)0xe6,
//(byte)0x16,(byte)0x96,(byte)0x56,(byte)0xd6,(byte)0x36,(byte)0xb6,(byte)0x76,(byte)0xf6,
//(byte)0xe,(byte)0x8e,(byte)0x4e,(byte)0xce,(byte)0x2e,(byte)0xae,(byte)0x6e,(byte)0xee,
//(byte)0x1e,(byte)0x9e,(byte)0x5e,(byte)0xde,(byte)0x3e,(byte)0xbe,(byte)0x7e,(byte)0xfe,
//(byte)0x1,(byte)0x81,(byte)0x41,(byte)0xc1,(byte)0x21,(byte)0xa1,(byte)0x61,(byte)0xe1,
//(byte)0x11,(byte)0x91,(byte)0x51,(byte)0xd1,(byte)0x31,(byte)0xb1,(byte)0x71,(byte)0xf1,
//(byte)0x9,(byte)0x89,(byte)0x49,(byte)0xc9,(byte)0x29,(byte)0xa9,(byte)0x69,(byte)0xe9,
//(byte)0x19,(byte)0x99,(byte)0x59,(byte)0xd9,(byte)0x39,(byte)0xb9,(byte)0x79,(byte)0xf9,
//(byte)0x5,(byte)0x85,(byte)0x45,(byte)0xc5,(byte)0x25,(byte)0xa5,(byte)0x65,(byte)0xe5,
//(byte)0x15,(byte)0x95,(byte)0x55,(byte)0xd5,(byte)0x35,(byte)0xb5,(byte)0x75,(byte)0xf5,
//(byte)0xd,(byte)0x8d,(byte)0x4d,(byte)0xcd,(byte)0x2d,(byte)0xad,(byte)0x6d,(byte)0xed,
//(byte)0x1d,(byte)0x9d,(byte)0x5d,(byte)0xdd,(byte)0x3d,(byte)0xbd,(byte)0x7d,(byte)0xfd,
//(byte)0x3,(byte)0x83,(byte)0x43,(byte)0xc3,(byte)0x23,(byte)0xa3,(byte)0x63,(byte)0xe3,
//(byte)0x13,(byte)0x93,(byte)0x53,(byte)0xd3,(byte)0x33,(byte)0xb3,(byte)0x73,(byte)0xf3,
//(byte)0xb,(byte)0x8b,(byte)0x4b,(byte)0xcb,(byte)0x2b,(byte)0xab,(byte)0x6b,(byte)0xeb,
//(byte)0x1b,(byte)0x9b,(byte)0x5b,(byte)0xdb,(byte)0x3b,(byte)0xbb,(byte)0x7b,(byte)0xfb,
//(byte)0x7,(byte)0x87,(byte)0x47,(byte)0xc7,(byte)0x27,(byte)0xa7,(byte)0x67,(byte)0xe7,
//(byte)0x17,(byte)0x97,(byte)0x57,(byte)0xd7,(byte)0x37,(byte)0xb7,(byte)0x77,(byte)0xf7,
//(byte)0xf,(byte)0x8f,(byte)0x4f,(byte)0xcf,(byte)0x2f,(byte)0xaf,(byte)0x6f,(byte)0xef,
//(byte)0x1f,(byte)0x9f,(byte)0x5f,(byte)0xdf,(byte)0x3f,(byte)0xbf,(byte)0x7f,(byte)0xff};    
    
/* CRC16 Table:8005 */
static private final int[] CRC16={
 0x0000, 0x8005, 0x800F, 0x000A, 0x801B, 0x001E, 0x0014, 0x8011,
 0x8033, 0x0036, 0x003C, 0x8039, 0x0028, 0x802D, 0x8027, 0x0022,
 0x8063, 0x0066, 0x006C, 0x8069, 0x0078, 0x807D, 0x8077, 0x0072,
 0x0050, 0x8055, 0x805F, 0x005A, 0x804B, 0x004E, 0x0044, 0x8041,
 0x80C3, 0x00C6, 0x00CC, 0x80C9, 0x00D8, 0x80DD, 0x80D7, 0x00D2,
 0x00F0, 0x80F5, 0x80FF, 0x00FA, 0x80EB, 0x00EE, 0x00E4, 0x80E1,
 0x00A0, 0x80A5, 0x80AF, 0x00AA, 0x80BB, 0x00BE, 0x00B4, 0x80B1,
 0x8093, 0x0096, 0x009C, 0x8099, 0x0088, 0x808D, 0x8087, 0x0082,
 0x8183, 0x0186, 0x018C, 0x8189, 0x0198, 0x819D, 0x8197, 0x0192,
 0x01B0, 0x81B5, 0x81BF, 0x01BA, 0x81AB, 0x01AE, 0x01A4, 0x81A1,
 0x01E0, 0x81E5, 0x81EF, 0x01EA, 0x81FB, 0x01FE, 0x01F4, 0x81F1,
 0x81D3, 0x01D6, 0x01DC, 0x81D9, 0x01C8, 0x81CD, 0x81C7, 0x01C2,
 0x0140, 0x8145, 0x814F, 0x014A, 0x815B, 0x015E, 0x0154, 0x8151,
 0x8173, 0x0176, 0x017C, 0x8179, 0x0168, 0x816D, 0x8167, 0x0162,
 0x8123, 0x0126, 0x012C, 0x8129, 0x0138, 0x813D, 0x8137, 0x0132,
 0x0110, 0x8115, 0x811F, 0x011A, 0x810B, 0x010E, 0x0104, 0x8101,
 0x8303, 0x0306, 0x030C, 0x8309, 0x0318, 0x831D, 0x8317, 0x0312,
 0x0330, 0x8335, 0x833F, 0x033A, 0x832B, 0x032E, 0x0324, 0x8321,
 0x0360, 0x8365, 0x836F, 0x036A, 0x837B, 0x037E, 0x0374, 0x8371,
 0x8353, 0x0356, 0x035C, 0x8359, 0x0348, 0x834D, 0x8347, 0x0342,
 0x03C0, 0x83C5, 0x83CF, 0x03CA, 0x83DB, 0x03DE, 0x03D4, 0x83D1,
 0x83F3, 0x03F6, 0x03FC, 0x83F9, 0x03E8, 0x83ED, 0x83E7, 0x03E2,
 0x83A3, 0x03A6, 0x03AC, 0x83A9, 0x03B8, 0x83BD, 0x83B7, 0x03B2,
 0x0390, 0x8395, 0x839F, 0x039A, 0x838B, 0x038E, 0x0384, 0x8381,
 0x0280, 0x8285, 0x828F, 0x028A, 0x829B, 0x029E, 0x0294, 0x8291,
 0x82B3, 0x02B6, 0x02BC, 0x82B9, 0x02A8, 0x82AD, 0x82A7, 0x02A2,
 0x82E3, 0x02E6, 0x02EC, 0x82E9, 0x02F8, 0x82FD, 0x82F7, 0x02F2,
 0x02D0, 0x82D5, 0x82DF, 0x02DA, 0x82CB, 0x02CE, 0x02C4, 0x82C1,
 0x8243, 0x0246, 0x024C, 0x8249, 0x0258, 0x825D, 0x8257, 0x0252,
 0x0270, 0x8275, 0x827F, 0x027A, 0x826B, 0x026E, 0x0264, 0x8261,
 0x0220, 0x8225, 0x822F, 0x022A, 0x823B, 0x023E, 0x0234, 0x8231,
 0x8213, 0x0216, 0x021C, 0x8219, 0x0208, 0x820D, 0x8207, 0x0202};
    
    
    static private final int[] HDLC_CRC_TABLE={
        0x0000, 0x1189, 0x2312, 0x329b, 0x4624, 0x57ad, 0x6536, 0x74bf,
                0x8c48, 0x9dc1, 0xaf5a, 0xbed3, 0xca6c, 0xdbe5, 0xe97e, 0xf8f7,
                0x1081, 0x0108, 0x3393, 0x221a, 0x56a5, 0x472c, 0x75b7, 0x643e,
                0x9cc9, 0x8d40, 0xbfdb, 0xae52, 0xdaed, 0xcb64, 0xf9ff, 0xe876,
                0x2102, 0x308b, 0x0210, 0x1399, 0x6726, 0x76af, 0x4434, 0x55bd,
                0xad4a, 0xbcc3, 0x8e58, 0x9fd1, 0xeb6e, 0xfae7, 0xc87c, 0xd9f5,
                0x3183, 0x200a, 0x1291, 0x0318, 0x77a7, 0x662e, 0x54b5, 0x453c,
                0xbdcb, 0xac42, 0x9ed9, 0x8f50, 0xfbef, 0xea66, 0xd8fd, 0xc974,
                0x4204, 0x538d, 0x6116, 0x709f, 0x0420, 0x15a9, 0x2732, 0x36bb,
                0xce4c, 0xdfc5, 0xed5e, 0xfcd7, 0x8868, 0x99e1, 0xab7a, 0xbaf3,
                0x5285, 0x430c, 0x7197, 0x601e, 0x14a1, 0x0528, 0x37b3, 0x263a,
                0xdecd, 0xcf44, 0xfddf, 0xec56, 0x98e9, 0x8960, 0xbbfb, 0xaa72,
                0x6306, 0x728f, 0x4014, 0x519d, 0x2522, 0x34ab, 0x0630, 0x17b9,
                0xef4e, 0xfec7, 0xcc5c, 0xddd5, 0xa96a, 0xb8e3, 0x8a78, 0x9bf1,
                0x7387, 0x620e, 0x5095, 0x411c, 0x35a3, 0x242a, 0x16b1, 0x0738,
                0xffcf, 0xee46, 0xdcdd, 0xcd54, 0xb9eb, 0xa862, 0x9af9, 0x8b70,
                0x8408, 0x9581, 0xa71a, 0xb693, 0xc22c, 0xd3a5, 0xe13e, 0xf0b7,
                0x0840, 0x19c9, 0x2b52, 0x3adb, 0x4e64, 0x5fed, 0x6d76, 0x7cff,
                0x9489, 0x8500, 0xb79b, 0xa612, 0xd2ad, 0xc324, 0xf1bf, 0xe036,
                0x18c1, 0x0948, 0x3bd3, 0x2a5a, 0x5ee5, 0x4f6c, 0x7df7, 0x6c7e,
                0xa50a, 0xb483, 0x8618, 0x9791, 0xe32e, 0xf2a7, 0xc03c, 0xd1b5,
                0x2942, 0x38cb, 0x0a50, 0x1bd9, 0x6f66, 0x7eef, 0x4c74, 0x5dfd,
                0xb58b, 0xa402, 0x9699, 0x8710, 0xf3af, 0xe226, 0xd0bd, 0xc134,
                0x39c3, 0x284a, 0x1ad1, 0x0b58, 0x7fe7, 0x6e6e, 0x5cf5, 0x4d7c,
                0xc60c, 0xd785, 0xe51e, 0xf497, 0x8028, 0x91a1, 0xa33a, 0xb2b3,
                0x4a44, 0x5bcd, 0x6956, 0x78df, 0x0c60, 0x1de9, 0x2f72, 0x3efb,
                0xd68d, 0xc704, 0xf59f, 0xe416, 0x90a9, 0x8120, 0xb3bb, 0xa232,
                0x5ac5, 0x4b4c, 0x79d7, 0x685e, 0x1ce1, 0x0d68, 0x3ff3, 0x2e7a,
                0xe70e, 0xf687, 0xc41c, 0xd595, 0xa12a, 0xb0a3, 0x8238, 0x93b1,
                0x6b46, 0x7acf, 0x4854, 0x59dd, 0x2d62, 0x3ceb, 0x0e70, 0x1ff9,
                0xf78f, 0xe606, 0xd49d, 0xc514, 0xb1ab, 0xa022, 0x92b9, 0x8330,
                0x7bc7, 0x6a4e, 0x58d5, 0x495c, 0x3de3, 0x2c6a, 0x1ef1, 0x0f78
    };
    
    
    
    
    static final int MAX_CRC_TABLE_SIZE=256;
    
    static int[] crcTable=genTable();
    
    /** Creates a new instance of crcGenerator */
    public CRCGenerator() {
    }
    
    static int[] genTable() {
        return genTable(0xA001);
    }
    
    static int[] genTable(int polynome) {
        int[] crcTable = new int[MAX_CRC_TABLE_SIZE];
        int inx,entry16,carry16,i;
        for (inx=0;inx<MAX_CRC_TABLE_SIZE;inx++) {
            entry16=inx;
            for (i=0;i<8;i++) {
                carry16=entry16 & 1;
                entry16=entry16 >> 1;
                if (carry16 != 0) {
                    entry16 = entry16 ^ polynome; //0xA001;
                }
                crcTable[inx] = entry16;
                //System.out.println("inx="+inx+", i="+i+", entry16="+Integer.toHexString(entry16));
            }
        }
        return crcTable;
    }
    
    static public int calcCRC16(byte[] buf){
        return calcCRC16(buf, buf.length);
    }
    
    static public int calcCRC16(byte[] buf,int size){
        int i;
        int crc = 0xFFFF;

        crc = 0xFFFF;       // Inital CRC should be set to 0xFFFF 
        for ( i = 0; i < size; i++ ) {
            int val = (int)buf[i] & 0xFF;
            crc = ((crc << 8) ^ CRC16[ ((crc >> 8) ^ val) & 0xff ]) &0xffff; // Forward
        }

        return ( crc );
    }

    
    /**
     * CRC calculation using HDLC CCITT CRC standard polynomial X16+X12+X5+1
     * Table is generated
     */
    static public int calcHDLCCRC(byte[] byteBuffer) {
        return calcHDLCCRC(byteBuffer,byteBuffer.length);
    }
    static public int calcHDLCCRC(byte[] byteBuffer,int length) {
        int iCharVal,i;
        
        int crc=0x0000FFFF;
        for (i=0; i<length; i++) {
            iCharVal = (int)byteBuffer[i] & 0x000000FF;
            crc = (crc>>8)^HDLC_CRC_TABLE[(crc^iCharVal) & 0x000000FF] ;
            crc &= 0x0000FFFF;
        }
        
        crc^=0x0000FFFF;
        i = crc;
        crc = (crc >> 8) & 0x000000FF;
        crc = crc | ((i<<8) & 0x0000FF00);
        
        return (crc);
        
    } // private unsigned short CalcHDLCCRC()
    
    static public int calcCRC(byte[] data,int iLength) {
        return (docalcCRC(data,iLength,true));
    }
    static public int calcCRC(byte[] data) {
        return (docalcCRC(data,data.length-2,true));
    }
    /*
     *  Calculate CRC on the complete data and return the CRC
     */
    static public int calcCRCFull(byte[] data) {
        return (docalcCRC(data,data.length,false));
    }
    static private int docalcCRC(byte[] ptr, int count, boolean check) {
        int inx16,crc16=0;
        int i=0;
        while(count>0) {
            inx16 = (ptr[i] ^ crc16) & 0x00FF;
            crc16 >>= 8;
            crc16 ^= crcTable[inx16];
            i++;
            count--;
        }
        
        if (check) {
            int receivedcrc16 = (int)ProtocolUtils.getShortLE(ptr, ptr.length-2)&0xFFFF;
            return (crc16 - receivedcrc16);
        } else return crc16;
    }
    
    
    static public int calcCRCModbus(byte[] data) {
        int crc=0xFFFF;
        
        for (int i=0;i<data.length;i++) {
            int dataByte = (int)data[i]&0xFF;
            int j;
            int bitCount;
            for (bitCount=0;bitCount<8;dataByte >>= 1,bitCount++) {
                j=(dataByte^crc) & 1;
                crc >>= 1;
                if (j==1)
                    crc ^= 0xA001;
            }
        }
        return crc;
    }

    static public int calcCRCDirect(byte[] data) {
        return calcCRCDirect(data, 0xA001);
    }
    
    static public int calcCRCDirect(byte[] data, int polynome) {
        int crc=0;
        
        for (int i=0;i<data.length;i++) {
            int dataByte = (int)data[i]&0xFF;
            int j;
            int bitCount;
            for (bitCount=0;bitCount<8;dataByte >>= 1,bitCount++) {
                j=(dataByte^crc) & 1;
                crc >>= 1;
                if (j==1)
                    crc ^= polynome;
            }
        }
        return crc;
    }
    
    static public boolean isCRCAlphaValid(byte[] data) {
        int crcorg = ((int)data[data.length-2]&0xff)*256+((int)data[data.length-1]&0xff);
        int crc = calcCRCAlpha(data,data.length-2);
        return crcorg == crc;
    }
    
    
    static public int ccittCRC(byte[] data) {
        return calcCRCAlpha(data);
    }
    static public int calcCRCAlpha(byte[] data) {
        return calcCRCAlpha(data, data.length);
    }
    static public int ccittCRC(byte[] data,int len) {
        return calcCRCAlpha(data, len);
    }
    static public int calcCRCAlpha(byte[] data,int len) {
        int crc = 0;
        int olddata;
        for (int t=0;t<len;t++ ) {
            int val = ((int)data[t]&0xff);
            crc^=(val << 8 &0xFFFF);
            for (int i=0;i<8;i++) {
                olddata = crc & 0x8000;
                crc = (crc << 1)&0xffff;
                if (olddata != 0)
                    crc^=0x1021; // CRC-CCITT polynom
            }
        }
        return crc;
    }
    
    static public int calcCRCCCITTSchlumberger(byte[] data) {
        return calcCRCCCITT(data, 0xffff);
    }
    
    static public int calcCRCCCITTEnermet(byte[] data) {
        return calcCRCCCITT(data, 0x1D0F);
    }
    
    static public int calcCRCCCITT(byte[] data, int startValue) {
        int crcmsb,crclsb,scratch,acc;
        crcmsb=(startValue>>8)&0xFF;
        crclsb=startValue&0xFF;
        for (int t=0;t<data.length;t++ ) {
            acc = ((int)data[t]&0xff); // a
            acc ^= crcmsb;             // b
            scratch = acc;             // c
            acc >>= 4;                 // d
            acc ^= scratch;            // e
            scratch = acc;             // f
            acc <<= 4; acc &= 0xFF;    // g
            acc ^= crclsb;             // h
            crcmsb = acc;              // i
            acc = scratch;             // j
            acc >>= 3;                 // k
            acc ^= crcmsb;             // l
            crcmsb = acc;              // m
            acc = scratch;             // n
            acc <<= 5; acc &= 0xFF;    // o
            acc ^= scratch;            // p
            crclsb = acc;              // q
        }
        return crcmsb<<8|crclsb;
    }
    
    static public int calcCRCSentry(byte[] data) {
        int crc = 0x0000;
        for (int ri = 0; ri < data.length; ri++) {
            byte c = data[ri];
            for (int i = 0; i < 8; i++) {
                boolean c15 = ((crc >> 15 & 1) == 1);
                boolean bit = ((c >> (7 - i) & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit)
                    crc ^= 0x8005; // CRC-16 polynom
            }
        }
        return crc&0xffff;
    }
    
    static public int calcCCITTCRC(byte[] data) {
        int crc = 0x0000;
        for (int ri = 0; ri < data.length; ri++) {
            byte c = data[ri];
            for (int i = 0; i < 8; i++) {
                boolean c15 = ((crc >> 15 & 1) == 1);
                boolean bit = ((c >> (7 - i) & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit)
                   crc ^= 0x1021; // CRC-16 polynom
            }
        }
        return crc&0xffff;
    }
    

    static final int POLY_CCITT=0x8408;   /* 1021 in reverse order */
    static public int calcCCITTCRCReverse(byte[] data) { 
	int  crc = 0;
	int   j;
	int  c, Q;
        
        for (int i=0;i<data.length;i++) {
            c = (int)(data[i])&0xff;
            for (j = 0; j < 8 ; j++) {
                Q = ((crc & 0x0001) ^ (c & 0x0001));
                crc >>= 1;
                if (Q == 0x0001) 
                        crc = (crc ^ POLY_CCITT)&0xffff;
                c = (c >> 1);
            }
	}
	return (((crc&0xff)<<8) + (crc>>8));
    }
    
    
//    static public int calcCCITTCRCReverse2(byte[] data) {
//        int crc = 0x0000;
//        for (int ri = 0; ri < data.length; ri++) {
//            byte c = REVERSE[(int)data[ri]&0xff];
//            for (int i = 0; i < 8; i++) {
//                boolean c15 = ((crc >> 15 & 1) == 1);
//                boolean bit = ((c >> (7 - i) & 1) == 1);
//                crc <<= 1;
//                if (c15 ^ bit)
//                    crc ^= 0x1021; // CRC-16 polynom
//            }
//        }
//        
//        crc = crc&0xffff;
//        return (((int)REVERSE[crc/256]&0xff)<<8) + ((int)REVERSE[crc&0xff]&0xff);
//    }
    
    public static void main(String[] args) {

        //byte[] data = new byte[]{(byte)0x00,(byte)0x98,(byte)0x80,(byte)0x66};
        //byte[] data = new byte[]{(byte)0x00,(byte)0xe0}; //,(byte)0x0e,(byte)0xe7};
        //byte[] data = new byte[]{(byte)0x7b,(byte)0xe0,(byte)0xa0,(byte)0x02,(byte)0x01,(byte)0x82,(byte)0x02,(byte)0x0c,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x11,(byte)0x04,(byte)0x28,(byte)0x0e,(byte)0x21,(byte)0x10,(byte)0x00,(byte)0x00,(byte)0x10,(byte)0x00,(byte)0x05,(byte)0x04,(byte)0x28,(byte)0xb8,(byte)0x21,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x10,(byte)0x00,(byte)0x0a,(byte)0x10,(byte)0x00,(byte)0xc8,(byte)0x01,(byte)0x08,(byte)0x10,(byte)0x05,(byte)0x0a,(byte)0x10,(byte)0x05,(byte)0x0a,(byte)0x10,(byte)0x05,(byte)0x0a,(byte)0x10,(byte)0x00,(byte)0x00,(byte)0x10,(byte)0x00,(byte)0x00,(byte)0x10,(byte)0x05,(byte)0x0a,(byte)0x10,(byte)0x05,(byte)0x0a,(byte)0x10,(byte)0x00,(byte)0x00,(byte)0x10,(byte)0x03,(byte)0xe8,(byte)0x0f,(byte)0x00,(byte)0x10,(byte)0x00,(byte)0x00,(byte)0x03,(byte)0x01,(byte)0x0f,(byte)0x02,(byte)0x01,(byte)0x0f,(byte)0x0f,(byte)0x06,(byte)0x0f,(byte)0x09,(byte)0x0f,(byte)0x0b,(byte)0x0f,(byte)0x12,(byte)0x0f,(byte)0x14,(byte)0x0f,(byte)0x16,(byte)0x0f,(byte)0x02,(byte)0x0f,(byte)0x02,(byte)0x0f,(byte)0x02,(byte)0x0f,(byte)0x02,(byte)0x0f,(byte)0x02,(byte)0x0f,(byte)0x02,(byte)0x0f,(byte)0x02,(byte)0x0f,(byte)0x02,(byte)0x0f,(byte)0x02,(byte)0x01,(byte)0x0f,(byte)0x0f,(byte)0x00,(byte)0x0f,(byte)0x00,(byte)0x0f,(byte)0x00,(byte)0x0f,(byte)0x00,(byte)0x0f,(byte)0x00,(byte)0x0f,(byte)0x00,(byte)0x0f,(byte)0x00,(byte)0x0f,(byte)0x00,(byte)0x0f,(byte)0x00}; //,(byte)0xd6,(byte)0xc3};
        
        
//byte[] data = new byte[]{(byte)0x7B,(byte)0xEF,(byte)0xA0,(byte)0x02,(byte)0x55,(byte)0x10,(byte)0x01,(byte)0x42,(byte)0x10,(byte)0xE0,(byte)0x60,(byte)0x10,(byte)0x01,(byte)0x48,(byte)0x10,(byte)0x01,(byte)0x39,(byte)0x10,(byte)0x01,(byte)0x49,(byte)0x10,(byte)0x01,(byte)0x49,(byte)0x10,(byte)0x01,(byte)0x3A,(byte)0x10,(byte)0x01,(byte)0x68,(byte)0x10,(byte)0xE0,(byte)0x70,(byte)0x10,(byte)0x01,(byte)0x70,(byte)0x10,(byte)0x01,(byte)0x6C,(byte)0x10,(byte)0x01,(byte)0x5E,(byte)0x10,(byte)0x01,(byte)0x57,(byte)0x10,(byte)0x01,(byte)0x43,(byte)0x10,(byte)0x01,(byte)0x40,(byte)0x10,(byte)0xE0,(byte)0x80,(byte)0x10,(byte)0x01,(byte)0x4C,(byte)0x10,(byte)0x01,(byte)0x53,(byte)0x10,(byte)0x01,(byte)0x54,(byte)0x10,(byte)0x01,(byte)0x42,(byte)0x10,(byte)0x01,(byte)0x32,(byte)0x10,(byte)0x01,(byte)0x25,(byte)0x10,(byte)0xE0,(byte)0x90,(byte)0x10,(byte)0x01,(byte)0x1E,(byte)0x10,(byte)0x01,(byte)0x20,(byte)0x10,(byte)0x01,(byte)0x39,(byte)0x10,(byte)0x01,(byte)0x2D,(byte)0x10,(byte)0x01,(byte)0x4F,(byte)0x10,(byte)0x01,(byte)0x4F,(byte)0x10,(byte)0xE0,(byte)0xA0,(byte)0x10,(byte)0x01,(byte)0x49,(byte)0x10,(byte)0x01,(byte)0x57,(byte)0x10,(byte)0x01,(byte)0x5D,(byte)0x10,(byte)0x01,(byte)0x4A,(byte)0x10,(byte)0x01,(byte)0x4E,(byte)0x10,(byte)0x01,(byte)0x40,(byte)0x10,(byte)0xE0,(byte)0xB0,(byte)0x10,(byte)0x01,(byte)0x46,(byte)0x10,(byte)0x01,(byte)0x4F,(byte)0x10,(byte)0x01,(byte)0x45}; //,(byte)0x6A,(byte)0xA3};
//byte[] data = new byte[]{(byte)0x7B,(byte)0xE3,(byte)0xA0,(byte)0x02,(byte)0x10,(byte)0x01,(byte)0x51,(byte)0x10,(byte)0x01,(byte)0x66,(byte)0x10,(byte)0x01,(byte)0x4E,(byte)0x10,(byte)0xE0,(byte)0xC0,(byte)0x10,(byte)0x01,(byte)0x54,(byte)0x10,(byte)0x01,(byte)0x52,(byte)0x10,(byte)0x01,(byte)0x50,(byte)0x10,(byte)0x01,(byte)0x5E,(byte)0x10,(byte)0x01,(byte)0x4F,(byte)0x10,(byte)0x01,(byte)0x5B,(byte)0x10,(byte)0xE0,(byte)0xD0,(byte)0x10,(byte)0x01,(byte)0x55,(byte)0x10,(byte)0x01,(byte)0x54,(byte)0x10,(byte)0x01,(byte)0x55,(byte)0x10,(byte)0x01,(byte)0x5B,(byte)0x10,(byte)0x01,(byte)0x60,(byte)0x10,(byte)0x01,(byte)0x54,(byte)0x10,(byte)0xE0,(byte)0xE0,(byte)0x10,(byte)0x11,(byte)0x46,(byte)0x24,(byte)0x05,(byte)0x7A,(byte)0x81,(byte)0x14,(byte)0x68,(byte)0x16,(byte)0xA2,(byte)0x40,(byte)0xB9,(byte)0x10,(byte)0x01,(byte)0x67,(byte)0x10,(byte)0x01,(byte)0x5E,(byte)0x10,(byte)0xE0,(byte)0xF0,(byte)0x10,(byte)0x01,(byte)0x4E,(byte)0x10,(byte)0x01,(byte)0x51,(byte)0x10,(byte)0x01,(byte)0x5E,(byte)0x10,(byte)0x01,(byte)0x60,(byte)0x10,(byte)0x01,(byte)0x54,(byte)0x10,(byte)0x01,(byte)0x4C,(byte)0x10,(byte)0xE1,(byte)0x00,(byte)0x10,(byte)0x01,(byte)0x5F,(byte)0x10,(byte)0x01,(byte)0x58,(byte)0x10,(byte)0x01,(byte)0x6C,(byte)0x10,(byte)0x01,(byte)0x66,(byte)0x10,(byte)0x01,(byte)0x62,(byte)0x10,(byte)0x01,(byte)0x57,(byte)0x10,(byte)0xE1,(byte)0x10,(byte)0x10,(byte)0x01,(byte)0x4B}; //,(byte)0x10,(byte)0xC0};
        //byte[] data = new byte[]{(byte)0x03,(byte)0x55,(byte)0xC9,(byte)0x52,(byte)0x48,(byte)0x50,(byte)0xF6};
        //byte[] data = new byte[]{(byte)0x02,(byte)0xE3,(byte)0xB0,(byte)0x02,(byte)0xCF,(byte)0xC6};
        
        //System.out.println(Integer.toHexString(CRCGenerator.calcCCITTCRCReverse(data)));
        
        //byte[] data = new byte[]{(byte)0x44,(byte)0x00,(byte)0x21,(byte)0x11,(byte)0x00,(byte)0x21,(byte)0x11}; //,(byte)0x15,(byte)0xd5};
        byte[] data = new byte[]{(byte)0x58,(byte)0x08,(byte)0x59,(byte)0xf7};
        System.out.println(Integer.toHexString(CRCGenerator.calcCRCCCITTSchlumberger(data)));
        
        data = new byte[]{(byte)0x02,(byte)0x08,(byte)0x10,(byte)0xf5,(byte)0xf8};
        System.out.println(CRCGenerator.isCRCAlphaValid(data));
        
//        for (int i = 0;i<0x100;i++) {
//        int val=0;    
//            for (int t=0;t<8;t++) {
//              if ((i&(0x01<<t)) != 0) {
//                  val |= (0x80 >> t);
//              }   
//                
//            }
//            if ((i%8)==0) System.out.println();
//            System.out.print("(byte)0x"+Integer.toHexString(val)+",");
//            
//        }
        
    }

    public static int getModulo256(byte[] rawData) {
        int result = 0;
        for (byte b : rawData) {
            result += b & 0xFF;
            result %= 256;
        }
        return result & 0x0FF;
    }
}
