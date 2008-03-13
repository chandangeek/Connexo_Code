package com.energyict.echelon;

import java.io.IOException;
import java.util.Date;

import javax.xml.parsers.*;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

class NessScripts {

    public static void main(String[] args) throws EchelonException, TransformerException, SAXException, IOException, ParserConfigurationException, FactoryConfigurationError{
        
        
        // TODO: replace serverUri !!
        EchelonSession session = EchelonSession.getInstance("10.0.0.102");

        Date yesterday = new Date( System.currentTimeMillis() - (3600 * 1000) );
        
        String rslt = session.retrieveMsgLog( 
            "<PARAMETERS>" +
                "<LOGTYPEID>" + Constants.LogType.DEBUG +
                "</LOGTYPEID>" +
                
                "<STARTDATETIME>" +
                    Util.DATE_FORMAT.format( yesterday ) +
                "</STARTDATETIME>" + 
                "<ENDDATETIME>" + 
                    Util.DATE_FORMAT.format( new Date() ) +
                "</ENDDATETIME>" +
            "</PARAMETERS>", session.getSession() );
     
        
        System.out.println(rslt);
        
        Util.checkStatus(Util.toDom(rslt));
    }
    
}
