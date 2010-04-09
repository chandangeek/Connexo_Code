package com.energyict.dlms.cosem.requests;

import com.energyict.obis.ObisCode;

/**
 * @author jme
 *
 */
public class CosemAttributeDescriptorWithSelection extends AbstractSequence {

	private CosemAttributeDescriptor cosemAttributeDescriptor;
	private SelectiveAccessDescriptor selectiveAccessDescriptor;

	public CosemAttributeDescriptorWithSelection(ObisCode obisCode, int attributeId, int classId) {
		cosemAttributeDescriptor = new CosemAttributeDescriptor(obisCode, attributeId, classId);
	}

	public Field[] getFields() {
		return new Field[] {
			cosemAttributeDescriptor,
			selectiveAccessDescriptor
		};
	}

}
