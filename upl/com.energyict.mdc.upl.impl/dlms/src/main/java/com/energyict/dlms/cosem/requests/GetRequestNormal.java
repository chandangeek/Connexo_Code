package com.energyict.dlms.cosem.requests;

/**
 * @author jme
 *
 */
public class GetRequestNormal extends AbstractSequence {

	private InvokeIdAndPriority invokeIdAndPriority;
	private CosemAttributeDescriptor cosemAttributeDescriptor;
	private SelectiveAccessDescriptor selectiveAccessDescriptor;

	public Field[] getFields() {
		return new Field[] {
				invokeIdAndPriority,
				cosemAttributeDescriptor,
				selectiveAccessDescriptor
		};
	}

	public void setInvokeIdAndPriority(InvokeIdAndPriority invokeIdAndPriority) {
		this.invokeIdAndPriority = invokeIdAndPriority;
	}

	public void setCosemAttributeDescriptor(CosemAttributeDescriptor cosemAttributeDescriptor) {
		this.cosemAttributeDescriptor = cosemAttributeDescriptor;
	}

	public void setSelectiveAccessDescriptor(SelectiveAccessDescriptor selectiveAccessDescriptor) {
		this.selectiveAccessDescriptor = selectiveAccessDescriptor;
	}

}
