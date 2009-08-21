package com.energyict.dlms.cosem;

import java.io.IOException;
import java.util.logging.Level;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.obis.ObisCode;

/**
 * 
 * @author gna
 * 
 * The Image transfer takes place in several steps: 
 * Step 1:   The client gets the ImageBlockSize from each server individually; 
 * Step 2:   The client initiates the Image transfer process individually or using broadcast; 
 * Step 3:   The client transfers ImageBlocks to (a group of) server(s) individually or using  broadcast; 
 * Step 4:   The client checks the completeness of the Image in each server individually and transfers any ImageBlocks not (yet) transferred; 
 * Step 5:   The Image is verified; 
 * Step 6:   Before activation, the Image is checked; 
 * Step 7:   The Image(s) is (are) activated. 
 * 
 */

public class ImageTransfer extends AbstractCosemObject{

	static public boolean DEBUG = false;
	static public final int CLASSID = 18;
	static private int delay = 3000;
	private int maxBlockRetryCount = 3;
	private int maxTotalRetryCount = 500;
	
	private ProtocolLink protocolLink;

	/** Attributes */
	private Unsigned32 imageMaxBlockSize = null; // holds the max size of the imageblocks to be sent to the server(meter)
	private BitString imageTransferBlocksStatus = null; // Provides information about the transfer status of each imageBlock (1=Transfered, 0=NotTransfered)
	private Unsigned32 imageFirstNotTransferedBlockNumber = null; // Provides the blocknumber of the first not transfered imageblock
	private BooleanObject imageTransferEnabled = null; // Controls enabling the image_transfer_proces
	private TypeEnum imageTransferStatus = null; // Holds the status of the image transfer process
	private Array imageToActivateInfo = null;	// Provides information on the image(s) ready for activation
	
	/** Attribute numbers */
	static private final int ATTRB_IMAGE_BLOCK_SIZE = 2;
	static private final int ATTRB_IMAGE_TRANSFER_BLOCK_STATUS = 3;
	static private final int ATTRB_IMAGE_FIRST_NOT_TRANSFERED_BLOCK = 4;
	static private final int ATTRB_IMAGE_TRANSFER_ENABLED = 5;
	static private final int ATTRB_IMAGE_TRANSFER_STATUS = 6;
	static private final int ATTRB_IMAGE_TO_ACTIVATE_INFO = 7;
	
	/** Method invoke */
	static private final int IMAGE_TRANSFER_INITIATE = 1;
	static private final int IMAGE_BLOCK_TRANSFER = 2;
	static private final int IMAGE_VERIFICATION = 3;
	static private final int IMAGE_ACTIVATION = 4;
	
	/** Image info */
	private Unsigned32 size = null; 	// the size of the image
	private byte[] data = null; // the complete image in byte
	private int blockCount = -1; // the amount of block numbers
	private OctetString imageIdentification = null;
	private OctetString imageSignature = null;
	
	static final byte[] LN=new byte[]{0,0,44,0,0,(byte)255};
	
	public ImageTransfer(ProtocolLink protocolLink) {
        super(protocolLink,new ObjectReference(LN));
        this.protocolLink = protocolLink;
    }
	
	public ImageTransfer(ProtocolLink protocolLink, ObjectReference objectReference) {
		super(protocolLink, objectReference);
		this.protocolLink = protocolLink;
	}

	static public ObisCode getObisCode() {
		return ObisCode.fromByteArray(LN) ;
	} 

	protected int getClassId() {
		return CLASSID;
	}
	
	/**
	 * Start the automatic upgrade procedure
	 * @param data - the image to transfer 
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public void upgrade(byte[] data) throws IOException, InterruptedException{
		this.data = data;
		this.size = new Unsigned32(data.length);
		
		// Set the imageTransferEnabledState to true
		writeImageTransferEnabledState(true);
		
		if(getImageTransferEnabledState().getState()){
			
			if(DEBUG) {
				System.out.println("ImageTrans: Enabled state is true.");
			}
			
			// Step1: Get the maximum image block size
			// and calculate the amount of blocks in one step
			this.blockCount = (int)(this.size.getValue()/readImageBlockSize().getValue()) + (((this.size.getValue()%readImageBlockSize().getValue())==0)?0:1);
			if(DEBUG) {
				System.out.println("ImageTrans: Maximum block size is: " + readImageBlockSize() + 
						", Number of blocks: " + blockCount + ".");
			}
			
			// Step2: Initiate the image transfer
			Structure imageInitiateStructure = new Structure();
			imageInitiateStructure.addDataType(OctetString.fromString("EICT-" + System.currentTimeMillis()));
			imageInitiateStructure.addDataType(this.size);
			imageTransferInitiate(imageInitiateStructure);
			if(DEBUG) {
				System.out.println("ImageTrans: Initialize success.");
			}
			
			
			// Step3: Transfer image blocks
			//TODO - TOTEST  
			transferImageBlocks();
			if(DEBUG) {
				System.out.println("ImageTrans: Transfered " + this.blockCount + " blocks.");
			}
			
			// Step4: Check completeness of the image and transfer missing blocks
			//TODO - Checking for missings is not necessary at the moment because we have a guaranteed connection,
			// Every block is confirmed by the meter
//			checkAndSendMissingBlocks();
			
			// Step5: Verify image
			//TODO - TOTEST
			verifyAndRetryImage();
			if(DEBUG) {
				System.out.println("ImageTrans: Verification successfull.");
			}
			
			this.protocolLink.getLogger().log(Level.INFO, "Start : " + System.currentTimeMillis());
			// Step6: Check image before activation
			// Skip this step
			
			// Step7: Activate image
			// This step is done in the ProtocolCode!
			
			
		} else {
			throw new IOException("Could not perform the upgrade because meter does not allow it.");
		}
		
	}
	
	/**
	 * Transfer all the image blocks to the meter
	 * @throws IOException
	 */
	private void transferImageBlocks() throws IOException {
		byte[] octetStringData = null;
		OctetString os = null;
		Structure imageBlockTransfer;
		for(int i = 0; i < blockCount; i++){
			if(i < blockCount -1){
				octetStringData = new byte[(int)readImageBlockSize().getValue()];
				System.arraycopy(this.data, (int)(i*readImageBlockSize().getValue()), octetStringData, 0, 
						(int)readImageBlockSize().getValue());
			} else {
				long blockSize = this.size.getValue() - (i*readImageBlockSize().getValue());
				octetStringData = new byte[(int)blockSize];
				System.arraycopy(this.data, (int)(i*readImageBlockSize().getValue()), octetStringData, 0, 
						(int)blockSize);
			}
//			os = new OctetString(trimByteArray(octetStringData));
			os = new OctetString(octetStringData);
			imageBlockTransfer = new Structure();
			imageBlockTransfer.addDataType(new Unsigned32(i));
			imageBlockTransfer.addDataType(os);
//			writeImageBlock(imageBlockTransfer);
			imageBlockTransfer(imageBlockTransfer);
			
			if(i % 50 == 0){ // i is multiple of 50
				this.protocolLink.getLogger().log(Level.INFO, "ImageTransfer: " + i + " of " + blockCount + " blocks are sent to the device");
			}
			
			if(DEBUG) {
				System.out.println("ImageTrans: Write block " + i + " success.");
			}
		}
	}
	
	/**
	 * Verify the image. If the result is a temporary failure, then wait a few seconds and retry it.
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public void verifyAndRetryImage() throws IOException, InterruptedException{
		try{
			int retry = 3;
			while(retry > 0){
				try{
					imageVerification();
					retry = 0;
				} catch (DataAccessResultException e) {
					if(e.getDataAccessResult() == 2){ //"Temporary failure"
						retry--;
						Thread.sleep(this.delay);
					} else {
						throw new IOException("Could not verify the image." + e.getMessage());
					}
				}
			}
		} catch (IOException e){
			e.printStackTrace();
			throw new IOException("Could not verify the image." + e.getMessage());
		}
	}
	
//	/**
//	 * Check if there are missing blocks, if so, resent them
//	 * @throws IOException
//	 */
//	private void checkAndSendMissingBlocks() throws IOException{
//		
//		byte[] octetStringData = null;
//		OctetString os = null;
//		long previousMissingBlock = -1;
//		int retryBlock = 0;
//		int totalRetry = 0;
//		while(readFirstMissingBlock().getValue() < this.blockCount){
//			
//			if(DEBUG)System.out.println("ImageTrans: First Missing block is " + getFirstMissingBlock().getValue());
//			
//			if(previousMissingBlock == getFirstMissingBlock().getValue()){
//				if(retryBlock++ == this.maxBlockRetryCount){
//					throw new IOException("Exceeding the maximum retry for block " + getFirstMissingBlock().getValue() + ", Image transfer is canceled.");
//				} else if(totalRetry++ == this.maxTotalRetryCount){
//					throw new IOException("Exceeding the total maximum retry count, Image transfer is canceled.");
//				}
//			} else {
//				previousMissingBlock = getFirstMissingBlock().getValue();
//				retryBlock = 0;
//			}
//			
//			if (getFirstMissingBlock().getValue() < this.blockCount -1) {
//				octetStringData = new byte[(int)getMaxImageBlockSize().getValue()];
//				System.arraycopy(this.data, (int)(getFirstMissingBlock().getValue()*getMaxImageBlockSize().getValue()), octetStringData, 0, 
//						(int)getMaxImageBlockSize().getValue());
//			} else {
//				long blockSize = this.size.getValue() - (getFirstMissingBlock().getValue()*getMaxImageBlockSize().getValue());
//				octetStringData = new byte[(int)blockSize];
//				System.arraycopy(this.data, (int)(getFirstMissingBlock().getValue()*getMaxImageBlockSize().getValue()), octetStringData, 0, 
//						(int)blockSize);
//			}
//			
//			os = new OctetString(trimByteArray(octetStringData));
//			this.imageBlockTransfer = new Structure();
//			this.imageBlockTransfer.addDataType(new Unsigned32((int)getFirstMissingBlock().getValue()));
//			this.imageBlockTransfer.addDataType(os);
//			writeImageBlock(this.imageBlockTransfer);
//			if(DEBUG)System.out.println("ImageTrans: Write block " + (int)getFirstMissingBlock().getValue() + " success.");
//		}
//	}
	
	
	/**
	 * Get the maximum block image size from the device
	 * @return
	 * @throws IOException
	 */
	public Unsigned32 readImageBlockSize() throws IOException{
		try {
			if(this.imageMaxBlockSize == null){
				this.imageMaxBlockSize = new Unsigned32(getLNResponseData(ATTRB_IMAGE_BLOCK_SIZE),0);
			}
			return this.imageMaxBlockSize;
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not get the maximum block size." + e.getMessage());
		}
	}
	
	/**
	 * Provides information about the transfer status of each ImageBlock. 
	 * Each bit in the bit-string provides information about one individual 
	 * ImageBlock: 
	 * 		0 = Not transferred, 
	 * 		1 = Transferred 
	 * @return
	 * @throws IOException
	 */
	public BitString readImageTransferedBlockStatus() throws IOException {
		try{
			this.imageTransferBlocksStatus = new BitString(getLNResponseData(ATTRB_IMAGE_TRANSFER_BLOCK_STATUS), 0);
			return this.imageTransferBlocksStatus;
		} catch (IOException e){
			e.printStackTrace();
			throw new IOException("Could not read the imagetransferblock status." + e.getMessage());
		}
	}

	/**
	 * Provides the ImageBlockNumber of the first ImageBlock not transferred. 
	 * NOTE:  If the Image is complete, the value returned should be above the number 
	 * of blocks calculated from the Image size and the ImageBlockSize. 
	 * @return
	 * @throws IOException
	 */
	public Unsigned32 readFirstNotTransferedBlockNumber() throws IOException {
		try{
			this.imageFirstNotTransferedBlockNumber = new Unsigned32(getLNResponseData(ATTRB_IMAGE_FIRST_NOT_TRANSFERED_BLOCK), 0);
			return this.imageFirstNotTransferedBlockNumber;
		} catch (IOException e){
			e.printStackTrace();
			throw new IOException("Could not retrieve the first not transfered block number." + e.getMessage());
		}
	}
	
	/**
	 * Controls enabling the Image transfer process. The method can be 
	 * invoked successfully only if the value of this attribute is TRUE. 
	 * boolean: FALSE = Disabled, 
	 * 			TRUE = Enabled 
	 * @return
	 * @throws IOException
	 */
	public BooleanObject readImageTransferEnabledState() throws IOException{
		try {
			this.imageTransferEnabled = new BooleanObject(getLNResponseData(ATTRB_IMAGE_TRANSFER_ENABLED),0);
			return this.imageTransferEnabled;
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not retrieve the transfer enabled state." + e.getMessage());
		}
	}
	
	/**
	 * Write the given state ot the imageTransfer enabled attribute
	 * @param state : true to indicate that in imageTransfer will be done, false otherwise
	 * @throws IOException
	 */
	public void writeImageTransferEnabledState(boolean state) throws IOException{
		try {
			write(ATTRB_IMAGE_TRANSFER_ENABLED, new BooleanObject(state).getBEREncodedByteArray());
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not write the transfer enabled state." + e.getMessage());
		}
	}
	
	/**
	 * Controls enabling the Image transfer process. The method can be 
	 * invoked successfully only if the value of this attribute is TRUE. 
	 * boolean: FALSE = Disabled, 
	 * 			TRUE = Enabled 
	 * @return
	 * @throws IOException
	 */
	public BooleanObject getImageTransferEnabledState() throws IOException{
		try {
			if(this.imageTransferEnabled == null){
				return readImageTransferEnabledState();
			} else {
				return this.imageTransferEnabled;
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not retrieve the transfer enabled state." + e.getMessage());
		}
	}
	
	/**
	 * Holds the status of the Image transfer process. 
	 * enum:  (0)  Image transfer not initiated, 
	 * 		(1)  Image transfer initiated, 
	 * 		(2)  Image verification initiated, 
	 * 		(3)  Image verification successful, 
	 * 		(4)  Image verification failed, 
	 * 		(5)  Image activation initiated, 
	 * 		(6)  Image activation successful 
	 * 		(7)  Image activation failed 
	 * @return
	 * @throws IOException
	 */
	public TypeEnum readImageTransferStatus() throws IOException {
		try{
			this.imageTransferStatus = new TypeEnum(getLNResponseData(ATTRB_IMAGE_TRANSFER_STATUS), 0);
			return this.imageTransferStatus;
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not read the transferStatus." + e.getMessage());
		}
	}

	/**
	 * Provides information on the Image(s) ready for activation. It is 
	 * generated as the result of the Image verification. The client may 
	 * check this information before activating the Image(s). 
	 * @return
	 * @throws IOException
	 */
	public Array readImageToActivateInfo() throws IOException {
		try{
			this.imageToActivateInfo = new Array(getLNResponseData(ATTRB_IMAGE_TO_ACTIVATE_INFO), 0, 0);
			return this.imageToActivateInfo;
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not read the imageToActivateInfo." + e.getMessage());
		}
	}
	/**
	 * Initializes the Image transfer process.
	 * The structure has the form of :
	 * 		data ::= structure
	 * 		{ 
 	 * 		image_identifier:  octet-string, 
	 * 		 image_size:   double-long-unsigned 
	 * 		} 
	 * 		where: 
	 * 		-  image_identifier identifies the Image to be transferred; 
	 * 		-  image_size holds the ImageSize, expressed in octets. 
	 * @param imageInfo
	 * @throws IOException
	 */
	public void imageTransferInitiate(Structure imageInfo) throws IOException {
		try {
			invoke(IMAGE_TRANSFER_INITIATE, imageInfo.getBEREncodedByteArray());
		} catch (IOException e){
			throw new IOException("Could not initiate the imageTransfer" + e.getMessage());
		}
	}
	
	/**
	 * Transfers one block of the Image to the server. 
	 * The structure has the form of :
	 * 		data ::= structure 
	 * 		{ 
 	 * 		image_block_number:   double-long-unsigned, 
 	 * 		image_block_value:   octet-string 
	 * 		} 
	 * 		NOTE: the first ImageBlock sent is block 0. 
	 * @param imageData
	 * @throws IOException
	 */
	public void imageBlockTransfer(Structure imageData) throws IOException {
		try{
			invoke(IMAGE_BLOCK_TRANSFER, imageData.getBEREncodedByteArray());
		} catch (IOException e) {
			throw new IOException("Could not write the current imageData block" + e.getMessage());
		}
	}
	
	/**
	 * Verifies the integrity of the Image before activation. 
	 * 
	 * The result of the invocation of this method may be success, 
	 * temporary_failure or other_reason. If it is not success, then the 
	 * result of the verification can be learned by retrieving the value of 
	 * the image_transfer_status attribute. 
	 * @throws IOException
	 */
	public void imageVerification() throws IOException {
		try{
			invoke(IMAGE_VERIFICATION, new Integer8(0).getBEREncodedByteArray());
		} catch (IOException e) {
			throw new IOException("Could not verify the imageData" + e.getMessage());
		}
	}
	
	/**
	 * Activates the Image(s). 
	 * 
	 * If the Image transferred has not been verified before, then this is 
	 * done as part of the Image activation. The result of the invocation 
	 * of this method may be success, temporary-failure or other-reason. 
	 * If it is not success, then the result of the activation can be learned 
	 * by retrieving the value of the image_transfer_status attribute. 
	 * 
	 * @throws IOException
	 */
	public void imageActivation() throws IOException {
		try{
			invoke(IMAGE_ACTIVATION, new Integer8(0).getBEREncodedByteArray());
		} catch (IOException e) {
			throw new IOException("Could not activate the image." + e.getMessage());
		}
	}
}
