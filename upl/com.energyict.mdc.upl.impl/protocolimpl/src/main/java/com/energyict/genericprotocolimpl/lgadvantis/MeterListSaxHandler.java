package com.energyict.genericprotocolimpl.lgadvantis;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


class MeterListSaxHandler extends DefaultHandler {
    
    private Stack stack;
    private List bcpl = null;

    public void startElement(
            String uri, String localName, String name, Attributes attributes) 
            throws SAXException {
        
        if( XmlTag.BCPL.equals( name ) ) {
            handleStartBcpl(attributes);
        }
        
        if( XmlTag.EE_ITEM.equals(name) ) {
            handleEeItem(attributes);
        }
        
    }
    
    public void endElement(
            String uri, String localName, String name) 
            throws SAXException {
        
        if( XmlTag.BCPL.equals( name ) ) {
            handleEndBcpl( );
        }

    }

    public void handleStartBcpl(Attributes attrib) {
                
        String ident = attrib.getValue( XmlTag.IDENT );
        String address = attrib.getValue( XmlTag.ADDRESS );
        long credit = Long.parseLong( attrib.getValue( XmlTag.CREDIT ) );
        String type = attrib.getValue( XmlTag.TYPE );
        InstallationType it= InstallationType.get(type);
        
        Bcpl bcpl = new Bcpl(ident, address, credit, it);
        getBcpl().add( bcpl );
        getStack().push( bcpl );
        
    }
    
    public void handleEndBcpl( ) {
        stack.pop();
    }
    
    public void handleEeItem(Attributes attrib) {
        
        String ident = attrib.getValue( XmlTag.IDENT );
     
        long lParamId = toLong( attrib.getValue( XmlTag.PARAM_ID ) );
        String configId = attrib.getValue( XmlTag.CONFIG_ID );
        String utilityId = attrib.getValue( XmlTag.UTILITY_ID );
        
        EeItem item = new EeItem(ident, lParamId, configId, utilityId);
        
        Bcpl bcpl = (Bcpl) getStack().peek();
        bcpl.addEeItem( item );
        
    }
    
    Stack getStack( ) {
        if( stack == null ) {
            stack = new Stack();
        }
        return stack;
    }
 
    public List getBcpl( ) {
        if( bcpl == null ) {
            bcpl = new ArrayList( );
        }
        return bcpl;
    }
    
    private long toLong(String aString) {
        if( aString != null )
            return Long.parseLong(aString);
        return 0;
    }

    static MeterListSaxHandler toXmlDataHandler( String xmlString ) 
        throws ParserConfigurationException, SAXException, IOException {
 
        MeterListSaxHandler xmlHandler = new MeterListSaxHandler();
        
        byte[] bai = xmlString.getBytes();
        InputStream i = (InputStream) new ByteArrayInputStream(bai);
        
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(i, xmlHandler );
        
        return xmlHandler;
        
    }
    
}
