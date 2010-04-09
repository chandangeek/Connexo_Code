package com.energyict.dlms.cosem.requests;

import com.energyict.obis.ObisCode;

/**
 * @author jme
 *
 */
public class CosemAttributeDescriptor extends AbstractSequence {

	private CosemClassId cosemClassId;
	private CosemObjectInstanceId cosemObjectInstanceId;
	private CosemObjectAttributeId cosemObjectAttributeId;

	public CosemAttributeDescriptor(ObisCode obisCode, int attributeId, int classId) {
		cosemClassId = new CosemClassId(classId);
		cosemObjectAttributeId = new CosemObjectAttributeId(attributeId);
		cosemObjectInstanceId = new CosemObjectInstanceId(obisCode);
	}

	public Field[] getFields() {
		return new Field[] {
				cosemClassId,
				cosemObjectInstanceId,
				cosemObjectAttributeId
		};
	}


}
