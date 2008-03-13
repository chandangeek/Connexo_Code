package com.energyict.genericprotocolimpl.lgadvantis;

import java.io.IOException;
import java.util.Date;

import org.w3c.dom.Element;

import com.energyict.cbo.TimePeriod;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.obis.ObisCode;
import com.energyict.xml.xmlhelper.DomHelper;

public class DirectAction extends DirectPrimitive{
    
    private TimePeriod timePeriod;
    
    public DirectAction( Cosem cosem, String serial ) {
        super(cosem,serial);
    }
     
    public int getShortName() {
        return cosem.getShortName();
    }
            
    public void setTimePeriod( TimePeriod period ) {
        this.timePeriod = period;
    }
    
    public void setTimePeriod( Date from, Date to ) {
        this.timePeriod = new TimePeriod( from, to);
    }
                
       
    public Element toXmlElement(DomHelper dh, Element parent) throws IOException {
        
        Element action = dh.addElement( parent, XmlTag.DIRECT_ACTION );
        
        action.setAttribute(XmlTag.TARGET, target);
        action.setAttribute(XmlTag.TYPE, type);
        action.setAttribute(XmlTag.ID, "" + cosem.getShortName() );
        
        
        if( timePeriod != null ) {
            
            Date from = timePeriod.getFrom();
            Date to = timePeriod.getTo();
            
            String pString = toPeriodString(from, to);
            dh.addElement( action, XmlTag.R_CONTENT, pString);
        }

        if( abstractDataType != null ) {
            
            dh.addElement( action, XmlTag.CONTENT, toHexString( ) );

        }
        
        return action;

        
    }
    
    private String toPeriodString(Date from, Date to) {
        return Constant.format(from) + ", " + Constant.format(to) + ", " + 0;
    }
    
    String uniqueKey( ){
        return getShortName() + "-" + getType();
    }
    
    private String toHexString( ) throws IOException {
        byte [] ber = getAbstractDataType().getBEREncodedByteArray();
        return toHexString( ber );
    }
    
    private String toHexString( byte [] bar ) {
        
        StringBuffer sb = new StringBuffer();   
        sb.append( toHex( (short)bar.length ) );
        
        for (int i = 0; i < bar.length; i++) {
            sb.append( toHex( bar[i] ) );
        }
        
        return sb.toString();
        
    }
    
    private String toHex( byte b ) {
         return 
             "" + 
             Integer.toHexString( ( b & 0xF0 ) >> 4 ) + 
             Integer.toHexString( b & 0x0F );  
    }
    
    private String toHex( short s ) {
        return 
            "" + 
            Integer.toHexString( (s & 0x0000F000) >> 12 ) + 
            Integer.toHexString( (s & 0x00000F00) >> 8  ) + 
            Integer.toHexString( (s & 0x000000F0) >> 4  ) + 
            Integer.toHexString( (s & 0x0000000F) );
    }
    
    
    public String toString( ){
        
        ObisCode o = cosem.getObisCode();
        
        StringBuffer rslt = 
            new StringBuffer( )
                .append( "DirectAction [ " + o )
                .append( ", id=" ).append( getShortName() )
                .append( " (0x" ).append( Integer.toHexString( getShortName() ) ).append( ")" )
        		.append( ", target=" ).append( target )
        		.append( ", type=" ).append(  type );
        
        if( cplStatus != null ) {
            
            String dataType = ( "" + abstractDataType )/*.replace('\n','-') */;
            
            rslt
                .append( ", cplStatus=" ).append( cplStatus ) 
                .append( ", abstractDataType=" ).append( dataType );
        }
        
        
        rslt.append( "]" );
        
        return rslt.toString();
        
    }
    
    
}
