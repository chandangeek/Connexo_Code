package com.energyict.protocolimpl.iec1107.abba1140;

import com.energyict.protocolimpl.base.Encryptor;

public class CAI700 implements Encryptor {

    private static final int DEBUG=0;

    static char[] C245hgj={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

    public CAI700(){}

    public char[] CAIMETHOD(String CA27,String CA28) {

        char[] CA29 = new char[16];

        if (DEBUG >= 1) {
           System.out.println("pw: "+CA27);
           System.out.println("key: "+CA28);
        }
        char[]CfG7821=new char[9];
        char[] K8057hwf=new char[9];
        char[] LI6348=new char[17];
        char CFG873,CFG837;
        for(int i=0;i<8;i++) {
            CfG7821[i]=CA27.charAt(i);
    }

    CfG7821[8]='\0';
    for (int i=0;i<16;i+=2) {
        CFG873=CAIMETHODB(CA28.charAt(i));
        CFG837=CAIMETHODB(CA28.charAt(i+1));
        int iVal=(((int)CFG873)*16)+((int)CFG837);
        K8057hwf[i/2]=(char)iVal;
    }

    K8057hwf[8]='\0';
    for (int i=0;i<8;i++) {
        LI6348[i]+=K8057hwf[i]^CfG7821[i];}LI6348[8]='\0';
        for(int i=0;i<8;i++) {
            int C396uia;
            C396uia=LI6348[i]+LI6348[i+7];
            C396uia=C396uia%256;
            LI6348[i+8]=(char)C396uia;
            CA29[i*2]=C245hgj[C396uia/16];
            CA29[(i*2)+1]=C245hgj[C396uia%16];
        }
        return CA29;
    }
    private char CAIMETHODB(char C786fim) {
        String C612ops=String.valueOf(C786fim);
        C612ops=C612ops.toUpperCase();
        C786fim=C612ops.charAt(0);
        int C871ilg=0;
        boolean f613idv=false;
        while(!f613idv) {
            if (C245hgj[C871ilg]==C786fim) {
                f613idv=true;
            }
            else {
                C871ilg++;
            }
        }
        return (char)C871ilg;
    }

    public String encrypt(String passWord, String key) {
        return new String(CAIMETHOD(passWord, key));
    }

}