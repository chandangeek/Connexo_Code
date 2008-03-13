package com.energyict.genericprotocolimpl.lgadvantis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConstructXmlFiles {

	public static String getResetAndDiscoverXml(){
		return resetAndDiscoverXml;
	}
	
	public static String getResetXml(){
		return resetXml;
	}
	
	public static String getDiscoverXml(){
		return discoverXml;
	}
	
	public static String getInitiatePerformanceTestXml(){
		return initiatePerformanceTestXml;
	}
	
	public static String getIndustrialRead(String serialNumber){
		return industrialReadPart1+serialNumber+industrialReadPart2;
	}
	
	public static String getIndustrialWrite(String serialNumber,String request){
		if (request.equals("10W")){
			return industrialWritePart1+serialNumber+industrialWritePart2+load10W+industrialWritePart3;
		} else {
			return industrialWritePart1+serialNumber+industrialWritePart2+load60kW+industrialWritePart3;
		}
			
	}
	
	public static String getXmlFile(String filename) throws FileNotFoundException, IOException{
		File file = new File(filename);
		FileInputStream fstream = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fstream);
		BufferedReader in = new BufferedReader(isr);
		String result = "";
		for (String line = in.readLine(); line != null; line = in.readLine()){
			result += line;
		}
		in.close();
		return result;

	}
	

	private final static String initiatePerformanceTestXml = 
		"<?xml version=\"1.0\"?>" +
		"<releve-cpl version=\"1.0\">" +
		"<telerel-rq ident=\"040000000001\">" +
		"<write-rq>" +
		"<exp-cmd>" +
		"<start-task0/>" +
		"</exp-cmd>" +
		"</write-rq>" +
		"</telerel-rq>" +
		"</releve-cpl>";

	
	private final static String resetAndDiscoverXml = 
		"<?xml version=\"1.0\"?>" +
		"<releve-cpl version=\"1.0\">" +
		"<telerel-rq ident=\"040000000001\">" +
		"<write-rq>" +
		"<exp-cmd>" +
		"<net-find/>" +
		"</exp-cmd>" +
		"</write-rq>" +
		"<write-rq>" +
		"<def-cmd>" +
		"<ee-data-type name=\"WALL-RESET-NEW-NOTSYNCHRO\" type-id=\"WALLMIB\" id=\"0080\">" +
		"<data name=\"data\" size=\"2\" format=\"opaque\"/>" +
		"</ee-data-type>" +
		"</def-cmd>" +
		"</write-rq>" +
		"<write-rq>" +
		"<task-cmd>" +
		"<definition id=\"0\">" +
		"<cr-compute>" +
		"<expr var1=\"datawrite\" op=\"\" var2=\"\" name=\"WALL-RESET-NEW-NOTSYNCHRO.data\"/>" +
		"</cr-compute>" +
		"</definition>" +
		"<target id=\"0\">" +
		"</target>" +
		"<datawrite id=\"0\">" +
		"<content>0000</content>" +
		"</datawrite>" +
		"</task-cmd>" +
		"</write-rq>" +
		"<write-rq>" +
		"<exp-cmd>" +
		"<start-task0/>" +
		"</exp-cmd>" +
		"</write-rq>" +
		"<write-rq>" +
		"<def-cmd>" +
		"<ee-data-type name=\"WALL-RESET-NEW-NOTSYNCHRO\" type-id=\"WALLMIB\" id=\"0080\">" +
		"</ee-data-type>" +
		"</def-cmd>" +
		"</write-rq>" +
		"<write-rq>" +
		"<task-cmd>" +
		"<definition id=\"0\">" +
		"</definition>" +
		"<target id=\"0\">" +
		"</target>" +
		"<datawrite id=\"0\">" +
		"</datawrite>" +
		"</task-cmd>" +
		"</write-rq>" +
		"</telerel-rq>" +
		"</releve-cpl>";

	private final static String resetXml = 
		"<?xml version=\"1.0\"?>" +
		"<releve-cpl version=\"1.0\">" +
		"<telerel-rq ident=\"040000000001\">" +
		"<write-rq>" +
		"<def-cmd>" +
		"<ee-data-type name=\"WALL-RESET-NEW-NOTSYNCHRO\" type-id=\"WALLMIB\" id=\"0080\">" +
		"<data name=\"data\" size=\"2\" format=\"opaque\"/>" +
		"</ee-data-type>" +
		"</def-cmd>" +
		"</write-rq>" +
		"<write-rq>" +
		"<task-cmd>" +
		"<definition id=\"0\">" +
		"<cr-compute>" +
		"<expr var1=\"datawrite\" op=\"\" var2=\"\" name=\"WALL-RESET-NEW-NOTSYNCHRO.data\"/>" +
		"</cr-compute>" +
		"</definition>" +
		"<target id=\"0\">" +
		"</target>" +
		"<datawrite id=\"0\">" +
		"<content>0000</content>" +
		"</datawrite>" +
		"</task-cmd>" +
		"</write-rq>" +
		"<write-rq>" +
		"<exp-cmd>" +
		"<start-task0/>" +
		"</exp-cmd>" +
		"</write-rq>" +
		"<write-rq>" +
		"<def-cmd>" +
		"<ee-data-type name=\"WALL-RESET-NEW-NOTSYNCHRO\" type-id=\"WALLMIB\" id=\"0080\">" +
		"</ee-data-type>" +
		"</def-cmd>" +
		"</write-rq>" +
		"<write-rq>" +
		"<task-cmd>" +
		"<definition id=\"0\">" +
		"</definition>" +
		"<target id=\"0\">" +
		"</target>" +
		"<datawrite id=\"0\">" +
		"</datawrite>" +
		"</task-cmd>" +
		"</write-rq>" +
		"</telerel-rq>" +
		"</releve-cpl>";
	
	private final static String discoverXml = 
		"<?xml version=\"1.0\"?>" +
		"<releve-cpl version=\"1.0\">" +
		"<telerel-rq ident=\"040000000001\">" +
		"<write-rq>" +
		"<exp-cmd>" +
		"<net-find/>" +
		"</exp-cmd>" +
		"</write-rq>" +
		"</telerel-rq>" +
		"</releve-cpl>";

	private final static String industrialReadPart1 =
		"<?xml version=\"1.0\"?>" +
		"<releve-cpl version=\"1.0\">" +
		"<telerel-rq ident=\"040000000001\">" +
		"<write-rq>" +
		"<def-cmd>" +
		"<ee-data-type name=\"TAB02\" type-id=\"TAB\" id=\"0002\">" +
		"<data name=\"date-debutP\" size=\"3\" big-endian=\"0\" format=\"hex\" list-min=\"1\" list-max=\"1\"/>" +
		"<data name=\"heur-debutP\" size=\"2\" big-endian=\"0\" format=\"hex\" list-min=\"1\" list-max=\"1\"/>" +
		"<data name=\"tarif\" size=\"1\" big-endian=\"0\" format=\"hex\" list-min=\"1\" list-max=\"1\"/>" +
		"<data name=\"RegEner_P\" size=\"3\" big-endian=\"0\" format=\"uint\" list-min=\"6\" list-max=\"6\"/>" +
		"<data name=\"MinDep_P\" size=\"2\" big-endian=\"0\" format=\"uint\" list-min=\"4\" list-max=\"4\"/>" +
		"<data name=\"PApp_P\" size=\"2\" big-endian=\"0\" format=\"uint\" list-min=\"4\" list-max=\"4\"/>" +
		"<data name=\"PSous_P\" size=\"2\" big-endian=\"0\" format=\"uint\" list-min=\"4\" list-max=\"4\"/>" +
		"<data name=\"K_P\" size=\"1\" big-endian=\"0\" format=\"uint\" list-min=\"4\" list-max=\"4\"/>" +
		"<data name=\"OptTar_P1\" size=\"1\" big-endian=\"0\" format=\"hex\" list-min=\"1\" list-max=\"1\"/>" +
		"<data name=\"TFonc\" size=\"2\" big-endian=\"0\" format=\"uint\" list-min=\"4\" list-max=\"4\"/>" +
		"</ee-data-type>" +
		"<cr-data-type name=\"type_CRTAB02\" archi-dim=\"1\">" +
		"<data name=\"Energie\" size=\"3\" big-endian=\"0\" format=\"uint\" list-min=\"6\" list-max=\"6\"/>" +
		"</cr-data-type>" +
		"<ee-data-type name=\"TAB0E\" type-id=\"TAB\" id=\"0014\">" +
		"<data name=\"Date\" size=\"3\" big-endian=\"0\" format=\"hex\" list-min=\"1\" list-max=\"1\"/>" +
		"<data name=\"Heure\" size=\"2\" big-endian=\"0\" format=\"hex\" list-min=\"1\" list-max=\"1\"/>" +
		"<data name=\"Ete-Hiver\" size=\"2\" big-endian=\"0\" format=\"hex\" list-min=\"1\" list-max=\"1\"/>" +
		"<data name=\"Hiver-Ete\" size=\"2\" big-endian=\"0\" format=\"hex\" list-min=\"1\" list-max=\"1\"/>" +
		"</ee-data-type>" +
		"<cr-data-type name=\"type_CRTAB0E\" archi-dim=\"1\">" +
		"<data name=\"Date\" size=\"3\" big-endian=\"0\" format=\"hex\" list-min=\"1\" list-max=\"1\"/>" +
		"<data name=\"Heure\" size=\"2\" big-endian=\"0\" format=\"hex\" list-min=\"1\" list-max=\"1\"/>" +
		"</cr-data-type>" +
		"<ee-type id-type=\"10\">" +
		"<local-data name=\"CI_TAB02\" type=\"type_CRTAB02\"/>" +
		"<local-data name=\"CI_TAB0E\" type=\"type_CRTAB0E\"/>" +
		"</ee-type>" +
		"</def-cmd>" +
		"<task-cmd>" +
		"<definition id=\"0\">" +
		"<cr-compute>" +
		"<expr var1=\"TAB02.RegEner_P\" op=\"\" var2=\"\" name=\"CI_TAB02.Energie\"/>" +
		"<expr var1=\"TAB0E.Date\" op=\"\" var2=\"\" name=\"CI_TAB0E.Date\"/>" +
		"<expr var1=\"TAB0E.Heure\" op=\"\" var2=\"\" name=\"CI_TAB0E.Heure\"/>" +
		"</cr-compute>" +
		"</definition>" +
		"<target id=\"0\">" +
		"<ident>";
		
	private final static String industrialReadPart2 =
		"</ident>" +
		"</target>" +
		"</task-cmd>" +
		"</write-rq>" +
		"<write-rq>" +
		"<exp-cmd>" +
		"<start-task0/>" +
		"</exp-cmd>" +
		"</write-rq>" +
		"</telerel-rq>" +
		"</releve-cpl>";

	private final static String industrialWritePart1 =
		"<?xml version=\"1.0\"?>" +
		"<releve-cpl version=\"1.0\">" +
		"<telerel-rq ident=\"040000000001\">" +
		"<write-rq>" +
		"<def-cmd>" +
		"<ee-data-type name=\"TDPFC\" type-id=\"TDP\" id=\"0252\">" +
		"<data name=\"data\" size=\"26\" format=\"opaque\"/>" +
		"</ee-data-type>" +
		"</def-cmd>" +
		"<task-cmd>" +
		"<definition id=\"0\">" +
		"<cr-compute>" +
		"<expr var1=\"datawrite\" op=\"\" var2=\"\" name=\"TDPFC.data\"/>" +
		"</cr-compute>" +
		"</definition>" +
		"<target id=\"0\">" +
		"<ident>";
	
	private final static String industrialWritePart2 =
		"</ident>" +
		"</target>" +
		"<datawrite id=\"0\">" +
		"<content>";
	
	
	private final static String industrialWritePart3 =
		"</content>" +
		"</datawrite>" +
		"</task-cmd>" +
		"</write-rq>" +
		"<write-rq>" +
		"<exp-cmd>" +
		"<start-task0/>" +
		"</exp-cmd>" +
		"</write-rq>" +
		"</telerel-rq>" +
		"</releve-cpl>";

	private final static String load10W = "01010001000100010064646464FFFFFFFFFFFFFFFFFFFFFFFFFF";
	
	private final static String load60kW = "01701770177017701764646464FFFFFFFFFFFFFFFFFFFFFFFFFF";

}
