package com.energyict.genericprotocolimpl.lgadvantis;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.xml.xmlhelper.DomHelper;

import java.io.IOException;

import org.w3c.dom.Element;

public abstract class DirectPrimitive {

    protected String target;
    protected String type;

    protected Cosem cosem;
    protected AbstractDataType abstractDataType;

    
    protected String cplStatus;
    protected byte[] binaryData;

    public DirectPrimitive( Cosem cosem, String serial ) {
        this.target         = serial;
        this.cosem          = cosem;
        this.type           = XmlTag.RDLMS;
    }

    public Cosem getCosem( ){ 
        return cosem;
    }
    
    public void setCplStatus(String cplStatus) {
        this.cplStatus = cplStatus;
    }

    public boolean isOk( ) {
        return "OK".equals(cplStatus);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type; 
    }

    public AbstractDataType getAbstractDataType( ) {
        return abstractDataType;
    }
    
    public void setAbstractDataType( AbstractDataType abstractDataType ) {
        this.abstractDataType = abstractDataType;
    }

    public void setAbstractDataType( Object object ){
        abstractDataType = cosem.getEncoder().encode(object);
    }

    public byte[] getBinaryData() {
		return binaryData;
	}

	public void setBinaryData(byte[] binaryData) {
		this.binaryData = binaryData;
	}

	public void setWrite( boolean flag ){
        if( flag ) {
            this.type = XmlTag.WDLMS;
        } else {
            this.type = XmlTag.RDLMS;
        }
    }
    
    public boolean isWrite( ) {
        return XmlTag.WDLMS.equals( this.type );
    }
    
    public boolean isRead( ) {
        return XmlTag.RDLMS.equals( this.type );
    }
    
    public abstract Element toXmlElement(DomHelper dh, Element parent) throws IOException;


}
