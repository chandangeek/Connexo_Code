package com.energyict.genericprotocolimpl.iskrap2lpc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public class FileUtils {

	static public String convertFromDocToString(Document doc) throws IOException{
		
        try {
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer trans = tFactory.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			
			StringWriter sw = new StringWriter();
			StreamResult sr = new StreamResult(sw);
			DOMSource source = new DOMSource(doc);
			
			trans.transform(source, sr);
			return sw.toString();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
			throw new IOException("Could not parse document to string.");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new IOException("Could not set transformer properties.");
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
			throw new IOException("Could not transform the String to a streamResult.");
		} catch (TransformerException e) {
			e.printStackTrace();
			throw new IOException("Could not parse document to string.");
		}
        
	}
	
	/**
	 * Convert the given string to a zipped bytearray
	 * @param str the text in the file
	 * @param name the filename in the zipfile, not the name of the zipfile
	 * @return
	 * @throws IOException
	 */
	static public byte[] convertStringToZippedBytes(String str, String name) throws IOException{
		
		// create the zipped byteArray
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream sops = new ZipOutputStream(baos);
		
		try {
			sops.putNextEntry(new ZipEntry(name));
			sops.write(str.getBytes(), 0, str.length());
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not add the string to the zip file.");
		}
		
		baos.close();
		sops.close();
		
		return baos.toByteArray();
		
	}
	
	public static void main(String[] args){
		String str = "String to zip.";
		try {
			byte[] zipByte = FileUtils.convertStringToZippedBytes(str, "TestFile.txt");
			
			System.out.println(new String(zipByte));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
