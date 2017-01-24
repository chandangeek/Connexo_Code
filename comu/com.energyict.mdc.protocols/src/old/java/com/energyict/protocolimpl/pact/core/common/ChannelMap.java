/*
 * ChannelMap.java
 *
 * Created on 12 maart 2004, 11:56
 * Changes:
 * KV 10092004 changed to have default demand values if no channel map is given!
 */

package com.energyict.protocolimpl.pact.core.common;

import java.io.IOException;
import java.util.StringTokenizer;

/**
 *
 * @author  Koen
 */
public class ChannelMap {
    
    static public final int FUNCTION_DEFAULT=0; 
    static public final int FUNCTION_SURVEY_ADVANCE=1;
    static public final int FUNCTION_ACTUAL_ADVANCE=2;
    static public final int FUNCTION_DEMAND=3;
    static public final int FUNCTION_CUMULATIVE=4; // do the differentiation in mdw
    
    int[] channelFunctions=null;
    
    /** Creates a new instance of ChannelMap 
     *  ChannelMap is used to configure the nr of channels and the processing mode.
     *  e.g. 3,3,0,0,0
     *  5 channels
     *  channel 0 and 1 are raw values processed and calculated to demand values (see PACT doc 'Interpreting Load Surveys')
     *  channel 2,3 and 4 contain raw values. 
     *
     */
    public ChannelMap(String channelMapProperty) throws IOException {
        if (channelMapProperty != null) {
			parse(channelMapProperty);
		}
    }
    
    public void reverse() {
        if (channelFunctions != null) {
            int[] channelFunctions2 = new int[channelFunctions.length];
            for (int i=0;i<channelFunctions.length;i++) {
                channelFunctions2[i]=channelFunctions[(channelFunctions.length-1)-i];
            }
            channelFunctions = channelFunctions2;
        }
    }
    
    public int getChannelFunction(int index) {
       if (channelFunctions == null) {
		return FUNCTION_DEMAND;
	} 
       try {
          return channelFunctions[index];    
       }
       catch(ArrayIndexOutOfBoundsException e) {
          return FUNCTION_DEMAND; 
       }
    }
    
            
    public boolean isDefault(int index) {
        return  (getChannelFunction(index) == FUNCTION_DEFAULT);  
    }
    public boolean isSurveyAdvance(int index) {
        return  (getChannelFunction(index) == FUNCTION_SURVEY_ADVANCE);  
    }
    public boolean isActualAdvance(int index) {
        return  (getChannelFunction(index) == FUNCTION_ACTUAL_ADVANCE);  
    }
    public boolean isDemand(int index) {
        return  (getChannelFunction(index) == FUNCTION_DEMAND);  
    }
    
    private void parse(String channelMapProperty) throws IOException {
        
        int count=0;
        StringTokenizer strTok=new StringTokenizer(channelMapProperty,",");
        channelFunctions = new int[strTok.countTokens()];
        while(strTok.hasMoreElements()) {
           try { 
              channelFunctions[count++] = Integer.parseInt(strTok.nextToken());
           }
           catch(NumberFormatException e) {
              throw new IOException("ChannelMap, parse, Error in channelmap property, "+e.toString()); 
           }
        }
    }
    
    /** Getter for property channelFunction.
     * @return Value of property channelFunction.
     *
     */
    public int[] getChannelFunctions() {
        return this.channelFunctions;
    }
    
    /** Setter for property channelFunction.
     * @param channelFunction New value of property channelFunction.
     *
     */
    public void setChannelFunctions(int[] channelFunctions) {
    	if(channelFunctions != null){
    		this.channelFunctions = channelFunctions;
    	}
    }
    
 // private void parse(String str) throws IOException
    
}
