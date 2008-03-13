package com.energyict.genericprotocolimpl.lgadvantis;

import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

class DirectPrimitiveSaxHandler extends DefaultHandler {

	private final boolean debug = true;

	private Task task;
	private Iterator directPrimitiveIterator;

	private DirectAction currentDirectAction;
	private DirectAccess currentDirectAccess;

	private boolean inDirectAction;
	private boolean inDirectAccess;
	private boolean inWriteRs;

	private char [] buffer = new char[0];


	public DirectPrimitiveSaxHandler(Task task) {
		this.task = task;
		this.directPrimitiveIterator = task.getActionMapIterator();
	}

	public void startElement(
			String uri, String localName, String name, Attributes attrib) 
	throws SAXException {

		if( XmlTag.WRITE_RS.equals(name) ) {
			inWriteRs = true;
		}

		if (XmlTag.DIRECT_ACTION.equals(name)) {

			inDirectAction = true;

			String id = attrib.getValue(XmlTag.ID);
			String cplStatus = attrib.getValue(XmlTag.CPL_STATUS);
			String type = attrib.getValue(XmlTag.TYPE);

			int iId = Integer.parseInt(id);

			try {
				DirectPrimitive primitive = (DirectPrimitive) directPrimitiveIterator.next();
				if (primitive instanceof DirectAction){
					currentDirectAction = (DirectAction) primitive;
					if( (currentDirectAction.getShortName() != iId) || (!(currentDirectAction.getType().equals(type))) ) {
						// Wrong action, something is wrong with the ordering
						currentDirectAction.setCplStatus( "WRONG RESPONSE ORDERING");
						currentDirectAction = null;
					} else {
						currentDirectAction.setCplStatus( cplStatus );
						currentDirectAction.setType(type);
					}
				} else {
					// Wrong action, something is wrong with the ordering
					currentDirectAction.setCplStatus( "WRONG RESPONSE ORDERING");
					currentDirectAction = null;
				}
			} catch (NoSuchElementException e) {
				currentDirectAction = null;
			}
		}

		if (XmlTag.DIRECT_ACCESS.equals(name)) {

			inDirectAccess = true;

			String cplStatus = attrib.getValue(XmlTag.CPL_STATUS);
			String type = attrib.getValue(XmlTag.TYPE);

			try {
				DirectPrimitive primitive = (DirectPrimitive) directPrimitiveIterator.next();
				if (primitive instanceof DirectAccess){

					currentDirectAccess = (DirectAccess) directPrimitiveIterator.next();
					if( !(currentDirectAccess.getType().equals(type)) ) {
						// Wrong action, something is wrong with the ordering
						currentDirectAction.setCplStatus( "WRONG RESPONSE ORDERING");
						currentDirectAction = null;
					} else {
						currentDirectAction.setCplStatus( cplStatus );
						currentDirectAction.setType(type);
					}
				} else {
					// Wrong action, something is wrong with the ordering
					currentDirectAction.setCplStatus( "WRONG RESPONSE ORDERING");
					currentDirectAction = null;					
				}
			} catch (NoSuchElementException e) {
				currentDirectAction = null;
			}
		}
	}


	public void endElement( String uri, String localName, String name) 
	throws SAXException {

		if( XmlTag.WRITE_RS.equals(name) ) {
			inWriteRs = false;
		}

		if( XmlTag.STATUS.equals(name) ) {

			if( inWriteRs && buffer != null ) {

				String bs = new String(buffer);
				task.setBadData( XmlTag.BAD_DATA.equals(bs) );

			}
		}

		if (XmlTag.DIRECT_ACTION.equals(name)) {
			try {
				if( ! inDirectAction || currentDirectAction == null ) return;
				currentDirectAction.setAbstractDataType( toAbstractDataType( ) );
			} catch(Exception ioe) {
				ioe.printStackTrace();
				throw new SAXException( ioe );
			} finally {
				buffer = new char[0];
				inDirectAction = false;
			}
		}

		if (XmlTag.DIRECT_ACCESS.equals(name)) {
			try {
				if( ! inDirectAccess || currentDirectAccess == null ) return;
				currentDirectAccess.setAbstractDataType( toAbstractDataType() );
			} catch(Exception ioe) {
				ioe.printStackTrace();
				throw new SAXException( ioe );
			} finally {
				buffer = new char[0];
				inDirectAccess = false;
			}
		}


	}

	private AbstractDataType toAbstractDataType( ) throws IOException {
		byte ba [] = new byte [buffer.length/2];
		for (int chi = 0, bai = 0; chi < buffer.length; chi+=2) {
			String sValue = "" + buffer[chi] + buffer[chi+1];
			ba[bai] = (byte) Integer.parseInt( sValue, 16);
			bai += 1;
		}
		if( ba.length > 0 ){
			try {
				return AXDRDecoder.decode( ba );
			} catch (IOException e) {
				if (currentDirectAction != null)
					currentDirectAction.setBinaryData(ba);
			}
		}
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

	static DirectPrimitiveSaxHandler parse( String xmlString, Task task) 
	throws IOException {

		try {
			DirectPrimitiveSaxHandler xmlHandler = 
				new DirectPrimitiveSaxHandler( task);

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

	
	private void debug(String message) {
		if( debug ) {
			System.out.println( message );
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
		result.append( "DirectPrimitiveSaxHandler [ \n" );
		result.append( "\n ]" );
		return result.toString();
	}

}
