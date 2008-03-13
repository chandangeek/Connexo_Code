package com.energyict.genericprotocolimpl.lgadvantis;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.protocol.UnsupportedException;
import com.energyict.xml.xmlhelper.DomHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

public class CosemList extends AbstractCosem {

    private List objects      = new ArrayList( );
    	
    CosemList( ) {
    }
    
    CosemList( List objects, String description ) {
        this.objects = objects;
    }
        
    CosemList add( Cosem cosem) {
        objects.add(cosem);
        return this;
    }

    public Collection getObjects( ){
        return objects;
    }
        
    public List getPrimitiveShortNameList(){
    	List list = new ArrayList();
    	for (Iterator it = objects.iterator(); it.hasNext();){
    		Cosem cosem = (Cosem) it.next();
    		list.addAll(cosem.getPrimitiveShortNameList());
    	}
    	return list;
    }
    
    DomHelper toDirectActionXmlElement(DomHelper dh, Element parent, String serial) {
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
        String result = 
            "CosemList [ ";
        for (Iterator it = objects.iterator(); it.hasNext();){
        	Cosem object = (Cosem) it.next();
        	result += object.toString() + ";";
        }
        result +=" ]";
        return result;
    }
    
	public void parse(AbstractDataType dataType, Task task) throws IOException {
		// to be implemented  
	}


}
