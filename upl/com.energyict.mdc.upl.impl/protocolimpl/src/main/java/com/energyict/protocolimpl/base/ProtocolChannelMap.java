/*
 * ProtocolChannelMap.java
 *
 * Created on 12 september 2003, 9:46
 */

package com.energyict.protocolimpl.base;

import java.io.*;
import java.util.*;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
/**
 *
 * @author  Koen
 * changes:
 * KV 04012005 Allow ',' as separator
 *
 * sequence==false
 * Individual channels are separated by ':' or ','.
 * e.g. 4.4:7.6
 * e.g. 5,6,7
 * e.g. 0,0,0,0
 * sequence == true
 * e.g. 0 3 4
 * will be 1,0,0,1,1 means channel 0,3 and 4 enabled and active! 
 * mapping:
 * e.g. 2.5 6.7  all 0 based!
 * means keep rtu channel 2 and save it to the 5 in the database
 * e.g. 2.5.6.7+7 6.7.1.8+5
 * means 2.5 6.7 mapping with registers 6.7+7 and 1.8+5
 */
public class ProtocolChannelMap {
    
    List protocolChannels = null;
    boolean mappedChannels=false; // KV 06092005 K&P
    
    /** Creates a new instance of ChannelMap */
    // KV 06092005 K&P
    public ProtocolChannelMap(String channelConfig) throws InvalidPropertyException {
        
        this(channelConfig,channelConfig.indexOf(" ")>=0?true:false); // KV 06092005 K&P
    }
    
    public ProtocolChannelMap(String channelConfig,boolean sequence) throws InvalidPropertyException {
        if (sequence && (channelConfig.indexOf(".")>=0)) { // KV 06092005 K&P
            mappedChannels=true;
        }
        protocolChannels = new ArrayList();
        parse(sequence?convert2NonSequence(channelConfig):channelConfig);
    }
    
    public ProtocolChannelMap(List protocolChannels) {
        this.protocolChannels = protocolChannels;
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ProtocolChannelMap:\n");
        for (int i=0;i<protocolChannels.size();i++) {
            ProtocolChannel pc = (ProtocolChannel)protocolChannels.get(i);
            strBuff.append(pc+"\n");
        }
        return strBuff.toString();
    }
    
    private String convert2NonSequence(String channelConfig) { // KV 06092005 K&P changes
        StringTokenizer strTok = new StringTokenizer(channelConfig,",: ");
        int[] channelIds = new int[strTok.countTokens()];
        String[] eiServerChannelIds = new String[strTok.countTokens()];
        int highestChannel=0,t=0;
        while(strTok.hasMoreTokens()) {
            String token = strTok.nextToken();
            int rtuChannel;
            if (mappedChannels) {
                int dotIndex = token.indexOf(".");
                rtuChannel = Integer.parseInt(token.substring(0,dotIndex));
                eiServerChannelIds[t] = token.substring(dotIndex+1);
            }
            else {
                rtuChannel = Integer.parseInt(token);
            }
            
            channelIds[t] = rtuChannel;
            highestChannel=channelIds[t]>highestChannel?channelIds[t]:highestChannel;
            t++;
        }   
        int[] channels = new int[highestChannel+1];
        for (int i=0;i<channels.length;i++) 
            channels[i] = mappedChannels?-1:0;
        int count=0;
        for (int i=0;i<channelIds.length;i++)
            channels[channelIds[i]]=mappedChannels?count++:1;
        StringBuffer strBuff = new StringBuffer();
        for (int i=0;i<channels.length;i++) {
            if (mappedChannels) {
               strBuff.append(channels[i]==-1?"-1":eiServerChannelIds[channels[i]]);
               strBuff.append(i<(channels.length-1)?",":"");
            }
            else {
               strBuff.append(channels[i]);
               strBuff.append(i<(channels.length-1)?",":"");
            }
        }
        return strBuff.toString();
    } // private String convert2NonSequence(String channelConfig)

    
    private String convert2NonSequence2(String channelConfig) { // KV 06092005 K&P changes
        StringTokenizer strTok = new StringTokenizer(channelConfig,",: ");
        int[] channelIds = new int[strTok.countTokens()];
        int[] eiServerChannelIds = new int[strTok.countTokens()];
        int highestChannel=0,t=0;
        while(strTok.hasMoreTokens()) {
            String token = strTok.nextToken();
            int rtuChannel,eiServerChannel;
            if (mappedChannels) {
                int dotIndex = token.indexOf(".");
                rtuChannel = Integer.parseInt(token.substring(0,dotIndex));
                eiServerChannel = Integer.parseInt(token.substring(dotIndex+1));
                eiServerChannelIds[t]=eiServerChannel;
            }
            else {
                rtuChannel = Integer.parseInt(token);
            }
            
            channelIds[t] = rtuChannel;
            highestChannel=channelIds[t]>highestChannel?channelIds[t]:highestChannel;
            t++;
        }   
        int[] channels = new int[highestChannel+1];
        for (int i=0;i<channels.length;i++) 
            channels[i] = mappedChannels?-1:0;
        for (int i=0;i<channelIds.length;i++)
            channels[channelIds[i]]=mappedChannels?eiServerChannelIds[i]:1;
        StringBuffer strBuff = new StringBuffer();
        for (int i=0;i<channels.length;i++) {
            strBuff.append(channels[i]);
            strBuff.append(i<(channels.length-1)?",":"");
        }
        return strBuff.toString();
    } // private String convert2NonSequence(String channelConfig)
    
    public String getChannelRegisterMap() {
        boolean init = true;
        StringBuffer strBuff = new StringBuffer();
        Iterator it = protocolChannels.iterator();
        while(it.hasNext()) {
           if (!init) strBuff.append(":"); 
           init = false;
           ProtocolChannel protocolChannel = (ProtocolChannel)it.next();
           strBuff.append(protocolChannel.getRegister());
        }
        return strBuff.toString();
    }
    
    public boolean hasEqualRegisters(ProtocolChannelMap protocolChannelMap) {
        return (getChannelRegisterMap().compareTo(protocolChannelMap.getChannelRegisterMap())==0);
    }
    
    public List getProtocolChannels() {
       return protocolChannels;    
    }
    
    public int getNrOfProtocolChannels() {
        return protocolChannels.size();
    }
    public int getNrOfUsedProtocolChannels() {
        int nrOfUsedProtocolChannels=0;
        for (int channel=0;channel<getProtocolChannels().size();channel++) {
            if (!isProtocolChannelZero(channel))
                nrOfUsedProtocolChannels++;
        }
        return nrOfUsedProtocolChannels;
    }
    
    public ProtocolChannel getProtocolChannel(int index) {
        if (index >= protocolChannels.size())
            return null;
        else
            return (ProtocolChannel)protocolChannels.get(index);
    }
    
    public boolean channelExists(String register) {
        Iterator it = protocolChannels.iterator();
        while(it.hasNext()) {
           ProtocolChannel protocolChannel = (ProtocolChannel)it.next();
           if (protocolChannel.getRegister().compareTo(register) == 0) return true;
        }
        return false;
    }
    
    
    private void parse(String channelConfig) throws InvalidPropertyException {
        
        int channelNr=0;
        StringTokenizer st1 = new StringTokenizer(channelConfig,":");
        StringTokenizer st2 = new StringTokenizer(channelConfig,",");
        
        if (st1.countTokens() >= st2.countTokens()) {
            while (st1.countTokens() > 0) {
                String strChannel = st1.nextToken();
                protocolChannels.add(new ProtocolChannel(strChannel));
            }
        }
        else {
            while (st2.countTokens() > 0) {
                String strChannel = st2.nextToken();
                protocolChannels.add(new ProtocolChannel(strChannel));
            }
        }
    }
    
    public boolean isProtocolChannel(int index) {
         if (index >= protocolChannels.size())
            return false;
        else
            return true;
    }
    
    public boolean isProtocolChannelEnabled(int index) {
        return !isProtocolChannelZero(index);
    }
    
    public boolean isProtocolChannelZero(int index) {
         if (index >= protocolChannels.size())
            return false;
        else {
            if (getProtocolChannel(index).getRegister().compareTo("0") != 0)
                return false;
            else
                return true;
        }
    }
    
    /** Convert this ChannelMap to a List of ChannelInfo Objects.
     * @return List of ChannelInfo's
     */
    public List toChannelInfoList( ){
        
        ArrayList result = new ArrayList();
        int channelCount = getNrOfProtocolChannels(); 
        
        for( int i = 0; i < channelCount; i++ ){
            ChannelInfo ci = getProtocolChannel(i).toChannelInfo(i);
            if( ci != null )
                result.add( ci );
        }
        
        return result;
        
    }
    
    static public void main(String[] args) {
        try {
            ProtocolChannelMap protocolChannelMap=null;
//            System.out.println("******************************************************");
            //protocolChannelMap = new ProtocolChannelMap("1,2.4.6+500,0,1,4");
            protocolChannelMap = new ProtocolChannelMap("3,2,1,0");
            
            System.out.println(protocolChannelMap.getProtocolChannel(0).getValue());
            System.out.println(protocolChannelMap.getProtocolChannel(1).getValue());
            System.out.println(protocolChannelMap.getProtocolChannel(2).getValue());
            System.out.println(protocolChannelMap.getProtocolChannel(3).getValue());
            
                    
//            System.out.println("******************************************************");
//            System.out.println(protocolChannelMap.getProtocolChannel(1).getRegister());
//            System.out.println(protocolChannelMap.getProtocolChannel(1).getWrapAroundValue());
//            System.out.println(protocolChannelMap.getProtocolChannel(1).isCumul());
//            System.out.println(protocolChannelMap.getProtocolChannel(1).getValue(0));
//            System.out.println(protocolChannelMap.getProtocolChannel(1).getValue(1));
//            System.out.println(protocolChannelMap.getProtocolChannel(1).getValue(2));
//            System.out.println(protocolChannelMap.getProtocolChannel(1).getValue(3));
//            System.out.println("nrOfValues="+protocolChannelMap.getProtocolChannel(1).getNrOfValues());
//            System.out.println("*****");
//            
//            System.out.println(protocolChannelMap.isProtocolChannelEnabled(2));
//            System.out.println("nrOfValues="+protocolChannelMap.getProtocolChannel(2).getNrOfValues());
//            System.out.println(protocolChannelMap.isProtocolChannelEnabled(3));
//            System.out.println("nrOfValues="+protocolChannelMap.getProtocolChannel(3).getNrOfValues());
//            System.out.println("*****");
            
//            System.out.println("******************************************************");
//            protocolChannelMap = new ProtocolChannelMap("1,2.4.6+5,3");
//            System.out.println(protocolChannelMap.getNrOfProtocolChannels());
//            System.out.println(protocolChannelMap.getProtocolChannel(1).getRegister());
//            System.out.println(protocolChannelMap.getProtocolChannel(1).getWrapAroundValue());
//            System.out.println(protocolChannelMap.getProtocolChannel(1).isCumul());
//            System.out.println(protocolChannelMap.getProtocolChannel(1).getValue(0));
//            System.out.println(protocolChannelMap.getProtocolChannel(1).getValue(1));
//            System.out.println(protocolChannelMap.getProtocolChannel(1).getValue(2));
//            System.out.println(protocolChannelMap.getProtocolChannel(1).getValue(3));
//            System.out.println("******************************************************");
//            protocolChannelMap = new ProtocolChannelMap("0+9:0:0:0");
//            System.out.println(protocolChannelMap.getNrOfProtocolChannels());
//            System.out.println(protocolChannelMap.getProtocolChannel(0).getRegister());
//            System.out.println(protocolChannelMap.getProtocolChannel(0).getWrapAroundValue());
//            System.out.println(protocolChannelMap.getProtocolChannel(0).isCumul());
//            System.out.println(protocolChannelMap.getProtocolChannel(0).getValue(0));
//            System.out.println("******************************************************");
//            protocolChannelMap = new ProtocolChannelMap("1,0,1");
//            System.out.println(protocolChannelMap.getNrOfProtocolChannels());
//            System.out.println(protocolChannelMap.getProtocolChannel(0).getValue());
//            System.out.println(protocolChannelMap.getProtocolChannel(1).getValue());
//            System.out.println(protocolChannelMap.isProtocolChannel(2));
//            System.out.println(protocolChannelMap.isProtocolChannelZero(1));
//            System.out.println(protocolChannelMap.channelExists("2"));
//            protocolChannelMap = new ProtocolChannelMap("2.6 8.3 1.1");
//            System.out.println(protocolChannelMap.getChannelRegisterMap());
//            System.out.println(protocolChannelMap.getNrOfProtocolChannels());
//            System.out.println(protocolChannelMap.getProtocolChannel(0).getValue(0));
//            System.out.println(protocolChannelMap.getProtocolChannel(1).getValue(0));
//            System.out.println(protocolChannelMap.getProtocolChannel(1).getValue(1));
//            System.out.println(protocolChannelMap.getProtocolChannel(2).getValue(0));
//            System.out.println(protocolChannelMap.getProtocolChannel(2).getValue(1));
//            System.out.println(protocolChannelMap.getProtocolChannel(2).isCumul());
//            System.out.println(protocolChannelMap.isProtocolChannel(2));
//            System.out.println("***************************************************************");
//            System.out.println(protocolChannelMap.isProtocolChannelZero(0));
//            System.out.println("***************************************************************");
//            System.out.println(protocolChannelMap.channelExists("0"));
//            
//            protocolChannelMap = new ProtocolChannelMap("1 4 2 0");
//            System.out.println(protocolChannelMap.getChannelRegisterMap());
//            System.out.println(protocolChannelMap.getNrOfProtocolChannels());
            //System.out.println(protocolChannelMap.getProtocolChannel(2).getValue());
            
            
            
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isMappedChannels() {
        return mappedChannels;
    }
}