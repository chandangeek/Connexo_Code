package com.energyict.genericprotocolimpl.iskragprs.imagetransfer;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.genericprotocolimpl.iskragprs.IskraMx37x;
import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.util.logging.Level;

/**
 * 
 * @author gna
 *
 *	TODO Complete this object. The getMissingBlocks object doesn't seem to work after sending all the blocks.
 * Try a different approach by reading the imageMissingBlocks object instead of the firstMissingBlock object.	
 *
 */
public class ImageTransfer {
	
	private IskraMx37x iskraMx37x;
	private byte[] imageData;
	private int blockCount = -1;
	private int firstMissingBlocks = -1;
	private Unsigned32 imageMaxBlockSize = null;
	private ScriptTable imageUpgradeScriptTable = null;
	private int imb = -1;
	private int maxBlockRetryCount = 3;
	
	private static String IMAGE_BLOCK_SIZE = "0.0.128.101.1.255";
	private static String IMAGE_MISSING_BLOCKS = "0.0.128.101.2.255";
	private static String IMAGE_SCRIPT_TABLE = "0.0.10.1.253.255";
	private static String IMAGE_FIRST_MISSING_BLOCK_OFFSET = "0.0.128.101.3.255";
	private static String IMAGE_BLOCK_TRANSFER = "0.0.128.101.4.255";
	
	private static int METHOD_INITIATE_TRANSFER = 0x03;
	private static int METHOD_ACTIVATE_IMAGE = 0x04;
	
	public ImageTransfer (IskraMx37x iskraMx37x){
		this.iskraMx37x = iskraMx37x;
	}
	
	public void upgradeImage(byte[] imageData) throws IOException{
		this.imageData = imageData;
		
		try {
			
			// Step 1: initiate upgrade
			initiateImageTransfer();
			
			// Step 2: transfer image blocks
			transferImageBlocks();
			
			// Step 3: check missing blocks // It is NOT a broadcast so no blocks can be missing...
			checkMissingBlocks2();
			
			// Step 4: activate image
			activateImage();
			
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
		
	}
	
	private byte[] createEmptyByteArray(byte[] b){
		for(int i = 0; i < b.length; i++){
			b[i] = (byte)0xFF;
		}
		return b;
	}

	/**
	 * @return the total image size
	 */
	private int imageSize(){
		return this.imageData.length;
	}
	
	/**
	 * Fetch the maximum block size from the device
	 * @return
	 * @throws IOException
	 */
	private long getImageBlockSize() throws IOException{
		try {
			if(this.imageMaxBlockSize == null){
				this.imageMaxBlockSize = getCosemObjectFactory().getData(ObisCode.fromString(IMAGE_BLOCK_SIZE)).getAttrbAbstractDataType(2).getUnsigned32();
			}
			return this.imageMaxBlockSize.getValue();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not fetch the maximum block size." + e.getMessage());
		}
	}
	
	/**
	 * @return the image ScriptTable
	 * @throws IOException
	 */
	private ScriptTable getImageScriptTable() throws IOException{
		if(this.imageUpgradeScriptTable == null){
			this.imageUpgradeScriptTable = getCosemObjectFactory().getScriptTable(ObisCode.fromString(IMAGE_SCRIPT_TABLE));
		}
		return this.imageUpgradeScriptTable;
	}
	
	private CosemObjectFactory getCosemObjectFactory(){
		return this.iskraMx37x.getCosemObjectFactory();
	}
	
	/**
	 * Initiate the image transfer by executing script number three
	 * @throws IOException
	 */
	private void initiateImageTransfer() throws IOException{
		try {
			getImageScriptTable().execute(METHOD_INITIATE_TRANSFER);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not initiate the image transfer." + e.getMessage());
		}
	}
	
	/**
	 * Activate the transfered image by executing script number four
	 * @throws IOException
	 */
	private void activateImage() throws IOException {
		try {
			getImageScriptTable().execute(METHOD_ACTIVATE_IMAGE);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not activate the image." + e.getMessage());
		}
	}
	
	/**
	 * Transfer all imageblocks to the device
	 * @throws IOException
	 */
	private void transferImageBlocks() throws IOException{
		byte[] sendImageData = null;
		OctetString imageOctetString = null;
		int blockOffset = 0;
		byte[] transferBlockOffset;
		long blockSize = 0;
		for(int i = 0; i < getBlockCount(); i++){
			if(i  < getBlockCount() - 1){
				blockSize = getImageBlockSize();
			} else {
				blockSize = this.imageSize() - (i*getImageBlockSize());
			}
			
			sendImageData = new byte[(int) getImageBlockSize()];
			sendImageData = createEmptyByteArray(sendImageData);
			System.arraycopy(this.imageData, (int)(i*getImageBlockSize()), sendImageData, 0, (int)blockSize);
			
			blockOffset = (int)(i * getImageBlockSize());
			transferBlockOffset = getByteBlockOffset(blockOffset);
			byte[] prefixOsLength = DLMSUtils.getAXDRLengthEncoding(sendImageData.length + 2 + 4);
			byte[] prefix;
			if(prefixOsLength.length == 2){
				prefix = new byte[]{0x09, prefixOsLength[0], prefixOsLength[1]};
			} else {
				prefix = new byte[]{0x09, prefixOsLength[0]};
			}
			
			byte[] temp = new byte[prefix.length + transferBlockOffset.length + sendImageData.length + 2]; // the 2 represents the CRC
			byte[] tempData = new byte[transferBlockOffset.length + sendImageData.length];
			System.arraycopy(prefix, 0, temp, 0, prefix.length);
			System.arraycopy(transferBlockOffset, 0, tempData, 0, transferBlockOffset.length);
			System.arraycopy(sendImageData, 0, tempData, transferBlockOffset.length, sendImageData.length);
			System.arraycopy(tempData, 0, temp, prefix.length, tempData.length);
			int calculatedCrc = calcCRC(tempData);
			System.arraycopy(new byte[]{highByte(calculatedCrc), lowByte(calculatedCrc)}, 0, temp, temp.length-2, 2);
			imageOctetString = new OctetString(temp, 0);

			writeImageBlock(imageOctetString);
			
			if(i % 50 == 0){ // i is multiple of 50
				this.iskraMx37x.getLogger().log(Level.INFO, "ImageTransfer: " + i + " of " + getBlockCount() + " blocks are sent to the device.");
			}
			
		}
	}
	
	/**
	 * Calculate the blockOffset in four bytes to place in front of the imagedata ...
	 * @param blockOffset
	 * @return
	 */
	private byte[] getByteBlockOffset(int blockOffset) {
		byte[] offset = new byte[4];
		offset[0] = (byte) ((blockOffset>>24)&0xFF);
		offset[1] = (byte) ((blockOffset>>16)&0xFF);
		offset[2] = (byte) ((blockOffset>>8)&0xFF);
		offset[3] = (byte) (blockOffset&0xFF);
		return offset;
	}

	/**
	 * Calculate the amount of blocks to send
	 * @return
	 * @throws IOException
	 */
	private int getBlockCount() throws IOException {
		if(this.blockCount == -1){
			this.blockCount = (int) (this.imageSize()/getImageBlockSize()) + (this.imageSize()%getImageBlockSize()==0?0:1);
		}
		return this.blockCount;
	}
	
	/**
	 * Write a given octetString containing the blockOffset and the image data
	 * @param imageData
	 * @throws IOException
	 */
	private void writeImageBlock(OctetString imageData) throws IOException{
		try {
			Data imgData = getCosemObjectFactory().getData(ObisCode.fromString(IMAGE_BLOCK_TRANSFER));
			imgData.setValueAttr(imageData);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not write the imageDataBlock." + e.getMessage());	
		}
	}
	
	private void writeImageBlock(Array imgArray) throws IOException{
		try {
			Data imgData = getCosemObjectFactory().getData(ObisCode.fromString(IMAGE_BLOCK_TRANSFER));
			imgData.setValueAttr(imgArray);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not write the imageDataBlock." + e.getMessage());	
		}
	}
	
	private void writeImageBlock(Structure struct) throws IOException{
		try {
			Data imgData = getCosemObjectFactory().getData(ObisCode.fromString(IMAGE_BLOCK_TRANSFER));
			imgData.setValueAttr(struct);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not write the imageDataBlock." + e.getMessage());	
		}
	}
	
	private void checkMissingBlocks2() throws IOException {
		int previousMissingBlock = -1;
		int retryBlock = 0;
		byte[] sendImageData = null;
		OctetString imageOctetString = null;
		int blockOffset = 0;
		byte[] transferBlockOffset;
		long blockSize = 0;
		try {
			while(readImageMissingBlocks() < getBlockCount()){
				if(previousMissingBlock == getImageMissingBlocks()){
					if(retryBlock++ == this.maxBlockRetryCount){
						throw new IOException("Exceeding the maximum retry for block " + getImageMissingBlocks() + ", Image transfer is canceled.");
					}
				} else {
					previousMissingBlock = getImageMissingBlocks();
					retryBlock = 0;
				}
				
				if(getImageMissingBlocks()  < getBlockCount() - 1){
					blockSize = getImageBlockSize();
				} else {
					blockSize = this.imageSize() - (getImageMissingBlocks()*getImageBlockSize());
				}
				
				sendImageData = new byte[(int) getImageBlockSize()];
				sendImageData = createEmptyByteArray(sendImageData);
				System.arraycopy(this.imageData, (int)(getImageMissingBlocks()*getImageBlockSize()), sendImageData, 0, (int)blockSize);
				
				blockOffset = (int)(getImageMissingBlocks() * getImageBlockSize());
				transferBlockOffset = getByteBlockOffset(blockOffset);
				byte[] temp = new byte[transferBlockOffset.length + sendImageData.length + 2 + 3];
				byte[] tempData = new byte[transferBlockOffset.length + sendImageData.length];
				System.arraycopy(new byte[]{0x09,(byte)0x81,(byte)0x86}, 0, temp, 0, 3);
				System.arraycopy(transferBlockOffset, 0, tempData, 0, transferBlockOffset.length);
				System.arraycopy(sendImageData, 0, tempData, transferBlockOffset.length, sendImageData.length);
				System.arraycopy(tempData, 0, temp, 3, tempData.length);
				int calculatedCrc = calcCRC(tempData);
				System.arraycopy(new byte[]{highByte(calculatedCrc), lowByte(calculatedCrc)}, 0, temp, temp.length-2, 2);
				imageOctetString = new OctetString(temp, 0);
				
				this.iskraMx37x.getLogger().log(Level.INFO, "ImageTransfer: Retransmitting imageblock nr. " + getImageMissingBlocks());
				writeImageBlock(imageOctetString);
				
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not complete 'Step3 : Check Missing Blocks'" + e.getMessage());
		}
	}
	
	/**
	 * Check for missing blocks, if you find missing blocks then resent that one
	 * @throws IOException
	 */
	private void checkMissingBlocks() throws IOException {
		int previousMissingBlock = -1;
		int retryBlock = 0;
		byte[] sendImageData = null;
		OctetString imageOctetString = null;
		int blockOffset = 0;
		byte[] transferBlockOffset;
		try {
			while(readFirstMissingBlocks() < getBlockCount()){
				if(previousMissingBlock == getFirstMissingBlocks()){
					if(retryBlock++ == this.maxBlockRetryCount){
						throw new IOException("Exceeding the maximum retry for block " + getFirstMissingBlocks() + ", Image transfer is canceled.");
					}
				} else {
					previousMissingBlock = getFirstMissingBlocks();
					retryBlock = 0;
				}
				
				if(getFirstMissingBlocks() < getBlockCount() - 1){
					sendImageData = new byte[(int) getImageBlockSize() + 4];
					System.arraycopy(this.imageData, (int)(getFirstMissingBlocks()*getImageBlockSize()), sendImageData, 4, (int)getImageBlockSize());
				} else {
					long blockSize = this.imageSize() - (getFirstMissingBlocks()*getImageBlockSize());
					sendImageData = new byte[(int)blockSize + 4];
					System.arraycopy(this.imageData, (int)(getFirstMissingBlocks()*getImageBlockSize()), sendImageData, 4, (int)blockSize);
				}

				Array imgArray = new Array();
				blockOffset = (int)(getFirstMissingBlocks() * getImageBlockSize());
				transferBlockOffset = getByteBlockOffset(blockOffset);
				imgArray.addDataType(OctetString.fromByteArray(transferBlockOffset));
				imgArray.addDataType(OctetString.fromByteArray(sendImageData));
				
				this.iskraMx37x.getLogger().log(Level.INFO, "ImageTransfer: Retransmitting imageblock nr. " + getFirstMissingBlocks());
				
				writeImageBlock(imgArray);
				
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not complete 'Step3 : Check Missing Blocks'" + e.getMessage());
		}
	}

	/**
	 * Get the first missing block number from the device
	 * @return
	 * @throws IOException
	 */
	private int readFirstMissingBlocks() throws IOException {
		try {
			Data fmbData = getCosemObjectFactory().getData(ObisCode.fromString(IMAGE_FIRST_MISSING_BLOCK_OFFSET));
			this.firstMissingBlocks = (int)fmbData.getValue();
			return this.firstMissingBlocks;
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not read the first MISSING block number." + e.getMessage());
		}
	}
	
	/** 
	 * Return the first missing block, don't read it again from the device
	 * @return
	 * @throws IOException 
	 */
	private int getFirstMissingBlocks() throws IOException {
		if(this.firstMissingBlocks == -1){
			return readFirstMissingBlocks();
		} else {
			return this.firstMissingBlocks;
		}
	}
	
	/**
	 * Get the missingBlocks from the device
	 * @return
	 * @throws IOException
	 */
	private int readImageMissingBlocks() throws IOException{
		try {
			Array imbArray = new Array(getCosemObjectFactory().getGenericRead(ObisCode.fromString(IMAGE_MISSING_BLOCKS), DLMSUtils.attrLN2SN(2), 1).getResponseData(),0,0);
			OctetString os;
			for(int i = 0; i < imbArray.nrOfDataTypes(); i++){
				os = (OctetString) imbArray.getDataType(i);
				for(int j = 0; j < os.getOctetStr().length; j++){
					byte b = os.getOctetStr()[j];
					for(int k = 0; k < 8; k++){
						if((b&(byte)(Math.pow(2, k))) != (byte)Math.pow(2, k)){
							this.imb = (i*128*8) + (j*8) + k;
							return imb;
						}
					}
				}
			}
			return getBlockCount();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not read the ImageMissingBlocks." + e.getMessage());
		}
	}
	
	private int getImageMissingBlocks() throws IOException{
		if(this.imb == -1){
			return readImageMissingBlocks();
		} else {
			return this.imb;
		}
	}
	
	public static void main(String args[]) throws IOException {
//		String str = "00024400" + 
//						"0EAE846FEA0EE7D128867D58FCFFFFFF" +
//						"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF" + 
//						"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF" +
//						"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF" +
//						"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF" +
//						"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF" +
//						"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF" +
//						"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";
//		
//		ImageTransfer it = new ImageTransfer(null);
//		int crc = it.calcCRC(ParseUtils.hexStringToByteArray(str));
//		byte high = it.highByte(crc);
//		byte low = it.lowByte(crc);
//		
//		
//		System.out.println("Done");
		
		String str = "0102098180ffff"+
			"ffffffffffffffffffffffffffffffff"+
			"ffffffffffffffffffffffffffffffff"+
			"ffffdfffffffffffffffffffffffffff"+
			"ffffffffffffffffffffffffffffffff"+
			"ffffffffffffffffffffffffffffffff"+
			"ffffffffffffffffffffffffffffffff"+
			"ffffffffffffffffffffffffffffffff"+
			"ffffffffffffffffffffffffffff0954"+
			"ffffffffffffffffffffffffffffffff"+
			"3f000000000000000000000000000000"+
			"00000000000000000000000000000000"+
			"00000000000000000000000000000000"+
			"00000000000000000000000000000000"+
			"00000000";
		
		ImageTransfer it = new ImageTransfer(null);
		Array array = new Array(ParseUtils.hexStringToByteArray(str), 0, 0);
		OctetString os;
		for(int i = 0; i < array.nrOfDataTypes(); i++){
			os = (OctetString) array.getDataType(i);
			for(int j = 0; j < os.getOctetStr().length; j++){
				byte b = os.getOctetStr()[j];
				for(int k = 0; k < 8; k++){
					if((b&(byte)(Math.pow(2, k))) != (byte)Math.pow(2, k)){
//						this.imb = (((i+1) * (j+1) * 8) * (k+1)) - 1;
						it.imb = (i*128*8) + (j*8) + k;
					}
				}
			}
		}
		
		
	}
	
	/******************************************************************************************************************************/
    private int calcCRC(byte[] buffer) {
    	int crc = 0;
    		int iLength = buffer.length;
//    		crc = 0xFFFF;
    		
    		for (int i=0; i < iLength; i++) {
    			int iCharVal = buffer[i] & 0xFF;
    			crc = ((crc >> 8) ^ crc_tab[(crc ^ iCharVal) & 0xFF]) & 0xFFFF;
    		}
//    		crc ^= 0xFFFF;
//    		crc = ((lowByte(crc) << 8) & 0xFF00) | (highByte(crc) & 0xFF);
    		
         return crc;
    }
    
    private byte highByte(int in) {
    	return (byte) ((in >> 8) & 0xFF); 
    }
    
    private byte lowByte(int in) {
    	return (byte) (in & 0xFF);
    }
    
    private int[] crc_tab={
            0x0000, 0x1189, 0x2312, 0x329b, 0x4624, 0x57ad, 0x6536, 0x74bf,
            0x8c48, 0x9dc1, 0xaf5a, 0xbed3, 0xca6c, 0xdbe5, 0xe97e, 0xf8f7,
            0x1081, 0x0108, 0x3393, 0x221a, 0x56a5, 0x472c, 0x75b7, 0x643e,
            0x9cc9, 0x8d40, 0xbfdb, 0xae52, 0xdaed, 0xcb64, 0xf9ff, 0xe876,
            0x2102, 0x308b, 0x0210, 0x1399, 0x6726, 0x76af, 0x4434, 0x55bd,
            0xad4a, 0xbcc3, 0x8e58, 0x9fd1, 0xeb6e, 0xfae7, 0xc87c, 0xd9f5,
            0x3183, 0x200a, 0x1291, 0x0318, 0x77a7, 0x662e, 0x54b5, 0x453c,
            0xbdcb, 0xac42, 0x9ed9, 0x8f50, 0xfbef, 0xea66, 0xd8fd, 0xc974,
            0x4204, 0x538d, 0x6116, 0x709f, 0x0420, 0x15a9, 0x2732, 0x36bb,
            0xce4c, 0xdfc5, 0xed5e, 0xfcd7, 0x8868, 0x99e1, 0xab7a, 0xbaf3,
            0x5285, 0x430c, 0x7197, 0x601e, 0x14a1, 0x0528, 0x37b3, 0x263a,
            0xdecd, 0xcf44, 0xfddf, 0xec56, 0x98e9, 0x8960, 0xbbfb, 0xaa72,
            0x6306, 0x728f, 0x4014, 0x519d, 0x2522, 0x34ab, 0x0630, 0x17b9,
            0xef4e, 0xfec7, 0xcc5c, 0xddd5, 0xa96a, 0xb8e3, 0x8a78, 0x9bf1,
            0x7387, 0x620e, 0x5095, 0x411c, 0x35a3, 0x242a, 0x16b1, 0x0738,
            0xffcf, 0xee46, 0xdcdd, 0xcd54, 0xb9eb, 0xa862, 0x9af9, 0x8b70,
            0x8408, 0x9581, 0xa71a, 0xb693, 0xc22c, 0xd3a5, 0xe13e, 0xf0b7,
            0x0840, 0x19c9, 0x2b52, 0x3adb, 0x4e64, 0x5fed, 0x6d76, 0x7cff,
            0x9489, 0x8500, 0xb79b, 0xa612, 0xd2ad, 0xc324, 0xf1bf, 0xe036,
            0x18c1, 0x0948, 0x3bd3, 0x2a5a, 0x5ee5, 0x4f6c, 0x7df7, 0x6c7e,
            0xa50a, 0xb483, 0x8618, 0x9791, 0xe32e, 0xf2a7, 0xc03c, 0xd1b5,
            0x2942, 0x38cb, 0x0a50, 0x1bd9, 0x6f66, 0x7eef, 0x4c74, 0x5dfd,
            0xb58b, 0xa402, 0x9699, 0x8710, 0xf3af, 0xe226, 0xd0bd, 0xc134,
            0x39c3, 0x284a, 0x1ad1, 0x0b58, 0x7fe7, 0x6e6e, 0x5cf5, 0x4d7c,
            0xc60c, 0xd785, 0xe51e, 0xf497, 0x8028, 0x91a1, 0xa33a, 0xb2b3,
            0x4a44, 0x5bcd, 0x6956, 0x78df, 0x0c60, 0x1de9, 0x2f72, 0x3efb,
            0xd68d, 0xc704, 0xf59f, 0xe416, 0x90a9, 0x8120, 0xb3bb, 0xa232,
            0x5ac5, 0x4b4c, 0x79d7, 0x685e, 0x1ce1, 0x0d68, 0x3ff3, 0x2e7a,
            0xe70e, 0xf687, 0xc41c, 0xd595, 0xa12a, 0xb0a3, 0x8238, 0x93b1,
            0x6b46, 0x7acf, 0x4854, 0x59dd, 0x2d62, 0x3ceb, 0x0e70, 0x1ff9,
            0xf78f, 0xe606, 0xd49d, 0xc514, 0xb1ab, 0xa022, 0x92b9, 0x8330,
            0x7bc7, 0x6a4e, 0x58d5, 0x495c, 0x3de3, 0x2c6a, 0x1ef1, 0x0f78
        };
	
}
