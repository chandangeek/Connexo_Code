/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.as220;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;

import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.protocolimpl.base.DLMSAttributeMapper;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author jme
 */
public class ExtendedLogging {

	private final AS220			as220;
	private static final String	CRLF	= "\r\n";

	/**
	 * @param as220
	 */
	public ExtendedLogging(AS220 as220) {
		this.as220 = as220;
	}

	public AS220 getAs220() {
		return as220;
	}

	public String getExtendedLogging() throws IOException {
		StringBuffer sb = new StringBuffer();
		sb.append(CRLF);

		sb.append("********************* All instantiated objects in the meter *********************\n");
		sb.append(getObjectListInfo());

		sb.append("********************* Objects captured into load profile *********************\n");
		sb.append(getProfileInfo());

		sb.append("************************** Custom registers **************************\n");
		sb.append(getCustumRegistersInfo());

		sb.append("************************** Mapped attribute registers **************************\n");
		sb.append(getMappedAttributesInfo());

		return sb.toString();
	}

	private String getMappedAttributesInfo() throws IOException {
		StringBuilder sb = new StringBuilder();

		if (getAs220().getAs220ObisCodeMapper() instanceof As220ObisCodeMapper) {
			As220ObisCodeMapper om = (As220ObisCodeMapper) getAs220().getAs220ObisCodeMapper();
			for (DLMSAttributeMapper mapper : om.getAttributeMappers()) {
				for (int index = 0; index < mapper.getSupportedAttributes().length; index++) {
					int attribute = mapper.getSupportedAttributes()[index];
					ObisCode oc = mapper.getBaseObjectObisCode();
					final ObisCode obisCode = new ObisCode(oc.getA(), oc.getB(), oc.getC(), oc.getD(), oc.getE(), attribute);
					RegisterInfo ri = mapper.getRegisterInfo(obisCode);
					if (ri != null) {
						sb.append(obisCode).append(" = ").append(ri.toString()).append(CRLF);
					}
				}
			}
		}

		return sb.append(CRLF).toString();
	}

	/**
	 * @return
	 */
	private String getCustumRegistersInfo() {
		StringBuilder sb = new StringBuilder();
		Iterator it = RegisterDescription.INFO.keySet().iterator();
		while (it.hasNext()) {
			ObisCode oc = ObisCode.fromString((String) it.next());
			sb.append(oc.toString()).append(" = ").append(RegisterDescription.INFO.get(oc.toString()));
			sb.append(CRLF);
		}
		return sb.append(CRLF).toString();
	}

	/**
	 * @param sb
	 * @throws IOException
	 */
	private String getProfileInfo() throws IOException {
		StringBuilder sb = new StringBuilder();
		Iterator it = getAs220().getCosemObjectFactory().getLoadProfile().getProfileGeneric().getCaptureObjects().iterator();
		while (it.hasNext()) {
			CapturedObject capturedObject = (CapturedObject) it.next();
			sb.append(capturedObject.getLogicalName().getObisCode().toString() + " " + capturedObject.getLogicalName().getObisCode().getDescription()
					+ " (load profile)\n");
		}
		return sb.append(CRLF).toString();
	}

	/**
	 * @param CRLF
	 * @param sb
	 * @throws IOException
	 */
	private String getObjectListInfo() throws IOException {
		StringBuffer sb = new StringBuffer();
		UniversalObject[] uo = getAs220().getMeterConfig().getInstantiatedObjectList();
		for (UniversalObject universalObject : uo) {

			final ObisCode oc = universalObject.getObisCode();
			final String shortName = "[" + universalObject.getBaseName() + "]";
			final String classType = DLMSClassId.findById(universalObject.getClassID()).name();
			final String description = oc.getDescription().equalsIgnoreCase(oc.toString()) ? RegisterDescription.INFO.get(oc.toString()) : oc.getDescription();

			sb.append(oc.toString()).append(" ");
			sb.append(shortName).append(" = ");
			sb.append(classType);
			if (description != null) {
				sb.append(" ");
				sb.append(description);
			}
			sb.append(CRLF);
		}
		return sb.append(CRLF).toString();
	}

}
