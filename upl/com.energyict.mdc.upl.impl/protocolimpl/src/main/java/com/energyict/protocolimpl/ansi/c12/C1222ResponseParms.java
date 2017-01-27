package com.energyict.protocolimpl.ansi.c12;

import com.energyict.cbo.ApplicationException;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class C1222ResponseParms 
{
	private String applicationContext = "";
	private String calledApTitle;
	private long calledApInvocationId = -1;
	private String callingApTitle;
	private long callingAeQualifier = -1;
	private long callingApInvocationId;
	private int securityKeyId = 0;
	private long initializationVector = 0;
	private byte[] mac = new byte[4];
	private byte[] userInformation;
	private byte epsemControl;
    private int securityMode = 0;
    private int uiExtra = 0;
    private int epsemSize = 0;
    private boolean securityKeyIdAndInitializationVectorWereSent = false;

	public static final int MAC_LENGTH = 4;

	public byte getEpsemControl() {
		return epsemControl;
	}
	public void setEpsemControl(byte epsemControl) {
		this.epsemControl = epsemControl;
	}
	public byte[] getUserInformation() {
		return userInformation;
	}
	public void setUserInformation(byte[] userInformation) {
		this.userInformation = userInformation;
	}
	public byte[] getMac() {
		return mac;
	}
	public void setMac(byte[] mac) {
		this.mac = mac;
	}
	public long getInitializationVector() {
		return initializationVector;
	}
	public void setInitializationVector(long initializationVector) {
		this.initializationVector = initializationVector;
	}
	public int getSecurityKeyId() {
		return securityKeyId;
	}
	public void setSecurityKeyId(int securityKeyId) {
		this.securityKeyId = securityKeyId;
	}
	public String getApplicationContext() {
		return applicationContext;
	}
	public void setApplicationContext(String applicationContext) {
		this.applicationContext = applicationContext;
	}
	public String getCalledApTitle() {
		return calledApTitle;
	}
	public void setCalledApTitle(String calledApTitle) {
		this.calledApTitle = calledApTitle;
	}
	public long getCalledApInvocationId() {
		return calledApInvocationId;
	}
	public void setCalledApInvocationId(long calledApInvocationId) {
		this.calledApInvocationId = calledApInvocationId;
	}
	public String getCallingApTitle() {
		return callingApTitle;
	}
	public void setCallingApTitle(String callingApTitle) {
		this.callingApTitle = callingApTitle;
	}
	public long getCallingAeQualifier() {
		return callingAeQualifier;
	}
	public void setCallingAeQualifier(long callingAeQualifier) {
		this.callingAeQualifier = callingAeQualifier;
	}
	public long getCallingApInvocationId() {
		return callingApInvocationId;
	}
	public void setCallingApInvocationId(long callingApInvocationId) {
		this.callingApInvocationId = callingApInvocationId;
	}
    public int getSecurityMode() {
        return securityMode;
    }
    public void setSecurityMode(int securityMode) {
        this.securityMode = securityMode;
    }
    public boolean isSecurityKeyIdAndInitializationVectorWereSent() {
		return securityKeyIdAndInitializationVectorWereSent;
	}
    public void setSecurityKeyIdAndInitializationVectorWereSent(
			boolean securityKeyIdAndInitializationVectorWereSent) {
		this.securityKeyIdAndInitializationVectorWereSent = securityKeyIdAndInitializationVectorWereSent;
	}
    public int getUiExtra() {
		return uiExtra;
	}
	public void setUiExtra(int uiExtra) {
		this.uiExtra = uiExtra;
	}
	public int getEpsemSize() {
		return epsemSize;
	}
	public void setEpsemSize(int epsemSize) {
		this.epsemSize = epsemSize;
	}

    public void assignFieldValue(int fieldId, ByteArrayOutputStream fieldValue) throws IOException
    {
		switch (fieldId)
		{
			case 0xA1:
			{
				setApplicationContext(extractUid(fieldValue.toByteArray()));
				break;
			}
			case 0xA2:
			{
				setCalledApTitle(extractUid(fieldValue.toByteArray()));
				break;
			}
			case 0xA4:
			{
				setCalledApInvocationId(extractInteger(fieldValue.toByteArray()));
				break;
			}
			case 0xA6:
			{
				setCallingApTitle(extractUid(fieldValue.toByteArray()));
				break;
			}
			case 0xA7:
			{
				setCallingAeQualifier(extractInteger(fieldValue.toByteArray()));
				break;
			}
			case 0xA8:
			{
				setCallingApInvocationId(extractInteger(fieldValue.toByteArray()));
				break;
			}
			case 0xAC:
			{
				byte[] byteArray = fieldValue.toByteArray();
				setSecurityKeyId(byteArray[8]);
				setInitializationVector(ProtocolUtils.getLongLE(byteArray, 11, 4));
				break;
			}
			case 0xBE:
			{
				extractAndSetUserInformation(fieldValue.toByteArray());
				break;
			}
			default: break;
		}
    }
    
    private void extractAndSetUserInformation(byte[] byteArray) throws IOException
    {
		int tempUserInfoIndirectReference = 0;
		byte tempEpsem = 0;
		byte[] tempUserInformation;
		byte[] tempMac = new byte[4];
		int pos = 0;
		int tempLength = 0;

		if (byteArray[pos] != 0x28) 
			throw new ApplicationException("First byte of user information must be 0x28");
		
		tempLength = byteArray[++pos]; // read off length bytes
		if ((tempLength & 0x80) != 0)
		{
			tempLength &= 0x7F;
			pos = pos + tempLength;
		}
		
		tempUserInfoIndirectReference = byteArray[++pos];
		
		tempLength = byteArray[++pos]; // read off length bytes
		if ((tempLength & 0x80) != 0)
		{
			tempLength &= 0x7F;
			pos = pos + tempLength;
		}

		tempEpsem = byteArray[++pos];
        if ((tempEpsem & 0x0C) == 0x00) {
            securityMode = 0;   // Unsecured
        } else if ((tempEpsem & 0x0C) == 0x04) {
            securityMode = 1;   // Authenticated
        } else if ((tempEpsem & 0x0C) == 0x08) {
            securityMode = 2;   // Encrypted
        }

        if (securityMode == 0) {
            tempLength = byteArray[++pos]; // read off length bytes
            if ((tempLength & 0x80) != 0) {
                tempLength &= 0x7F;
                pos = pos + tempLength;
            }
            pos++;
        } else {
            pos++;
            tempLength = (byteArray.length - pos) - MAC_LENGTH;
        }

		tempUserInformation = new byte[tempLength];
		System.arraycopy(byteArray, pos, tempUserInformation, 0, tempLength);
		if (securityMode != 0) {
            System.arraycopy(byteArray, byteArray.length - MAC_LENGTH, tempMac, 0, MAC_LENGTH);
        }
		setEpsemControl(tempEpsem);
		setUserInformation(tempUserInformation);
		setMac(tempMac);
    }
    
    private long extractInteger(byte[] byteArray)
    {
    	long result = 0;
    	byte fieldType = byteArray[0];
    	
    	if (fieldType != 0x02) 
    		throw new ApplicationException("Invalid Field Type");
    	
		for(int i = 2; i < byteArray.length; i++)
		{
			result <<= 8;
			result |= byteArray[i];
		}
		
    	return result;
    }
    
    private String extractUid(byte[] byteArray)
    {
    	StringBuffer result = new StringBuffer();
    	byte fieldType = byteArray[0];

    	if (fieldType != 0x80 && fieldType != 0x06) 
    		throw new ApplicationException("Invalid Field Type");
    	
    	result.append(byteArray[2] / 40);
    	result.append(".");
    	result.append(byteArray[2] % 40);
    	
    	long number = 0;
		for(int i = 3; i < byteArray.length; i++)
		{
			number <<= 7;
			int myByte = byteArray[i];
			if ((myByte & 0x80) == 0)
			{
				result.append(".");
				number += myByte;
				result.append(number);
				number = 0;
			}
			else
			{
				number += (long)(myByte & 0x7F);
			}
		}
		
    	return result.toString();    	
    }

    public void reset() {
        applicationContext = new String();
        calledApTitle = new String();
        calledApInvocationId = -1;
        callingApTitle = new String();
        callingAeQualifier = -1;
        callingApInvocationId = -1;
        securityKeyId = 0;
        mac = new byte[4];
        userInformation = new byte[]{};
        epsemControl = 0x00;
        securityMode = 0;
        uiExtra = 0;
        epsemSize = 0;
        // initializationVector not reset - consistent field during whole session
        // securityKeyIdAndInitializationVectorWereSent not reset - consistent field during whole session
    }
}