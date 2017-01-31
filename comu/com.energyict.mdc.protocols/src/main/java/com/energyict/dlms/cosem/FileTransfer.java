/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.ProtocolException;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned32;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;

/**
 *
 * @author gna
 * <pre>
 * The file transfer takes place in several steps:
 * Step 1:   The client gets the ImageBlockSize from each server individually;
 * Step 2:   The client initiates the Image transfer process individually or using broadcast;
 * Step 3:   The client transfers ImageBlocks to (a group of) server(s) individually or using  broadcast;
 * Step 4:   The client checks the completeness of the Image in each server individually and transfers any ImageBlocks not (yet) transferred;
 * Step 5:   The Image is verified;
 * Step 6:   Before activation, the Image is checked;
 * Step 7:   The Image(s) is(/are) activated.
 * </pre>
 */

public class FileTransfer extends AbstractCosemObject {

	public static boolean DEBUG = false;
	private static int delay = 3000;
	private int maxBlockRetryCount = 3;
	private int maxTotalRetryCount = 500;

	private ProtocolLink protocolLink;

	/* Attributes */
	private Unsigned32 imageMaxBlockSize = null; // holds the max size of the imageblocks to be sent to the server(meter)
	private BitString imageTransferBlocksStatus = null; // Provides information about the transfer status of each imageBlock (1=Transfered, 0=NotTransfered)
	private Unsigned32 imageFirstNotTransferedBlockNumber = null; // Provides the blocknumber of the first not transfered imageblock
	private BooleanObject imageTransferEnabled = null; // Controls enabling the image_transfer_proces
	private TypeEnum imageTransferStatus = null; // Holds the status of the image transfer process
	private Array imageToActivateInfo = null;	// Provides information on the image(s) ready for activation

	/* Attribute numbers */
	private static final int ATTRB_IMAGE_BLOCK_SIZE = 2;
	private static final int ATTRB_IMAGE_TRANSFER_BLOCK_STATUS = 3;
	private static final int ATTRB_IMAGE_FIRST_NOT_TRANSFERED_BLOCK = 4;
	private static final int ATTRB_IMAGE_TRANSFER_ENABLED = 5;
	private static final int ATTRB_IMAGE_TRANSFER_STATUS = 6;
	private static final int ATTRB_IMAGE_TO_ACTIVATE_INFO = 7;

	/* Method invoke */
	private static final int IMAGE_TRANSFER_INITIATE = 1;
	private static final int IMAGE_BLOCK_TRANSFER = 2;
	private static final int IMAGE_VERIFICATION = 3;
	private static final int IMAGE_ACTIVATION = 4;
	/* Method writes SN */
	private static final int IMAGE_TRANSFER_INITIATE_SN = 0x40;
	private static final int IMAGE_BLOCK_TRANSFER_SN = 0x48;
	private static final int IMAGE_VERIFICATION_SN = 0x50;
	private static final int IMAGE_ACTIVATION_SN = 0x58;

	/* Image info */
	private Unsigned32 size = null; 	// the size of the image
	private byte[] data = null; // the complete image in byte
	private int blockCount = -1; // the amount of block numbers
	private OctetString imageIdentification = null;
	private OctetString imageSignature = null;
    private String filename;
    private String filetype = null;
    private Unsigned32 file_flags = null;

	static final byte[] LN=new byte[]{0,0,44,0,(byte)128,(byte)255};
    private static final NullData NULLDATA = new NullData();

    public FileTransfer(ProtocolLink protocolLink) {
            super(protocolLink,new ObjectReference(LN));
            this.protocolLink = protocolLink;
        }

	public FileTransfer(ProtocolLink protocolLink, ObjectReference objectReference) {
		super(protocolLink, objectReference);
		this.protocolLink = protocolLink;
	}

    public static ObisCode getDefaultObisCode() {
        return ObisCode.fromByteArray(LN);
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setFile_flags(Unsigned32 file_flags) {
        this.file_flags = file_flags;
    }

    public void setFiletype(String filetype) {
        this.filetype = filetype;
    }

    /**
	 * @return the obisCode from the ImageTransferObject
	 */
	public ObisCode getObisCode() {
		return ObisCode.fromByteArray(getObjectReference().getLn()) ;
	}

	/**
	 * @return the classId of the ImageTransfer object, should always be 18
	 */
	protected int getClassId() {
		return DLMSClassId.IMAGE_TRANSFER.getClassId();
	}

	/**
	 * Start the automatic upgrade procedure. If the last block is not a multiple of the blockSize, then additional zeros will be padded at the end.
	 * If you don't want this behavior then use {{@link #upgrade(byte[], boolean)} instead.
	 *
	 * @param data
	 * 		- the image to transfer
	 *
	 * @throws java.io.IOException if something went wrong during the upgrade.
	 * @throws InterruptedException when interrupted while sleeping
	 */
	public void upgrade(byte[] data) throws IOException, InterruptedException {
	    this.upgrade(data, true);
	}

	/**
	 * Start the automatic upgrade procedure. You may choose to add additional zeros at in the last block to match the blockSize for each block.
	 *
	 * @param data
	 * 		- the image to transfer
	 * @param additionalZeros
	 * 		- indicate whether you need to add zeros to the last block to match the blockSize
	 *
	 * @throws java.io.IOException when something went wrong during the upgrade
	 * @throws InterruptedException when interrupted while sleeping
	 */
	public void upgrade(byte[] data, boolean additionalZeros) throws IOException, InterruptedException {
		this.data = data;
		this.size = new Unsigned32(data.length);

		// Set the imageTransferEnabledState to true (otherwise the upgrade can not be performed)
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

			// Step2: Initiate the file transfer
			Structure imageInitiateStructure = new Structure();
			imageInitiateStructure.addDataType(OctetString.fromString(filename));
			imageInitiateStructure.addDataType(size);
            if (filetype == null) {
                imageInitiateStructure.addDataType(NULLDATA);
            } else {
                imageInitiateStructure.addDataType(OctetString.fromString(filetype));
            }
            if (file_flags == null) {
                imageInitiateStructure.addDataType(NULLDATA);
            } else {
                imageInitiateStructure.addDataType(file_flags);
            }
			imageTransferInitiate(imageInitiateStructure);
			if(DEBUG) {
				System.out.println("ImageTrans: Initialize success.");
			}


			// Step3: Transfer image blocks
			transferImageBlocks(additionalZeros);
			this.protocolLink.getLogger().log(Level.INFO, "All blocks are sent at : " + new Date(System.currentTimeMillis()));

			// Step4: Check completeness of the image and transfer missing blocks
			//TODO - Checking for missings is not necessary at the moment because we have a guaranteed connection,
			// Every block is confirmed by the meter
//			checkAndSendMissingBlocks();

			// Step5: Verify image
			verifyAndRetryImage();
			this.protocolLink.getLogger().log(Level.INFO, "Verification of the image was succesfull at : " + new Date(System.currentTimeMillis()));

			// Step6: Check image before activation
			// Skip this step

			// Step7: Activate image
			// This step is done in the ProtocolCode!


		} else {
			throw new ProtocolException("Could not perform the upgrade because meter does not allow it.");
		}

	}

	/**
	 * Transfer all the image blocks to the meter.
	 *
	 * @param additionalZeros
	 * 		- add additional zeros to match the last blocksize to a multiple of the fileSize
	 *
	 * @throws java.io.IOException if something went wrong during the upgrade
	 */
	public void transferImageBlocks(boolean additionalZeros) throws IOException {

//	    File file = new File("C:\\testDebugFile.txt");
//	    FileOutputStream fos = new FileOutputStream(file);

		byte[] octetStringData = null;
		OctetString os = null;
		Structure imageBlockTransfer;
		for(int i = 0; i < blockCount; i++){
			if(i < blockCount -1){
				octetStringData = new byte[(int)readImageBlockSize().getValue()];
				System.arraycopy(this.data, (int) (i * readImageBlockSize().getValue()), octetStringData, 0,
                        (int) readImageBlockSize().getValue());
			} else {
			    /*
			     * If it is the last block then it is dependent from vendor to vendor whether they want the size of the last block
			     * to be the same as the others, or just the size of the remaining bytes.
			     */
			    long blockSize = this.size.getValue() - (i*readImageBlockSize().getValue());
			    if(additionalZeros){
				octetStringData = new byte[(int)readImageBlockSize().getValue()];
				System.arraycopy(this.data, (int) (i * readImageBlockSize().getValue()), octetStringData, 0,
                        (int) blockSize);
			    } else {
				octetStringData = new byte[(int)blockSize];
				System.arraycopy(this.data, (int) (i * readImageBlockSize().getValue()), octetStringData, 0,
                        (int) blockSize);
			    }

			}
			os = OctetString.fromByteArray(octetStringData);
			imageBlockTransfer = new Structure();
			imageBlockTransfer.addDataType(new Unsigned32(i));
			imageBlockTransfer.addDataType(os);

//			//***** Temporary implementation of retrying 'Temporary failures' *****//
//			int tempRetry = 0;
//			while(tempRetry < 5){
//				try {
//					imageBlockTransfer(imageBlockTransfer);
//					tempRetry = 5;
//				} catch (IOException e) {
//					if(e.getMessage().indexOf("Cosem Data-Access-Result exception Temporary failure ") > -1){
//						tempRetry++;
//						if(tempRetry == 5){
//							throw new IOException("Max. retries (5) exceeded. " + e.getMessage());
//						}
//						this.protocolLink.getLogger().log(Level.INFO, "Transfering image block resulted in temporary failure, retry " + tempRetry);
//						try {
//							Thread.sleep(2000);
//						} catch (InterruptedException e1) {
//							this.protocolLink.getLogger().log(Level.INFO, e1.getLocalizedMessage());
//						}
//					} else {
//						throw e;
//					}
//				}
//			}
//			//******************************************************************//

			// without retries
			imageBlockTransfer(imageBlockTransfer);

			if(i % 50 == 0){ // i is multiple of 50
				this.protocolLink.getLogger().log(Level.INFO, "ImageTransfer: " + i + " of " + blockCount + " blocks are sent to the device");
			}
		}
//		fos.close();
	}

	/**
	 * Check if there are missing blocks, if so, resent them
	 * @throws java.io.IOException
	 */
	public void checkAndSendMissingBlocks() throws IOException {
		Structure imageBlockTransfer;
		byte[] octetStringData = null;
		OctetString os = null;
		long previousMissingBlock = -1;
		int retryBlock = 0;
		int totalRetry = 0;
		while(readFirstNotTransferedBlockNumber().getValue() < this.blockCount){

			if(previousMissingBlock == this.getImageFirstNotTransferedBlockNumber().getValue()){
				if(retryBlock++ == this.maxBlockRetryCount){
					throw new ProtocolException("Exceeding the maximum retry for block " + this.getImageFirstNotTransferedBlockNumber().getValue() + ", Image transfer is canceled.");
				} else if(totalRetry++ == this.maxTotalRetryCount){
					throw new ProtocolException("Exceeding the total maximum retry count, Image transfer is canceled.");
				}
			} else {
				previousMissingBlock = this.getImageFirstNotTransferedBlockNumber().getValue();
				retryBlock = 0;
			}

			if (this.getImageFirstNotTransferedBlockNumber().getValue() < this.blockCount -1) {
				octetStringData = new byte[(int)readImageBlockSize().getValue()];
				System.arraycopy(this.data, (int) (this.getImageFirstNotTransferedBlockNumber().getValue() * readImageBlockSize().getValue()), octetStringData, 0,
                        (int) readImageBlockSize().getValue());
			} else {
				long blockSize = this.size.getValue() - (this.getImageFirstNotTransferedBlockNumber().getValue()*readImageBlockSize().getValue());
				octetStringData = new byte[(int)blockSize];
				System.arraycopy(this.data, (int) (this.getImageFirstNotTransferedBlockNumber().getValue() * readImageBlockSize().getValue()), octetStringData, 0,
                        (int) blockSize);
			}

			os = OctetString.fromByteArray(octetStringData);
			imageBlockTransfer = new Structure();
			imageBlockTransfer.addDataType(new Unsigned32((int)this.getImageFirstNotTransferedBlockNumber().getValue()));
			imageBlockTransfer.addDataType(os);
			imageBlockTransfer(imageBlockTransfer);

		}
	}

	/**
	 * Verify the image. If the result is a temporary failure, then wait a few seconds and retry it.
	 * @throws java.io.IOException
	 * @throws InterruptedException
	 */
	public void verifyAndRetryImage() throws IOException, InterruptedException {
		try{
			int retry = 3;
			while(retry >= 0){
				try{
					imageVerification();
					retry = -1;
				} catch (DataAccessResultException e) {
					if((e.getDataAccessResult() == 2) && retry >= 1){ //"Temporary failure"
						this.protocolLink.getLogger().log(Level.INFO, "Received a temporary failure during verification, will retry.");
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
	 * Get the maximum block image size from the device
	 * @return
	 * @throws java.io.IOException
	 */
	public Unsigned32 readImageBlockSize() throws IOException {
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
	 * Provides information about the transfer status of each ImageBlock.
	 * Each bit in the bit-string provides information about one individual
	 * ImageBlock:
	 * 		0 = Not transferred,
	 * 		1 = Transferred
	 * @return
	 * @throws java.io.IOException
	 */
	public BitString readImageTransferedBlockStatus() throws IOException {
		try{
			this.imageTransferBlocksStatus = new BitString(getLNResponseData(ATTRB_IMAGE_TRANSFER_BLOCK_STATUS), 0);
			return this.imageTransferBlocksStatus;
		} catch (IOException e){
			e.printStackTrace();
			throw new NestedIOException(e, "Could not read the imagetransferblock status." + e.getMessage());
		}
	}

	/**
	 * Provides the ImageBlockNumber of the first ImageBlock not transferred.
	 * NOTE:  If the Image is complete, the value returned should be above the number
	 * of blocks calculated from the Image size and the ImageBlockSize.
	 * @return
	 * @throws java.io.IOException
	 */
	public Unsigned32 readFirstNotTransferedBlockNumber() throws IOException {
		try{
			this.setImageFirstNotTransferedBlockNumber(new Unsigned32(getLNResponseData(ATTRB_IMAGE_FIRST_NOT_TRANSFERED_BLOCK), 0));
			return this.getImageFirstNotTransferedBlockNumber();
		} catch (IOException e){
			e.printStackTrace();
			throw new NestedIOException(e, "Could not retrieve the first not transfered block number." + e.getMessage());
		}
	}

	/**
	 * Controls enabling the Image transfer process. The method can be
	 * invoked successfully only if the value of this attribute is TRUE.
	 * Boolean: FALSE = Disabled,
	 * 			TRUE = Enabled
	 * @return
	 * @throws java.io.IOException
	 */
	public BooleanObject readImageTransferEnabledState() throws IOException {
		try {
			this.imageTransferEnabled = new BooleanObject(getLNResponseData(ATTRB_IMAGE_TRANSFER_ENABLED),0);
			return this.imageTransferEnabled;
		} catch (IOException e) {
			e.printStackTrace();
			throw new NestedIOException(e, "Could not retrieve the transfer enabled state." + e.getMessage());
		}
	}

	/**
	 * Write the given state to the imageTransfer enabled attribute
	 * @param state : true to indicate that in imageTransfer will be done, false otherwise
	 * @throws java.io.IOException
	 */
	public void writeImageTransferEnabledState(boolean state) throws IOException {
		try {
			write(ATTRB_IMAGE_TRANSFER_ENABLED, new BooleanObject(state).getBEREncodedByteArray());
		} catch (IOException e) {
			e.printStackTrace();
			throw new NestedIOException(e, "Could not write the transfer enabled state." + e.getMessage());
		}
	}

	/**
	 * Controls enabling the Image transfer process. The method can be
	 * invoked successfully only if the value of this attribute is TRUE.
	 * Boolean: FALSE = Disabled,
	 * 			TRUE = Enabled
	 * @return
	 * @throws java.io.IOException
	 */
	public BooleanObject getImageTransferEnabledState() throws IOException {
		try {
			if(this.imageTransferEnabled == null){
				return readImageTransferEnabledState();
			} else {
				return this.imageTransferEnabled;
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new NestedIOException(e, "Could not retrieve the transfer enabled state." + e.getMessage());
		}
	}

	/**
	 * <pre>
	 * Holds the status of the Image transfer process.
	 * enum:  (0)  Image transfer not initiated,
	 * 		(1)  Image transfer initiated,
	 * 		(2)  Image verification initiated,
	 * 		(3)  Image verification successful,
	 * 		(4)  Image verification failed,
	 * 		(5)  Image activation initiated,
	 * 		(6)  Image activation successful
	 * 		(7)  Image activation failed
	 * </pre>
	 * @return
	 * @throws java.io.IOException
	 */
	public TypeEnum readImageTransferStatus() throws IOException {
		try{
			this.imageTransferStatus = new TypeEnum(getLNResponseData(ATTRB_IMAGE_TRANSFER_STATUS), 0);
			return this.imageTransferStatus;
		} catch (IOException e) {
			e.printStackTrace();
			throw new NestedIOException(e, "Could not read the transferStatus." + e.getMessage());
		}
	}

	/**
	 * Provides information on the Image(s) ready for activation. It is
	 * generated as the result of the Image verification. The client may
	 * check this information before activating the Image(s).
	 * @return
	 * @throws java.io.IOException
	 */
	public Array readImageToActivateInfo() throws IOException {
		try{
			this.imageToActivateInfo = new Array(getLNResponseData(ATTRB_IMAGE_TO_ACTIVATE_INFO), 0, 0);
			return this.imageToActivateInfo;
		} catch (IOException e) {
			e.printStackTrace();
			throw new NestedIOException(e, "Could not read the imageToActivateInfo." + e.getMessage());
		}
	}
	/**
	 * <pre>
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
	 * </pre>
	 * @param imageInfo
	 * @throws java.io.IOException
	 */
	public void imageTransferInitiate(Structure imageInfo) throws IOException {
		try {
			if (getObjectReference().isLNReference()) {
				invoke(IMAGE_TRANSFER_INITIATE, imageInfo.getBEREncodedByteArray());
			} else { // SN referencing
				write(IMAGE_TRANSFER_INITIATE_SN, imageInfo.getBEREncodedByteArray());
			}
		} catch (IOException e) {
			throw new NestedIOException(e, "Could not initiate the imageTransfer" + e.getMessage());
		}
	}

	/**
	 * <pre>
	 * Transfers one block of the Image to the server.
	 * The structure has the form of :
	 * 		data ::= structure
	 * 		{
 	 * 		image_block_number:   double-long-unsigned,
 	 * 		image_block_value:   octet-string
	 * 		}
	 * 		NOTE: the first ImageBlock sent is block 0.
	 * </pre>
	 * @param imageData
	 * @throws java.io.IOException
	 */
	public void imageBlockTransfer(Structure imageData) throws IOException {
		try{
			if (getObjectReference().isLNReference()) {
				invoke(IMAGE_BLOCK_TRANSFER, imageData.getBEREncodedByteArray());
			} else {
				write(IMAGE_BLOCK_TRANSFER_SN, imageData.getBEREncodedByteArray());
			}
		} catch (IOException e) {
		    throw new NestedIOException(e, "Could not write the current imageData block" + e.getMessage());
		}
	}

	/**
	 * Verifies the integrity of the Image before activation.
	 *
	 * The result of the invocation of this method may be success,
	 * temporary_failure or other_reason. If it is not success, then the
	 * result of the verification can be learned by retrieving the value of
	 * the image_transfer_status attribute.
	 * @throws java.io.IOException
	 */
	public void imageVerification() throws IOException {
	    if(getObjectReference().isLNReference()){
		try{
		    invoke(IMAGE_VERIFICATION, new Integer8(0).getBEREncodedByteArray());
		} catch (IOException e) {
		    throw new NestedIOException(e, "Could not verify the imageData" + e.getMessage());
		}
	    } else {
		write(IMAGE_VERIFICATION_SN, new Integer8(0).getBEREncodedByteArray());
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
	 * @throws java.io.IOException
	 */
	public void imageActivation() throws IOException {
		try{
			if (getObjectReference().isLNReference()) {
				invoke(IMAGE_ACTIVATION, new Integer8(0).getBEREncodedByteArray());
			} else {
				write(IMAGE_ACTIVATION_SN, new Integer8(0).getBEREncodedByteArray());
			}
		} catch (IOException e) {
		    throw new NestedIOException(e, "Could not activate the image." + e.getMessage());
		}
	}

	/**
	 * @param imageFirstNotTransferedBlockNumber the imageFirstNotTransferedBlockNumber to set
	 */
	public void setImageFirstNotTransferedBlockNumber(Unsigned32 imageFirstNotTransferedBlockNumber) {
		this.imageFirstNotTransferedBlockNumber = imageFirstNotTransferedBlockNumber;
	}

	/**
	 * @return the imageFirstNotTransferedBlockNumber
	 */
	public Unsigned32 getImageFirstNotTransferedBlockNumber() {
		return imageFirstNotTransferedBlockNumber;
	}

}
