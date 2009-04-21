package com.energyict.genericprotocolimpl.webrtukp.csvhandling;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.energyict.cbo.ApplicationException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.cosem.GenericInvoke;
import com.energyict.dlms.cosem.GenericRead;
import com.energyict.dlms.cosem.GenericWrite;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.RtuMessage;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.shadow.RtuMessageShadow;
import com.energyict.mdw.shadow.UserFileShadow;

public class CSVParser {
	
	private String rawText;
	private ArrayList lines = new ArrayList();
	
	public CSVParser(byte[] rawBytes){
		
		this.rawText = new String(rawBytes);
		parse();
	}

	private void parse(){
		int beginOffset = 0;
		int endOffset = this.rawText.indexOf(new String(new byte[]{0x0D, 0x0A}));
		while(endOffset != -1){
			lines.add(new TestObject(this.rawText.substring(beginOffset, endOffset)));
			beginOffset = endOffset + 2;
			endOffset = this.rawText.indexOf(new String(new byte[]{0x0D, 0x0A}), beginOffset+1);
		}
	}
	
	public TestObject getTestObject(int index){
		return (TestObject)this.lines.get(index);
	}
	
	public int size(){
		return this.lines.size();
	}
	
	public int getValidSize(){
		int count = 0;
		for(int i = 0; i < size(); i++){
			if(isValidLine((TestObject)this.lines.get(i))){
				count++;
			}
		}
		return count;
	}	
	
	public boolean isValidLine(TestObject to){
		switch(to.getType()){
		case 0 :{ // GET
			return true;
		}
		case 1 :{ // SET
			return true;
		}
		case 2 :{ // ACTION
			return true;
		}
		case 3 :{ // MESSAGE
			return true;
		}
		case 4:{ // WAIT
			return true;
		}
		case 5:{ // EMPTY line
			return false;
		}
		default:{
			return false;
		}
		}
	}
	
	public static void main(String args[])throws IOException{
		try {
//			Utilities.createEnvironment();
//			MeteringWarehouse.createBatchContext(false);
			MeteringWarehouse mw = MeteringWarehouse.getCurrent();
			int id = 460;
			UserFile uf = mw.getUserFileFactory().find(id);
			
			CSVParser csvParser = new CSVParser(uf.loadFileInByteArray());
			System.out.println(csvParser.rawText);
			System.out.println(((TestObject)csvParser.lines.get(1)).getObisCode());
			System.out.println(((TestObject)csvParser.lines.get(2)).getData());
			System.out.println(((TestObject)csvParser.lines.get(3)).getData());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public UserFileShadow convertResultToUserFile(UserFile uf) throws IOException{
		UserFileShadow ufs = new UserFileShadow();
		ufs.setName("Result - " + System.currentTimeMillis());
		ufs.setExtension("csv");
		ufs.setFolderId(uf.getFolderId());
		File file = File.createTempFile("Tempfile", ".csv");
		FileOutputStream fos = new FileOutputStream(file);
        fos.write(convertToByteArray());
        fos.close();
        file.deleteOnExit();
        ufs.setFile(file);
        return ufs;
	}
	
	private byte[] convertToByteArray(){
		int offset = 0;
		StringBuffer strBuffer = new StringBuffer();
		for(int i = 0; i < this.lines.size(); i++){
			TestObject to = (TestObject)this.lines.get(i);
			if(!to.getString(0).equalsIgnoreCase("")){
				for(int j = 0; j < to.size(); j++){
					strBuffer.append(to.getString(j));
					if(j != to.size()){
						strBuffer.append(";");
					}
				}
				strBuffer.append(new String(new byte[]{0x0D, 0x0A}));
			}
		}
		return strBuffer.toString().getBytes();
	}

	public void addLine(String string) {
		lines.add(new TestObject(string));
	}
}
