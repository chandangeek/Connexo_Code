/**
 * 
 */
package com.energyict.dlms.cosem;

import java.io.IOException;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;

/**
 * @author gna
 * 
 * The PPPSetup object is not completely implemented, please feel free to complete the object.
 *
 */
public class PPPSetup extends AbstractCosemObject {

	private boolean DEBUG = true;	//TODO set it back to false
	private int CLASSID = 44;
	
	/** Attributes */
	private OctetString phyReference = null;	// Contains information about the specific physical layer interface, supporting the PPP layer
	private LCPOptionsType lcpOptions = null;	// Contains the parameters for the Link Control Protocol options
	private IPCPOptionsType ipcpOptions = null; // Contains the parameters for the IP Control Protocol - the network control protocol module 
												//of the PPP for negotiating IP parameters on the PPP link options
	private PPPAuthenticationType pppAuthentication = null; 	// Contains the parameters required by the PPP authentication procedure
	
	/** Attribute numbers */
	static private final int ATTRB_PHY_REFERENCE = 2;
	static private final int ATTRB_LCP_OPTIONS = 3;
	static private final int ATTRB_IPCP_OPTIONS = 4;
	static private final int ATTRB_PPP_AUTHENTICATION = 5;
	
	/** Method invoke */
	// none
	
	public PPPSetup(ProtocolLink protocolLink, ObjectReference objectReference) {
		super(protocolLink, objectReference);
	}

	protected int getClassId() {
		return this.CLASSID;
	}
	
	/**
	 * Read the current lcpOptionsType from the device
	 * @return
	 * @throws IOException
	 */
	public LCPOptionsType readLCPOptionsType() throws IOException{
		try{
			this.lcpOptions = new LCPOptionsType(getLNResponseData(ATTRB_LCP_OPTIONS));
			return this.lcpOptions;
		} catch (IOException e){
			e.printStackTrace();
			throw new IOException("Could not read the lcpOptionsType." + e.getMessage());
		}
	}
	
	/**
	 * Return the latest retrieved lcpOptionsType
	 * @return
	 * @throws IOException 
	 */
	public LCPOptionsType getLCPOptionsType() throws IOException{
		if(this.lcpOptions == null){
			readLCPOptionsType();	// do a dummy read;
		}
		return this.lcpOptions;
	}
	
	/**
	 * Read the current pppAuthenticationType from the device
	 * @return
	 * @throws IOException
	 */
	public PPPAuthenticationType readPPPAuthenticationType() throws IOException{
		try{
			this.pppAuthentication = new PPPAuthenticationType(getLNResponseData(ATTRB_PPP_AUTHENTICATION));
			return this.pppAuthentication;
		} catch (IOException e){
			e.printStackTrace();
			throw new IOException("Could not read the pppAuthenticationType." + e.getMessage());
		}
	}
	
	/**
	 * Return the latest retrieved pppAuthenticationType
	 * @return
	 * @throws IOException 
	 */
	public PPPAuthenticationType getPPPAuthenticationType() throws IOException{
		if(this.pppAuthentication == null){
			readPPPAuthenticationType();	// do a dummy read
		}
		return this.pppAuthentication;
	}
	
	/**
	 * Write a self build pppAuthenticationType to the device
	 * @param pppAuthenticationType
	 * @throws IOException
	 */
	public void writePPPAuthenticationType(PPPAuthenticationType pppAuthenticationType) throws IOException{
		write(ATTRB_PPP_AUTHENTICATION, pppAuthenticationType.getBEREncodedByteArray());
		this.pppAuthentication = pppAuthenticationType;
	}
	
	/**
	 * Write a self build pppAuthenticationStructure to the device
	 * @param pppAuthenticationType
	 * @throws IOException
	 */
	public void writePPPAuthenticationType(Structure pppAuthenticationType) throws IOException{
		write(ATTRB_PPP_AUTHENTICATION, pppAuthenticationType.getBEREncodedByteArray());
		this.pppAuthentication = new PPPAuthenticationType(pppAuthenticationType.getBEREncodedByteArray());
	}
	
	public class PPPAuthenticationType{

		private final static int CHAP_MD5 = 0x05;	// default
		private final static int CHAP_SHA_1 = 0x06;
		private final static int CHAP_MS_SHAP = 0x80;
		private final static int CHAP_MS_CHAP2 = 0x81;
		
		private int authentication = -1;
		
		private OctetString username = null;
		private OctetString password = null;
		private Unsigned8 algorithmId = new Unsigned8(CHAP_MD5);	// the default value is 5(MD5)
		
		private BooleanObject md5Challange = null;
		private BooleanObject oneTimePassword = null;
		private BooleanObject genericTokenCard = null;
		
		private DataContainer dataContainer = null;
		
		public PPPAuthenticationType(){
		}
		
		public byte[] getBEREncodedByteArray() throws IOException {
			switch(this.authentication){
			case LCPOptionsType.AUTH_NO_AUTHENTICATION : {return new NullData().getBEREncodedByteArray();}
			case LCPOptionsType.AUTH_PAP : {
				Structure papStruct = new Structure();
				papStruct.addDataType(getUsername());
				papStruct.addDataType(getPassword());
				return papStruct.getBEREncodedByteArray();
			}
			case LCPOptionsType.AUTH_CHAP : {}	// TODO
			case LCPOptionsType.AUTH_EAP : {}	//TODO
			default : {throw new IOException("Could not get the BEREncodedByteArray for authenticationtype " + this.authentication);}
			}
		}

		public PPPAuthenticationType(byte[] responseData) throws IOException {
			this.dataContainer = new DataContainer();
			try {
				this.dataContainer.parseObjectList(responseData, protocolLink.getLogger());
				if(DEBUG)this.dataContainer.printDataContainer();
				
				if(getLCPOptionsType().getAuthProt() != null){
					switch(getLCPOptionsType().getAuthProt().getValue()){
					case LCPOptionsType.AUTH_NO_AUTHENTICATION : {this.authentication = LCPOptionsType.AUTH_NO_AUTHENTICATION;}break;
					case LCPOptionsType.AUTH_PAP : {
						this.authentication = LCPOptionsType.AUTH_PAP;
						this.username = new OctetString(this.dataContainer.getRoot().getOctetString(0).getArray());	// conversion from one octetString to the other ...
						this.password = new OctetString(this.dataContainer.getRoot().getOctetString(1).getArray());	// conversion from one octetString to the other ...
						}break;
					case LCPOptionsType.AUTH_CHAP : {
						this.authentication = LCPOptionsType.AUTH_CHAP;
						this.username = new OctetString(this.dataContainer.getRoot().getOctetString(0).getArray());	// conversion from one octetString to the other ...
						this.algorithmId = new Unsigned8(this.dataContainer.getRoot().getInteger(1));
						}break;
					case LCPOptionsType.AUTH_EAP : {
						this.authentication = LCPOptionsType.AUTH_EAP;
						this.md5Challange = new BooleanObject(this.dataContainer.getRoot().getInteger(0)==0?false:true);
						this.oneTimePassword = new BooleanObject(this.dataContainer.getRoot().getInteger(1)==0?false:true);
						this.genericTokenCard = new BooleanObject(this.dataContainer.getRoot().getInteger(2)==0?false:true);
						}break;
					default : {throw new IOException("Unknown AuthenticationProtocol value: " + getLCPOptionsType().getAuthProt().getValue());}
					}
				} else {
					throw new IOException("Could not get the AuthenticationProtocol.");
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new IOException("PPPAuthentication - can't parse response." + e.getMessage());
			}
		}
		
		public void setUserName(String userName){
			this.username = OctetString.fromString(userName);
		}
		
		public void setPassWord(String password){
			this.password = OctetString.fromString(password);
		}
		
		public void setUserName(OctetString userName){
			this.username = userName;
		}
		
		public void setPassWord(OctetString password){
			this.password = password;
		}
		
		public void setAuthenticationType(int type){
			this.authentication = type;
		}
		
		public int getAuthentication() {
			return this.authentication;
		}

		public OctetString getUsername() {
			return this.username;
		}

		public OctetString getPassword() {
			return this.password;
		}

		public Unsigned8 getAlgorithmId() {
			return this.algorithmId;
		}

		public BooleanObject getMd5Challange() {
			return this.md5Challange;
		}

		public BooleanObject getOneTimePassword() {
			return this.oneTimePassword;
		}

		public BooleanObject getGenericTokenCard() {
			return this.genericTokenCard;
		}
	}

	public class LCPOptionsType{
		
		private final static int LCP_OPTION_TYPE = 0;
		private final static int LCP_OPTION_LENGTH = 1;
		private final static int LCP_OPTION_DATA = 2;
		
		private final static int DATA_MRU = 1;
		private final static int DATA_ACCM = 2;
		private final static int DATA_AUTH_PROT = 3;
		private final static int DATA_MAG_NUM = 5;
		private final static int DATA_PROTF_COMPR = 7;
		private final static int DATA_ADCTR_COMPR = 8;
		private final static int DATA_FCS_ALTER = 9;
		private final static int DATA_CALLBACK = 13;
		
		public final static int AUTH_NO_AUTHENTICATION = 0x000;
		public final static int AUTH_PAP = 0xc023;
		public final static int AUTH_CHAP = 0xc223;
		public final static int AUTH_EAP = 0xc227;
		
		private DataContainer dataContainer = null;
		private Unsigned16 mru = null;
		private Unsigned32 accm = null;
		private Unsigned16 authProt = null;
		private Unsigned32 magNum = null;
		private BooleanObject protFCompr = null;
		private BooleanObject adCtrCompr = null;
		private Unsigned8 fcsAlter = null;
		private CallBackData callBack = null;
		
		public LCPOptionsType(byte[] responseData) throws IOException {
			this.dataContainer = new DataContainer();
			this.dataContainer.parseObjectList(responseData, protocolLink.getLogger());
			if(DEBUG)this.dataContainer.printDataContainer();
				
			for (int i=0; i<dataContainer.getRoot().getNrOfElements(); i++) {
				switch(dataContainer.getRoot().getStructure(i).getInteger(LCP_OPTION_TYPE)){
				case DATA_MRU : {this.mru = new Unsigned16((int)dataContainer.getRoot().getValue(LCP_OPTION_DATA)&0xFFFF);}break;
				case DATA_ACCM : {this.accm = new Unsigned32((int)dataContainer.getRoot().getStructure(i).getValue(LCP_OPTION_DATA)&0xFFFF);}break;
				case DATA_AUTH_PROT : {this.authProt = new Unsigned16((int)dataContainer.getRoot().getStructure(i).getValue(LCP_OPTION_DATA)&0xFFFF);}break;
				case DATA_MAG_NUM : {this.magNum = new Unsigned32((int)dataContainer.getRoot().getStructure(i).getValue(LCP_OPTION_DATA)&0xFFFF);}break;
				case DATA_PROTF_COMPR : {this.protFCompr = new BooleanObject((dataContainer.getRoot().getStructure(i).getValue(LCP_OPTION_DATA) == 0)?false:true);}break;
				case DATA_ADCTR_COMPR : {this.adCtrCompr = new BooleanObject((dataContainer.getRoot().getStructure(i).getValue(LCP_OPTION_DATA) == 0)?false:true);}break;
				case DATA_FCS_ALTER : {this.fcsAlter = new Unsigned8((int)dataContainer.getRoot().getStructure(i).getValue(LCP_OPTION_DATA)&0xFFFF);}break;
				case DATA_CALLBACK : {this.callBack = new CallBackData(dataContainer.getRoot().getStructure(i).getElement(LCP_OPTION_DATA));}break;
				default : {throw new IOException("Unknown LCP-Option-Data element: " + (int)dataContainer.getRoot().getStructure(i).getValue(LCP_OPTION_TYPE));}
				}
			}
		}
		
		public Unsigned16 getMru() {
			return this.mru;
		}

		public Unsigned32 getAccm() {
			return this.accm;
		}

		public Unsigned16 getAuthProt() {
			return this.authProt;
		}

		public Unsigned32 getMagNum() {
			return this.magNum;
		}

		public BooleanObject getProtFCompr() {
			return this.protFCompr;
		}

		public BooleanObject getAdCtrCompr() {
			return this.adCtrCompr;
		}

		public Unsigned8 getFcsAlter() {
			return this.fcsAlter;
		}

		public CallBackData getCallBack() {
			return this.callBack;
		}
		
		public class CallBackData{

			public CallBackData(Object element) {
				// TODO Auto-generated constructor stub
			}
			
		}
		
	}
	
	public class IPCPOptionsType{
		
	}
	
}
