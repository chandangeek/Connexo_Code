/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.ProtocolException;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;

import java.io.IOException;
import java.util.logging.Level;

/**
 *
 * @author gna
 * The P3ImageTransfer Object is created from the DSMR P3 Companion Standard and it is slightly different from the
 * ImageTransfer Object in the draft of the BlueBook_V9. So remember, this object is NOT completely DLMS compliant.
 * BeginChanges:
 * GNA 30032009 : Added a trimByteArray to reduce the FF blocks.
 */

public class P3ImageTransfer extends AbstractCosemObject {

	public static boolean DEBUG = false;
	public static final int CLASSID = 18;
	private static int delay = 3000;
	private int maxBlockRetryCount = 3;
	private int maxTotalRetryCount = 500;

	private ProtocolLink protocolLink;

	/** Attributes */
	private Unsigned32 imageMaxBlockSize = null; // holds the max size of the imageblocks to be sent to the server(meter)
	private Structure imageBlockTransfer = null; // contains the block data
	private BitString imageMissingBlocks = null; // Each bit provides information about the individual image blocks
	private Unsigned32 firstMissingBlockOffset = null; // provides offset of the first missing block
	private BooleanObject transferEnabled = null; // is the enabled status of the image transfer
	private Structure imageInfo = null; // provides image info for all images

	/** Attribute numbers */
	private static final int ATTRB_IMAGE_BLOCK_SIZE = 2;
	private static final int ATTRB_IMAGE_BLOCK_TRANSFER = 3;
	private static final int ATTRB_IMAGE_MISSING_BLOCKS = 4;
	private static final int ATTRB_IMAGE_FIRST_MISSING_BLOCK_OFFSET = 5;
	private static final int ATTRB_TRANSFER_ENABLED = 6;
	private static final int ATTRB_IMAGES_INFO = 7;

	/** Method invoke */
	private static final int INITIATE_IMAGE_TRANSFER = 1;
	private static final int IMAGE_VERIFICTION = 2;
	private static final int IMAGE_VERIFICATION_ACTIVATION = 3;

	/** Image info */
	private Unsigned32 size = null; 	// the size of the image
	private byte[] data = null; // the complete image in byte
	private int blockCount = -1; // the amount of block numbers
	private OctetString imageIdentification = null;
	private OctetString imageSignature = null;


	public P3ImageTransfer(ProtocolLink protocolLink, ObjectReference objectReference) {
		super(protocolLink, objectReference);
		this.protocolLink = protocolLink;
	}

	protected int getClassId() {
		return CLASSID;
	}

	/**
	 * Start the automatic upgrade procedure
	 * @param data - the image to transfer
	 * @throws java.io.IOException
	 * @throws InterruptedException
	 */
	public void upgrade(byte[] data) throws IOException, InterruptedException {
		this.data = data;
		this.size = new Unsigned32(data.length);

		if(getTransferEnabledState().getState()){

			if(DEBUG) {
				System.out.println("ImageTrans: Enabled state is true.");
			}

			// Step1: Get the maximum image block size
			// and calculate the amount of blocks in one step
			this.blockCount = (int)(this.size.getValue()/getMaxImageBlockSize().getValue()) + (((this.size.getValue()%getMaxImageBlockSize().getValue())==0)?0:1);
			if(DEBUG) {
				System.out.println("ImageTrans: Maximum block size is: " + getMaxImageBlockSize() +
						", Number of blocks: " + blockCount + ".");
			}

			this.protocolLink.getLogger().log(Level.INFO, "Start : " + System.currentTimeMillis());

			// Step2: Initiate the image transfer
			initiateImageTransfer();
			if(DEBUG) {
				System.out.println("ImageTrans: Initialize success.");
			}


			// Step3: Transfer image blocks
			transferImageBlocks();
			if(DEBUG) {
				System.out.println("ImageTrans: Transfered " + this.blockCount + " blocks.");
			}

			// Step4: Check completeness of the image and transfer missing blocks
			checkAndSendMissingBlocks();

			// Step5: Verify image
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
			throw new ProtocolException("Could not perform the upgrade because meter does not allow it.");
		}

	}

	/**
	 * Transfer all the image blocks to the meter
	 * @throws java.io.IOException
	 */
	private void transferImageBlocks() throws IOException {
		byte[] octetStringData = null;
		OctetString os = null;
		for(int i = 0; i < blockCount; i++){
			if(i < blockCount -1){
				octetStringData = new byte[(int)getMaxImageBlockSize().getValue()];
				System.arraycopy(this.data, (int) (i * getMaxImageBlockSize().getValue()), octetStringData, 0,
                        (int) getMaxImageBlockSize().getValue());
			} else {
				long blockSize = this.size.getValue() - (i*getMaxImageBlockSize().getValue());
				octetStringData = new byte[(int)blockSize];
				System.arraycopy(this.data, (int) (i * getMaxImageBlockSize().getValue()), octetStringData, 0,
                        (int) blockSize);
			}
			os = OctetString.fromByteArray(trimByteArray(octetStringData));
			this.imageBlockTransfer = new Structure();
			this.imageBlockTransfer.addDataType(new Unsigned32(i));
			this.imageBlockTransfer.addDataType(os);
			writeImageBlock(this.imageBlockTransfer);

			if(i % 50 == 0){ // i is multiple of 50
				this.protocolLink.getLogger().log(Level.INFO, "ImageTransfer: " + i + " of " + blockCount + " blocks are send to the device");
			}

			if(DEBUG) {
				System.out.println("ImageTrans: Write block " + i + " success.");
			}
		}
	}

	/**
	 * Trim the byteArray so all ending 0xFF bytes are trimed.
	 * @param octetStringData
	 * @return the trimmed byteArray
	 */
	private byte[] trimByteArray(byte[] octetStringData){
		int last = octetStringData.length-1;
		while((last >= 0) && (octetStringData[last] == -1)){
			last--;
		}
		byte[] b = new byte[last+1];
		System.arraycopy(octetStringData, 0, b, 0, last + 1);
		return b;
	}

	/**
	 * Check if there are missing blocks, if so, resent them
	 * @throws java.io.IOException
	 */
	private void checkAndSendMissingBlocks() throws IOException {

		byte[] octetStringData = null;
		OctetString os = null;
		long previousMissingBlock = -1;
		int retryBlock = 0;
		int totalRetry = 0;
		while(readFirstMissingBlock().getValue() < this.blockCount){

			if(DEBUG) {
				System.out.println("ImageTrans: First Missing block is " + getFirstMissingBlock().getValue());
			}

			if(previousMissingBlock == getFirstMissingBlock().getValue()){
				if(retryBlock++ == this.maxBlockRetryCount){
					throw new ProtocolException("Exceeding the maximum retry for block " + getFirstMissingBlock().getValue() + ", Image transfer is canceled.");
				} else if(totalRetry++ == this.maxTotalRetryCount){
					throw new ProtocolException("Exceeding the total maximum retry count, Image transfer is canceled.");
				}
			} else {
				previousMissingBlock = getFirstMissingBlock().getValue();
				retryBlock = 0;
			}

			if (getFirstMissingBlock().getValue() < this.blockCount -1) {
				octetStringData = new byte[(int)getMaxImageBlockSize().getValue()];
				System.arraycopy(this.data, (int) (getFirstMissingBlock().getValue() * getMaxImageBlockSize().getValue()), octetStringData, 0,
                        (int) getMaxImageBlockSize().getValue());
			} else {
				long blockSize = this.size.getValue() - (getFirstMissingBlock().getValue()*getMaxImageBlockSize().getValue());
				octetStringData = new byte[(int)blockSize];
				System.arraycopy(this.data, (int) (getFirstMissingBlock().getValue() * getMaxImageBlockSize().getValue()), octetStringData, 0,
                        (int) blockSize);
			}

			os = OctetString.fromByteArray(trimByteArray(octetStringData));
			this.imageBlockTransfer = new Structure();
			this.imageBlockTransfer.addDataType(new Unsigned32((int)getFirstMissingBlock().getValue()));
			this.imageBlockTransfer.addDataType(os);
			writeImageBlock(this.imageBlockTransfer);
			if(DEBUG) {
				System.out.println("ImageTrans: Write block " + (int)getFirstMissingBlock().getValue() + " success.");
			}
		}
	}

	/**
	 * Get the enable state to make sure the Image transfer can be executed
	 * @return
	 * @throws java.io.IOException
	 */
	public BooleanObject getTransferEnabledState() throws IOException {
		try {
			if(this.transferEnabled == null){
				this.transferEnabled = new BooleanObject(getLNResponseData(ATTRB_TRANSFER_ENABLED),0);
			}
			return this.transferEnabled;
		} catch (IOException e) {
			e.printStackTrace();
			throw new NestedIOException(e, "Could not retrieve the transfer enabled state." + e.getMessage());
		}
	}
	/**
	 * Don't know if it is possible to set the enabled state.
	 * Can be used to disable meters to upgrade when using broadcast image transfer ...
	 * @param state
	 * @throws java.io.IOException
	 */
	public void writeTransferEnabledState(boolean state) throws IOException {
		try {
			write(ATTRB_TRANSFER_ENABLED, new BooleanObject(state).getBEREncodedByteArray());
		} catch (IOException e) {
			e.printStackTrace();
			throw new NestedIOException(e, "Could not set the transfer enabled state." + e.getMessage());
		}
	}

	/**
	 * Get the maximum block image size from the meter
	 * @return
	 * @throws java.io.IOException
	 */
	public Unsigned32 getMaxImageBlockSize() throws IOException {
		try {
			if(this.imageMaxBlockSize == null){
				this.imageMaxBlockSize = new Unsigned32(getLNResponseData(ATTRB_IMAGE_BLOCK_SIZE),0);
			}
			return this.imageMaxBlockSize;
		} catch (IOException e) {
			e.printStackTrace();
			throw new NestedIOException(e, "Could not get the maximum block size." + e.getMessage());
		}
	}

	/**
	 * Inform the Cosem client to initiate an image transfer
	 * @throws java.io.IOException
	 */
	public void initiateImageTransfer() throws IOException {
		try {
			invoke(INITIATE_IMAGE_TRANSFER, getImageSize().getBEREncodedByteArray());
		} catch (IOException e) {
			e.printStackTrace();
			throw new NestedIOException(e, "Could not initiate the image transfer." + e.getMessage());
		}
	}

	/**
	 * Verify the image
	 * @throws java.io.IOException
	 */
	public void verifyImage() throws IOException {
		try{
			invoke(IMAGE_VERIFICTION, new Unsigned16(0).getBEREncodedByteArray());
		} catch (IOException e){
			e.printStackTrace();
			throw new NestedIOException(e, "Could not verify the image." + e.getMessage());
		}
	}

	/**
	 * Verify the image. If the result is a temporary failure, then wait a few seconds and retry it.
	 * @throws java.io.IOException
	 * @throws InterruptedException
	 */
	public void verifyAndRetryImage() throws IOException {
		try{
			int retry = 3;
			while(retry > 0){
				try{
					invoke(IMAGE_VERIFICTION, new Unsigned16(0).getBEREncodedByteArray());
					retry = 0;
				} catch (DataAccessResultException e) {
					if(e.getDataAccessResult() == 2){ //"Temporary failure"
						retry--;
						DLMSUtils.delay(delay);
					} else {
						throw new NestedIOException(e, "Could not verify the image." + e.getMessage());
					}
				}
			}
		} catch (IOException e){
			e.printStackTrace();
			throw new NestedIOException(e, "Could not verify the image." + e.getMessage());
		}
	}

	/**
	 * Activate the image
	 * @throws java.io.IOException
	 */
	public void activateImage() throws IOException {
		try{
			invoke(IMAGE_VERIFICATION_ACTIVATION, new Unsigned16(0).getBEREncodedByteArray());
		} catch (IOException e){
			e.printStackTrace();
			throw new NestedIOException(e, "Could not activate image." + e.getMessage());
		}
	}

	/**
	 * Actiate the image. If the result is a temporary failure, then wait a few seconds and retry it.
	 * @throws java.io.IOException
	 * @throws InterruptedException
	 */
	public void activateAndRetryImage() throws IOException {
		try{
			int retry = 3;
			while(retry > 0){
				try{
					invoke(IMAGE_VERIFICATION_ACTIVATION, new Unsigned16(0).getBEREncodedByteArray());
					retry = 0;
				} catch (DataAccessResultException e) {
					if(e.getDataAccessResult() == 2){ //"Temporary failure"
						retry--;
                        DLMSUtils.delay(delay);
					} else {
						throw new ProtocolException("Could not verify the image." + e.getMessage());
					}
				}
			}
		} catch (IOException e){
			e.printStackTrace();
			throw new NestedIOException(e, "Could not verify the image." + e.getMessage());
		}
	}

	/**
	 * Write one image block 'imageData' with offset 'blockOffset' to the meter
	 * @param blockOffset
	 * @param imageData
	 * @throws java.io.IOException
	 */
	public void writeImageBlock(Unsigned32 blockOffset, OctetString imageData) throws IOException {
		try {
			Structure imageBlock = new Structure();
			imageBlock.addDataType(blockOffset);
			imageBlock.addDataType(imageData);
			write(ATTRB_IMAGE_BLOCK_TRANSFER, imageBlock.getBEREncodedByteArray());
		} catch (IOException e) {
			e.printStackTrace();
			throw new NestedIOException(e, "Could not write the current image block with offset: " + blockOffset.getValue() + "." + e.getMessage());
		}
	}

	/**
	 * Write one image block 'imageData' with offset 'blockOffset' to the meter
     *
     * @param imageStruct
     *              The given imageStructure to write
     *
	 * @throws java.io.IOException
	 */
	public void writeImageBlock(Structure imageStruct) throws IOException {
		try {
			write(ATTRB_IMAGE_BLOCK_TRANSFER, imageStruct.getBEREncodedByteArray());
		} catch (DataAccessResultException e){
			e.printStackTrace();
			// catch and go to the next
		} catch (IOException e) {
			e.printStackTrace();
			if(e.getMessage().indexOf("Connection reset by peer: socket write error") > -1){
				throw new NestedIOException(e, e.getMessage());
			}
			// Catch and go to the next!
			if(DEBUG) {
				System.out.println("ImageTrans: Write block " + imageStruct.getDataType(0).getUnsigned32().getValue() + " has failed.");
			}
		}
	}

	/**
	 * Get the complete image size
	 * @return
	 */
	public Unsigned32 getImageSize(){
		return this.size;
	}

	/**
	 * Query for the missing blocks bitString from meter
	 * @return
	 * @throws java.io.IOException
	 */
	public BitString readImageMissingBlocks() throws IOException {
		try {
			this.imageMissingBlocks = new BitString(getLNResponseData(ATTRB_IMAGE_MISSING_BLOCKS),0);
			return imageMissingBlocks;
		} catch (IOException e) {
			e.printStackTrace();
			throw new NestedIOException(e, "Could not retrieve the missing blocks." + e.getMessage());
		}
	}
	/**
	 * @return the current bitstring for the missingblocks (NOT read from meter)
	 */
	public BitString getImageMissingBlocks(){
		return this.imageMissingBlocks;
	}

	/**
	 * Query for the first missing image block in the meter
	 * @return
	 * @throws java.io.IOException
	 */
	public Unsigned32 readFirstMissingBlock() throws IOException {
		try {
			this.firstMissingBlockOffset = new Unsigned32(getLNResponseData(ATTRB_IMAGE_FIRST_MISSING_BLOCK_OFFSET),0);
			return this.firstMissingBlockOffset;
		} catch (IOException e) {
			e.printStackTrace();
			throw new NestedIOException(e, "Could not retrieve the first missing block." + e.getMessage());
		}
	}
	/**
	 * @return return the current missing block (NOT read from meter)
	 */
	public Unsigned32 getFirstMissingBlock(){
		return this.firstMissingBlockOffset;
	}

}
