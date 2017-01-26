/*
 * CTVTRead.java
 *
 * Created on 14 september 2005, 13:51
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.iec1107.enermete70x;

import java.util.StringTokenizer;


/**
 *
 * @author Koen
 */
public class CTVTRead extends AbstractDataReadingCommand {
    
    String ctvtRatio;
    
    /** Creates a new instance of CTVTRead */
    public CTVTRead(DataReadingCommandFactory drcf) {
        super(drcf);
    }
    
    
    public void parse(byte[] data, java.util.TimeZone timeZone) throws java.io.IOException {
        data = getDataReadingCommandFactory().getEnermet().getIec1107Connection().parseDataBetweenBrackets(data);
        String str = new String(data);
        StringTokenizer strTok = new StringTokenizer(str,",");
        ctvtRatio = "VT="+strTok.nextToken()+"/"+strTok.nextToken()+"(/"+strTok.nextToken()+"), CT="+strTok.nextToken()+"/"+strTok.nextToken()+"(/"+strTok.nextToken()+")";
    }    

    public void retrieveCTVTRatio() throws java.io.IOException {
        retrieve("3");
    }
    
    public String getCtvtRatio() {
        return ctvtRatio;
    }
}
