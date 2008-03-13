package com.energyict.genericprotocolimpl.lgadvantis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

import com.energyict.genericprotocolimpl.lgadvantis.collector.DefaultCosemAttributeCollector;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.UnsupportedException;
import com.energyict.xml.xmlhelper.DomHelper;

class CosemMethod extends AbstractCosem implements Cosem  {
    
    private CosemObject father;
    
    private String description;
    
    CosemMethod(int shortName, String description){
        this.setShortName( shortName );
        this.description = description;
        this.setCollector( new DefaultCosemAttributeCollector() );
    }
    
    CosemMethod(String obisCode, int shortName, String description){
        this( shortName, description );
        this.setObisCode( obisCode );
    }
    
    void setFather(CosemObject father) {
        this.father = father;
    }
    
    Element toDirectActionXmlElement(DomHelper dh, Element parent, String serial) {
        
        Element action = dh.addElement( parent, XmlTag.DIRECT_ACTION );
        
        action.setAttribute(XmlTag.TARGET, serial);
        action.setAttribute(XmlTag.ID, "" + ( getShortName() + 8 ) );
        action.setAttribute(XmlTag.TYPE, "RDLMS");
        
        return action;
        
    }
    
    Element toDirectActionWriteXmlElement(
            DomHelper dh, Element parent, String serial, Object value) 
            throws UnsupportedException {
        
        throw new UnsupportedException( "" );
        
    }
    
    DirectAction find( List directActions ) {
        Iterator i = directActions.iterator();
        
        while( i.hasNext() ) {
            DirectAction da = (DirectAction)i.next();
            if( getShortName() == da.getShortName() )
                return da;
        }
        
        return null;
    }
    
    public List addMeTo(Task task, boolean write) {
        return task.addActionFor( this );
    }
    
    public void addMeTo(CosemObject cosemObject) {
        cosemObject.add(this);
    }

    /* Attempt to answer myself, if not ask daddy */
    public ObisCode getObisCode( ) {
        
        if( super.getObisCode() != null )
            return super.getObisCode();
        
        return father.getObisCode();
    }
    
    public List getPrimitiveShortNameList(){
    	List list = new ArrayList();
    	list.add(new Integer(getShortName()));
    	return list;
    }

    
    public String toString( ) {
        return 
            "ShortName [ " + 
                "0x" + Integer.toHexString( getShortName() ) + ", " +
                getObisCode() + ", " + 
                description + "]";
    }
    

}
