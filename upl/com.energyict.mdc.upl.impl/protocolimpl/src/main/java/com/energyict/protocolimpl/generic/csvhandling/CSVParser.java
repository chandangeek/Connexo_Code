package com.energyict.protocolimpl.generic.csvhandling;

import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.properties.DeviceMessageFile;

import com.energyict.mdw.shadow.UserFileShadow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CSVParser {

	private final DeviceMessageFileExtractor messageFileExtractor;
	private String rawText;
	private List<TestObject> lines = new ArrayList<>();

	public CSVParser(DeviceMessageFileExtractor messageFileExtractor) {
		this.messageFileExtractor = messageFileExtractor;
	}

	public void parse(byte[] rawBytes){
		this.rawText = new String(rawBytes);
		int beginOffset = 0;
		int endOffset = this.rawText.indexOf(new String(new byte[]{0x0D, 0x0A}));
		while(endOffset != -1){
			lines.add(new TestObject(this.rawText.substring(beginOffset, endOffset)));
			beginOffset = endOffset + 2;
			endOffset = this.rawText.indexOf(new String(new byte[]{0x0D, 0x0A}), beginOffset+1);
		}
	}

	public TestObject getTestObject(int index){
		return this.lines.get(index);
	}

	public int size(){
		return this.lines.size();
	}

	public int getValidSize(){
		int count = 0;
		for(int i = 0; i < size(); i++){
			if(isValidLine(this.lines.get(i))){
				count++;
			}
		}
		return count;
	}

	public boolean isValidLine(TestObject to){
		if(to.size() == 0){
			return false;
		}
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

	public UserFileShadow convertResultToUserFile(DeviceMessageFile deviceMessageFile, int folderId) throws IOException {
		UserFileShadow ufs = new UserFileShadow();
		ufs.setName(createFileName(this.messageFileExtractor.name(deviceMessageFile)));
		ufs.setExtension("csv");
		ufs.setFolderId(folderId);
		File file = File.createTempFile("Tempfile", ".csv");
		FileOutputStream fos = new FileOutputStream(file);
        fos.write(convertToByteArray());
        fos.close();
        file.deleteOnExit();
        ufs.setFile(file);
        return ufs;
	}

	private String createFileName(String ufName){
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());

		return "Result - " + ufName + " "
			+ cal.get(Calendar.YEAR) + "-"
			+ (cal.get(Calendar.MONTH)+1)+ "-"
			+ cal.get(Calendar.DAY_OF_MONTH) + " "
			+ cal.get(Calendar.HOUR_OF_DAY) + "h"
			+ cal.get(Calendar.MINUTE) + "m"
			+ cal.get(Calendar.SECOND) + "s";
	}


	private byte[] convertToByteArray(){
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < this.lines.size(); i++){
			TestObject to = this.lines.get(i);
			if(!"".equalsIgnoreCase(to.getString(0))){
				for(int j = 0; j < to.size(); j++){
					builder.append(to.getString(j));
					if(j != to.size()){
						builder.append(";");
					}
				}
				builder.append(new String(new byte[]{0x0D, 0x0A}));
			}
		}
		return builder.toString().getBytes();
	}

	public void addLine(String string) {
		lines.add(new TestObject(string));
	}
}
