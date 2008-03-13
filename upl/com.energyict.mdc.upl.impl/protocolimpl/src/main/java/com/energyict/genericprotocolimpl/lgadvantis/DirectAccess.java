package com.energyict.genericprotocolimpl.lgadvantis;

import com.energyict.cbo.TimePeriod;
import com.energyict.obis.ObisCode;
import com.energyict.xml.xmlhelper.DomHelper;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

public class DirectAccess extends DirectPrimitive {

	public DirectAccess( Cosem cosem, String serial ) {
		super(cosem,serial);
	}

	public List getShortNames() {
		return cosem.getPrimitiveShortNameList();
	}       

	public Element toXmlElement(DomHelper dh, Element parent) throws IOException {

		Element action = dh.addElement( parent, XmlTag.DIRECT_ACCESS );

		action.setAttribute(XmlTag.TARGET, target);
		action.setAttribute(XmlTag.VDE, XmlTag.VDE_VALUE);
		action.setAttribute(XmlTag.TYPE, type);

		// adding the shortNames
		dh.addElement( action, XmlTag.CONTENT, toHexString( ) );

		return action;
	}

	private String toHexString() {
		List names = getShortNames();
		int length = names.size()*3+1;
		
		String result = "";
		// add length 
		result += toShortHex(length);
		// add names
		for (Iterator it=names.iterator(); it.hasNext();){
			Integer name = (Integer) it.next();
			// add read indication
			result += toByteHex(0x02);
			// add shortname
			result += toShortHex(name.intValue());
		}
		
		return result;
	}

	private String toByteHex( int b ) {
		return 
		"" + 
		Integer.toHexString( ( b & 0xF0 ) >> 4 ) + 
		Integer.toHexString( b & 0x0F );  
	}

	private String toShortHex( int s ) {
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
		.append( "DirectAccess [ " + o )
		.append( ", content=" ).append( toHexString() )
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
