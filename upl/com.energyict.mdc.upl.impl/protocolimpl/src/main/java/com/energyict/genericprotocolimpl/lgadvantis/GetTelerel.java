package com.energyict.genericprotocolimpl.lgadvantis;

import java.util.Date;

import org.w3c.dom.Element;

import com.energyict.xml.xmlhelper.DomHelper;

class GetTelerel {
    
    private String ident;
    private String crName;
    private Date from;
    private Date to;
    
    public String getIdent() {
        return ident;
    }
    
    void setIdent(String ident) {
        this.ident = ident;
    }
    
    String getCrName() {
        return crName;
    }
    
    void setCrName(String crName) {
        this.crName = crName;
    }
    
    Date getFrom() {
        return from;
    }
    
    void setFrom(Date from) {
        this.from = from;
    }
    
    Date getTo() {
        return to;
    }
    
    void setTo(Date to) {
        this.to = to;
    }

    
    DomHelper addToDom(DomHelper domHelper, Element parent) {

        Element telerel = domHelper.addElement(parent, XmlTag.GET_TELEREL);
        
        telerel.setAttribute(XmlTag.IDENT, ident);
        telerel.setAttribute(XmlTag.CR_NAME, crName);
        
        if( from != null ) {
            String fromString = Constant.DATEFORMAT.format(from);
            telerel.setAttribute(XmlTag.IDENT, fromString );
        }
        
        if( to != null ) {
            String toString = Constant.DATEFORMAT.format(from);
            telerel.setAttribute(XmlTag.IDENT, toString);
        }
        
        return domHelper;
 
    }
    
}


