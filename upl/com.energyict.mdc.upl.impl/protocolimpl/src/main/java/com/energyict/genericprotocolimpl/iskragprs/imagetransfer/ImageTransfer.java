package com.energyict.genericprotocolimpl.iskragprs.imagetransfer;

import java.io.IOException;

import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.ScriptTable;
import com.energyict.genericprotocolimpl.iskragprs.IskraMx37x;
import com.energyict.obis.ObisCode;

public class ImageTransfer {
	
	private IskraMx37x iskraMx37x;
	private byte[] imageData;
	private Unsigned32 imageMaxBlockSize = null;
	private ScriptTable imageUpgradeScriptTable = null;
	
	private static String IMAGE_BLOCK_SIZE = "0.0.128.101.1.255";
	private static String IMAGE_MISSING_BLOCKS = "0.0.128.101.2.255";
	private static String IMAGE_SCRIPT_TABLE = "0.0.128.101.253.255";
	private static String IMAGE_FIRST_MISSING_BLOCK_OFFSET = "0.0.128.101.3.255";
	private static String IMAGE_BLOCK_TRANSFER = "0.0.128.101.4.255";
	
	private static int METHOD_INITIATE_TRANSFER = 3;
	private static int METHOD_ACTIVATE_IMAGE = 4;
	
	public ImageTransfer (IskraMx37x iskraMx37x){
		this.iskraMx37x = iskraMx37x;
	}
	
	public void upgradeImage(byte[] imageData) throws IOException{
		this.imageData = imageData;
		
		try {

			// Step 1: initiate upgrade
			initiateImageTransfer();
			
			// TODO complete the imageObject
			
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
		
	}

	private int imageSize(){
		return this.imageData.length;
	}
	
	private long getImageBlockSize() throws IOException{
		try {
			if(this.imageMaxBlockSize == null){
				this.imageMaxBlockSize = getCosemObjectFactory().getData(ObisCode.fromString("0.0.128.101.1.255")).getAttrbAbstractDataType(2).getUnsigned32();
			}
			return this.imageMaxBlockSize.getValue();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not fetch the maximum block size." + e.getMessage());
		}
	}
	
	private ScriptTable getImageScriptTable() throws IOException{
		if(this.imageUpgradeScriptTable == null){
			this.imageUpgradeScriptTable = getCosemObjectFactory().getScriptTable(ObisCode.fromString("0.0.128.101.253.255"));
		}
		return this.imageUpgradeScriptTable;
	}
	
	private CosemObjectFactory getCosemObjectFactory(){
		return this.iskraMx37x.getCosemObjectFactory();
	}
	
	private void initiateImageTransfer() throws IOException{
		try {
			getImageScriptTable().execute(METHOD_INITIATE_TRANSFER);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not initiate the image transfer." + e.getMessage());
		}
	}
}
