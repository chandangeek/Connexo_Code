package com.energyict.protocolimpl.dlms.as220;

import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocolimpl.base.DLMSAttributeMapper;

import java.io.IOException;

/**
 * @author jme
 */
public class ExtendedLogging {

	private final AS220 as220;
	private static final String	CRLF = "\r\n";

	public ExtendedLogging(AS220 as220) {
		this.as220 = as220;
	}

	public AS220 getAs220() {
		return as220;
	}

	public String getExtendedLogging() throws IOException {
		StringBuilder builder = new StringBuilder();
		builder.append(CRLF);

		builder.append("********************* All instantiated objects in the meter *********************\n");
		builder.append(getObjectListInfo());

		builder.append("********************* Objects captured into load profile *********************\n");
		builder.append(getProfileInfo());

		builder.append("************************** Custom registers **************************\n");
		builder.append(getCustumRegistersInfo());

		builder.append("************************** Mapped attribute registers **************************\n");
		builder.append(getMappedAttributesInfo());

		return builder.toString();
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

	private String getCustumRegistersInfo() {
		StringBuilder sb = new StringBuilder();
        for (String s : RegisterDescription.INFO.keySet()) {
            ObisCode oc = ObisCode.fromString(s);
            sb.append(oc.toString()).append(" = ").append(RegisterDescription.INFO.get(oc.toString()));
            sb.append(CRLF);
        }
		return sb.append(CRLF).toString();
	}

	private String getProfileInfo() throws IOException {
		StringBuilder builder = new StringBuilder();
		for (CapturedObject capturedObject : getAs220().getCosemObjectFactory().getLoadProfile().getProfileGeneric().getCaptureObjects()) {
			builder
                .append(capturedObject.getLogicalName().getObisCode().toString())
                .append(" ")
                .append(capturedObject.getLogicalName().getObisCode().toString())
                .append(" (load profile)\n");
		}
		return builder.append(CRLF).toString();
	}

	private String getObjectListInfo() {
		StringBuilder builder = new StringBuilder();
		UniversalObject[] uo = getAs220().getMeterConfig().getInstantiatedObjectList();
		for (UniversalObject universalObject : uo) {

			final ObisCode oc = universalObject.getObisCode();
			final String shortName = "[" + universalObject.getBaseName() + "]";
			final String classType = DLMSClassId.findById(universalObject.getClassID()).name();
			final String description = oc.toString().equalsIgnoreCase(oc.toString()) ? RegisterDescription.INFO.get(oc.toString()) : oc.toString();

			builder.append(oc.toString()).append(" ");
			builder.append(shortName).append(" = ");
			builder.append(classType);
			if (description != null) {
				builder.append(" ");
				builder.append(description);
			}
			builder.append(CRLF);
		}
		return builder.append(CRLF).toString();
	}

}
