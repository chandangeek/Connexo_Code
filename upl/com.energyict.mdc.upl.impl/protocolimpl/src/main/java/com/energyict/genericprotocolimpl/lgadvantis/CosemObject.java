package com.energyict.genericprotocolimpl.lgadvantis;

import java.io.IOException;
import java.util.*;

import org.w3c.dom.Element;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.genericprotocolimpl.lgadvantis.collector.DefaultCosemObjectCollector;
import com.energyict.protocol.UnsupportedException;
import com.energyict.xml.xmlhelper.DomHelper;

public class CosemObject extends AbstractCosem implements Cosem {
    
    private String description;

    private Map attributes      = new TreeMap( );
    private Map methods         = new TreeMap( );
    
    CosemObject( String description ) {
        this.description = description;
        setCollector( new DefaultCosemObjectCollector() );
    }
    
    CosemObject( String obis, String description ) {
        this.setObisCode( obis );
        this.description = description;
        setCollector( new DefaultCosemObjectCollector() );
    }
    
    CosemObject add( Cosem cosem ) {
        cosem.addMeTo(this);
        return this;
    }
    
    CosemObject add( CosemAttribute cosemAttribute ) {
        String key = ""+cosemAttribute.getShortName();
        attributes.put( key, cosemAttribute );
        cosemAttribute.setFather(this);
        return this;
    }
    
    CosemObject add( CosemMethod cosemMethod ) {
        String key = ""+cosemMethod.getShortName();
        methods.put( key, cosemMethod );
        cosemMethod.setFather(this);
        return this;
    }

    public Collection getAttributes( ){
        return attributes.values();
    }
    
    public Collection getMethods( ){
        return methods.values();
    }
    
    public List getPrimitiveShortNameList(){
    	List list = new ArrayList();
    	for (Iterator it = getAttributes().iterator(); it.hasNext();){
    		Cosem cosem = (Cosem) it.next();
    		list.addAll(cosem.getPrimitiveShortNameList());
    	}
    	for (Iterator it = getMethods().iterator(); it.hasNext();){
    		Cosem cosem = (Cosem) it.next();
    		list.addAll(cosem.getPrimitiveShortNameList());
    	}
    	return list;
    }

    
    DomHelper toDirectActionXmlElement(DomHelper dh, Element parent, String serial) {
        
        Iterator i = attributes.values().iterator();
        while( i.hasNext() ) {
            CosemAttribute attribute = (CosemAttribute) i.next();
            attribute.toDirectActionXmlElement(dh, parent, serial);
        }
        
        return dh;
    }
    
    Element toDirectActionWriteXmlElement(
            DomHelper dh, Element parent, String serial, Object value) 
            throws UnsupportedException {
        
        throw new UnsupportedException( "" );
        
    }

    public List addMeTo(Task task, boolean write) {
        return task.addActionFor(this, write); 
    }

    public void addMeTo(CosemObject cosemObject) {
        cosemObject.add(this);
    }

    public String toString( ) {
        return 
            "ShortName [ " + 
                "0x" + Integer.toHexString(getShortName()) + ", " +
                getObisCode() + ", " + 
                description + "]";
    }
    
	public void parse(AbstractDataType dataType, Task task) throws IOException {
		// To be implemented
	}
    
}
