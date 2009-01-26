package com.energyict.genericprotocolimpl.iskrap2lpc;

import java.io.IOException;
import java.util.Calendar;

import javax.xml.rpc.ServiceException;

import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.genericprotocolimpl.iskrap2lpc.handlers.ResultsFileHandler;

/**
 * @author gna
 * Changes:
 * GNA|05012009| - getExactFileName needs additional zeros when month was smaller then 10
 */

public class ResultsFile {

	private MeterReadTransaction mrt;
	private String fileName;
	private ResultsFileHandler handler;
	private String[] primaryAddress = {"0", "0", "0", "0"};
	private String[] serialNumbers = {"", "", "", ""};
	private String[] mediums = {"0", "0", "0", "0"};
	private String[] VIF = {"0", "0", "0", "0"};
	private String configChange = "0";
	
	public ResultsFile(){
	}
	
	public ResultsFile(MeterReadTransaction mrt) throws ServiceException, BusinessException, IOException{
		this.mrt = mrt;
		this.fileName = getExactFileName();
		downloadAndParse();
	}
	
	private void downloadAndParse() throws ServiceException, BusinessException, IOException{
		try {
			int fileSize = mrt.getConnection().getFileSize(Constant.defaultDirectory+this.fileName);
			byte[] fileChunk = mrt.getConnection().downloadFileChunk(Constant.defaultDirectory+this.fileName, 0, fileSize);
			handler = new ResultsFileHandler(this);
			String result = FileUtils.convertZippedBytesToString(fileChunk);
			mrt.getConcentrator().importData(result, this.handler);
		} catch (ServiceException e) {
			e.printStackTrace();
			throw new ServiceException(e);
		} catch (BusinessException e) {
			e.printStackTrace();
			throw new BusinessException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException();
		}
	}
	
	private String getExactFileName() throws ServiceException, BusinessException, IOException{
		try {
			Calendar cal = Calendar.getInstance(); 
			int month = cal.get(Calendar.MONTH)+1;
			int year = cal.get(Calendar.YEAR);
			String filter = "*" + mrt.getMeter().getSerialNumber() + "_"+year + ((month < 10)?"0"+month:month) + ".plez";
//			String filter = "*";
			String[] files = mrt.getConnection().getFiles(Constant.defaultDirectory, filter);
			
			// Tricky, it can be in the beginning of the month that the file is not on the concentrator yet, so we get the one from last month.
			if(files.length == 0){	
				month--;
				if (month == 0){
					month = 12;
					year--;
				}
				filter = "*" + mrt.getMeter().getSerialNumber() + "_"+year + ((month < 10)?"0"+month:month) + ".plez";
				files = mrt.getConnection().getFiles(Constant.defaultDirectory, filter);
			}
			
			if(files.length == 0){
				throw new IOException("Could not retrieve the necessary configuration parameters");
			} else {
				return files[0];
			}
		} catch (ServiceException e) {
			e.printStackTrace();
			throw new ServiceException(e);
		} catch (BusinessException e) {
			e.printStackTrace();
			throw new BusinessException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException();
		}
	}

	public void setPrimaryAddress(int i, String value) {
		this.primaryAddress[i] = value;
	}

	public void setCustomerID(int i, String value) {
//		if(ParseUtils.checkIfAllAreChars(parseStr))
//		str = new String(parseStr);
//	else
//		str = ParseUtils.decimalByteToString(parseStr);
//	return str;
		
		//TODO can be that the watermeter returns an different serialnumber then expected
		byte[] b = value.getBytes();
		String str = "";
		if(ParseUtils.checkIfAllAreChars(b)){
			str = new String(b);
		} else{
			str = ParseUtils.decimalByteToString(b);
		}
//		return str;
		this.serialNumbers[i] = str;
	}

	public void setMedium(int i, String value) {
		this.mediums[i] = value;
	}

	public void setVIF(int i, String value) {
		this.VIF[i] = value;
	}

	public void setConfigurationChange(String value) {
		this.configChange = value;
	}

	public String getPrimaryAddress(int i) {
		return primaryAddress[i];
	}

	public String getSerialNumbers(int i) {
		return serialNumbers[i];
	}

	public String getMediums(int i) {
		return mediums[i];
	}

	public String getVIF(int i) {
		return VIF[i];
	}

	public String getConfigChange() {
		return configChange;
	}
	
}
