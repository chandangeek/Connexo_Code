package com.energyict.genericprotocolimpl.iskrap2lpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
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
	
	static public String convertZippedBytesToString(byte[] zipped) throws IOException{
		ByteArrayInputStream bais = new ByteArrayInputStream(zipped);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipInputStream zis = new ZipInputStream(bais);
		ZipEntry zipEntry = zis.getNextEntry();
		long size = zipEntry.getSize();
		if(size == -1){
			size = zipEntry.getCompressedSize();
		}
		if(size == -1){
			size = 10*1024;
		}
		byte[] b = new byte[(int)size];
		int n;
		if(zipEntry != null){
			String name = zipEntry.getName();
//			while((n=zis.read(b)) > -1){
//				baos.write(n);
//			}
			zis.read(b);
		}
		return new String(b);
	}
	
	public static void main(String[] args){
		String str = "String to zip.";
		byte[] zipByte = new byte[]{};
		try {
			zipByte = FileUtils.convertStringToZippedBytes(str, "TestFile.txt");
			
			System.out.println(new String(zipByte));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		byte[] b = new byte[]{80, 75, 3, 4, 20, 0, 2, 0, 8, 0, 72, 97, -123, 57, -21, -3, -27, -52, -7, 1, 0, 0, 40, 12, 0, 0, 23, 0, 17, 0, 83, 117, 98, 95, 51, 56, 56, 54, 53, 48, 49, 
					48, 95, 50, 48, 48, 56, 49, 50, 46, 112, 108, 101, 85, 84, 13, 0, 7, 44, 26, 57, 73, 44, 26, 57, 73, 44, 26, 57, 73, -67, 87, -63, 110, -101, 64, 16, -67, 87, -22, 
					63, 32, -18, -63, 51, -77, -69, -80, 32, 92, -87, -78, 91, 41, 82, 84, 89, 73, -44, 67, 110, -85, 120, -29, 32, 17, 35, 1, 118, -43, -65, -17, 66, -20, -128, 85, 32,
					97, 11, -27, 96, 35, 121, -34, 123, 51, -13, 102, -76, -21, 120, -83, -113, -55, -93, 118, 94, -65, -82, -41, 75, -105, 73, -23, 11, 64, 112, -99, 13, -35, 108, 86,
					-41, 91, -67, 47, -105, 46, 103, -100, 73, 31, -104, -5, -27, -13, -89, -8, 86, 23, -121, -76, 44, -86, -41, 77, -98, 61, 37, -87, 118, 78, 97, 97, -24, -123, -46,
					-85, -80, 58, 79, -78, -19, -46, -123, 10, -32, -104, 39, -2, 118, 52, 17, -50, 90, -107, -6, 62, 121, -47, 75, -105, 0, -28, 21, -46, 21, -16, 123, -112, 17, 81, 4,
					-12, -32, 58, 119, -91, 42, 15, -123, -55, -126, 2, 41, 92, 103, -15, 65, 116, 104, 8, 90, 104, 36, 57, 6, -53, -126, 22, -42, -25, -93, -96, -95, 117, -46, 44, -120,
					-56, -73, 67, 35, 70, -126, 34, 4, 75, -76, 31, 33, 69, 66, -38, -96, 69, 93, 55, 70, 24, 116, -93, -29, -59, 105, 34, 122, -122, 35, -24, 30, -114, 77, -10, 75, -25,
					-105, -94, 55, -39, -82, -49, -86, 115, -56, 119, -107, -92, 125, -93, -80, 62, -28, -86, 76, -78, -67, 25, -122, -114, -44, 106, -47, 91, -67, 75, -118, 82, -25, -25,
					-4, -64, 67, -49, 76, -114, 39, -64, 35, 51, 4, 63, 85, 122, 48, -91, -93, -28, 110, 111, 39, -64, -88, -75, 59, 1, -83, 30, 14, -47, -61, 27, 61, -21, 33, 55, 14, 25,
					114, 36, 11, 114, 124, 35, 39, 9, -64, -63, -112, 34, 9, -16, 57, -56, -23, -59, -40, -116, -107, -80, -90, 77, -56, -32, -30, -103, 74, -117, 58, 44, -63, -23, -55,
					27, 75, 32, 0, 92, 65, 48, -67, 68, 99, 4, -47, -28, -20, -1, -63, 9, 54, 122, 57, -56, -122, -36, 118, 57, -84, -60, -40, -116, -107, -40, 90, 50, 66, -117, 119, 88,
					50, 3, 121, 99, -55, -12, -36, 108, -58, -60, 91, 14, 0, -52, -29, 64, -25, -127, 20, 14, -17, -9, -120, 3, 9, -68, -48, 55, 75, -34, -44, 65, 28, -19, -72, -65, 62,
					-106, -55, 49, 41, 127, -81, 84, -86, -9, 91, 117, -95, -127, -52, 124, -104, -32, -13, 111, 63, -44, -117, -82, -29, 107, -63, 65, 61, 81, 93, 113, 86, -49, 106, -65,
					-45, -35, 71, 112, 80, 93, -124, 4, 62, -4, 67, 42, 27, 85, 20, 117, 46, 108, 120, 73, 5, 31, -50, -91, 21, 53, -40, -14, -54, 81, 52, -45, -45, 52, -3, 125, -39, 15,
					-37, 25, -100, -82, 88, -17, 15, 61, 66, -124, -2, -33, -44, -15, -94, 117, -69, 95, -68, -2, 45, 48, -81, 127, 0, 80, 75, 1, 2, 23, 11, 20, 0, 2, 0, 8, 0, 72, 97, -123,
					57, -21, -3, -27, -52, -7, 1, 0, 0, 40, 12, 0, 0, 23, 0, 9, 0, 0, 0, 0, 0, 0, 0, 32, 0, -128, -127, 0, 0, 0, 0, 83, 117, 98, 95, 51, 56, 56, 54, 53, 48, 49, 48, 95, 50,
					48, 48, 56, 49, 50, 46, 112, 108, 101, 85, 84, 5, 0, 7, 44, 26, 57, 73, 80, 75, 5, 6, 0, 0, 0, 0, 1, 0, 1, 0, 78, 0, 0, 0, 63, 2, 0, 0, 0, 0};
		
		try {
			String result;
			result = FileUtils.convertZippedBytesToString(b);
			System.out.println(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
