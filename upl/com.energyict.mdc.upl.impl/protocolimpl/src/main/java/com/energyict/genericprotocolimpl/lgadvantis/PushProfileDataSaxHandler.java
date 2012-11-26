package com.energyict.genericprotocolimpl.lgadvantis;

import java.io.*;

import javax.xml.parsers.*;

import com.energyict.mdw.core.Device;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.mdw.core.MeteringWarehouse;

class PushProfileDataSaxHandler extends DefaultHandler {

    private final boolean debug = true;
    
    private char [] buffer = new char[0];
    
    private Device meter;
    
    
    public void startElement(
        String uri, String localName, String name, Attributes attrib) 
        throws SAXException {
        
        if( XmlTag.TELEREL_ITEM.equals(name) ) {
            int id = Integer.parseInt( attrib.getValue(XmlTag.IDENT) ); 
            meter = MeteringWarehouse.getCurrent().getRtuFactory().find(id);
        }
        
    }
    

    public void endElement( String uri, String localName, String name) 
        throws SAXException {

        try {
            
            if( XmlTag.TELEREL_ITEM.equals(name) ) {
                
                
                if( meter != null ) {
                    AbstractDataType adt = toAbastractDataType();
//                    meter.store(profileData);
                }
                
                meter = null;
            
            }
            
         
        } catch (IOException e) {
            
            e.printStackTrace();
            throw new SAXException( e );
            
        } finally {
            
            buffer = new char[0];
        
        }
            
    }
    
//    private ProfileData toProfileData( ) {
//        
//        if( meter != null ) {
//
//            ProfileData pd = new ProfileData();
//            pd.setChannelInfos( Constant.toChannelInfos(meter) );
//            
//            
//            
//            
//        }
//        
//    }
    
    private AbstractDataType toAbastractDataType( ) throws IOException {
        
        byte ba [] = new byte [buffer.length/2];
        for (int chi = 0, bai = 0; chi < buffer.length; chi+=2) {
            
            String sValue = "" + buffer[chi] + buffer[chi+1];
            ba[bai] = (byte) Integer.parseInt( sValue, 16);
            bai += 1;
            
        }
    
        if( ba.length > 0 )
            return AXDRDecoder.decode( ba );
        
        return null;

    }
    
    public void characters (char ch[], int start, int length)
        throws SAXException {
        
        
        char [] temp = new char[buffer.length + length];
        
        if( buffer.length > 0 )
            System.arraycopy(buffer, 0, temp, 0, buffer.length);
        
        System.arraycopy(ch, start, temp, buffer.length, length);
        
        buffer = temp;
        
    }
    
    private void debug(String message) {
        if( debug ) {
            System.out.println( message );
        }
    }

    static PushProfileDataSaxHandler parse(String xmlString) 
        throws IOException {
        
        try {
            PushProfileDataSaxHandler xmlHandler = new PushProfileDataSaxHandler( );
            
            byte[] bai = xmlString.getBytes();
            InputStream i = (InputStream) new ByteArrayInputStream(bai);
            
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(i, xmlHandler);
            
            return xmlHandler;
            
        } catch( SAXException sex ) {
            sex.printStackTrace();
            throw new IOException( sex.getMessage() );
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
            throw new IOException( pce.getMessage() );
        } 
        
        
    }
    
    public void error(SAXParseException e) throws SAXException {
        debug( e.getMessage() );
        System.out.println( e );
    }

    public void fatalError(SAXParseException e) throws SAXException {
        debug( e.getMessage() );
        System.out.println( e );
    }

    public void warning(SAXParseException e) throws SAXException {
        debug( e.getMessage() );
        System.out.println( e );
    }

    public String toString( ) {
        StringBuffer result = new StringBuffer();
        
        result.append( "DirectActionSaxHandler [ \n" );
        
        
        result.append( "\n ]" );
        
        return result.toString();
    }
    
}
