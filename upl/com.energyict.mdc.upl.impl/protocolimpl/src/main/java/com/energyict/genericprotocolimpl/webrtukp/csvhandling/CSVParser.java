package com.energyict.genericprotocolimpl.webrtukp.csvhandling;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.shadow.UserFileShadow;
import com.energyict.protocolimpl.iec870.ziv5ctd.ByteArray;
import com.energyict.utils.Utilities;

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
	
	public static void main(String args[])throws IOException{
		try {
			Utilities.createEnvironment();
			MeteringWarehouse.createBatchContext(false);
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
			for(int j = 0; j <= 12; j++){
				strBuffer.append(to.getString(j));
				if(j != 12){
					strBuffer.append(";");
				}
			}
			strBuffer.append(new String(new byte[]{0x0D, 0x0A}));
		}
		return strBuffer.toString().getBytes();
	}
}
