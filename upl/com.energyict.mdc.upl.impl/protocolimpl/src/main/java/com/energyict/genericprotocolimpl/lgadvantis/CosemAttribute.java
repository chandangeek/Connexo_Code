package com.energyict.genericprotocolimpl.lgadvantis;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.genericprotocolimpl.lgadvantis.collector.DefaultCosemAttributeCollector;
import com.energyict.genericprotocolimpl.lgadvantis.parser.DefaultParser;
import com.energyict.genericprotocolimpl.lgadvantis.parser.Parser;
import com.energyict.obis.ObisCode;
import com.energyict.xml.xmlhelper.DomHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

public class CosemAttribute extends AbstractCosem implements Cosem { 
    
    private CosemObject father;
    
    private String description;

    CosemAttribute(int shortName, String description){
        this( null, shortName, description );
    }
    
    CosemAttribute(String obisCode, int shortName, String description){
        this( obisCode, shortName, description, Unit.getUndefined() );
    }
    
    CosemAttribute(String obisCode, int shortName, String description, Unit unit ) {
        this.setObisCode(obisCode);
        this.setShortName( shortName );
        this.description = description;
        
        this.setParser( new DefaultParser( unit ) );
        this.setCollector( new DefaultCosemAttributeCollector() );
    }
    
    void setFather(CosemObject father) {
        this.father = father;
    }
    
    Cosem setParser( Parser parser ) {
        super.setParser(parser);
        parser.setAttribute( this );
        return this;
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
    
    Element toDirectActionXmlElement(DomHelper dh, Element parent, String serial) {
        
        Element action = dh.addElement( parent, XmlTag.DIRECT_ACTION );
        
        action.setAttribute(XmlTag.TARGET, serial);
        action.setAttribute(XmlTag.ID, "" + ( getShortName() + 8 ) );
        action.setAttribute(XmlTag.TYPE, "RDLMS");
        
        return action;
        
    }
    
    /** Collect my data in list of directActions */
    public void collect(List directActions, Task task ) 
        throws IOException { 
        
        parse( find( directActions ).getAbstractDataType(), task );
        
    }
    
    /** Interprete my data */
    public void parse(AbstractDataType dataType, Task task) 
        throws IOException {
        
        super.parse(dataType, task);
        
    }
    
    public void parse(byte[] binaryData, Task task) throws IOException{
    	super.parse(binaryData, task);
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
        return task.addActionFor(this, write);
    }
    
 
    public void addMeTo(CosemObject cosemObject) {
        cosemObject.add(this);
    }
    
    boolean isWriteable( ){
        return getEncoder() != null;
    }

    
    public String toString( ) {
        return "CosemAttribute [ 0x" + Integer.toHexString( getShortName() ) +
                ", " + description + " ]";
    }


    
}
