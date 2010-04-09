package com.energyict.dlms.cosem.requests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author jme
 *
 */
public class GetRequestWithList extends AbstractSequence {

	private InvokeIdAndPriority invokeIdAndPriority = null;
	private List<CosemAttributeDescriptorWithSelection> getDataResults = null;

	public Field[] getFields() {
		Field[] fields = new Field[getCosemAttributeDescriptorWithSelections().size() + 1];
		fields[0] = getInvokeIdAndPriority();
		int ptr = 1;
		for (Iterator it = getCosemAttributeDescriptorWithSelections().iterator(); it.hasNext();) {
			CosemAttributeDescriptorWithSelection getDataResult = (CosemAttributeDescriptorWithSelection) it.next();
			fields[ptr++] = getDataResult;
		}
		return fields;
	}

	/**
	 * @param invokeIdAndPriority
	 * @param getDataResults
	 */
	public GetRequestWithList(InvokeIdAndPriority invokeIdAndPriority, List<CosemAttributeDescriptorWithSelection> getDataResults) {
		this.invokeIdAndPriority = invokeIdAndPriority;
		this.getDataResults = getDataResults;
	}

	/**
	 * @param invokeIdAndPriority
	 * @param getDataResults
	 */
	public GetRequestWithList(InvokeIdAndPriority invokeIdAndPriority) {
		this(invokeIdAndPriority, null);
	}

	/**
	 * @param invokeIdAndPriority
	 * @param getDataResults
	 */
	public GetRequestWithList() {
		this(null);
	}

	/**
	 * @return
	 */
	public List<CosemAttributeDescriptorWithSelection> getCosemAttributeDescriptorWithSelections() {
		if (getDataResults == null) {
			getDataResults = new ArrayList<CosemAttributeDescriptorWithSelection>();
		}
		return getDataResults;
	}

	/**
	 * @param getDataResults
	 */
	public void setCosemAttributeDescriptorWithSelections(List<CosemAttributeDescriptorWithSelection> getDataResults) {
		this.getDataResults = getDataResults;
	}

	/**
	 * @param getDataResult
	 */
	public void addCosemAttributeDescriptorWithSelection(CosemAttributeDescriptorWithSelection getDataResult) {
		getCosemAttributeDescriptorWithSelections().add(getDataResult);
	}

	/**
	 * @param getDataResult
	 */
	public void removeCosemAttributeDescriptorWithSelection(CosemAttributeDescriptorWithSelection getDataResult) {
		getCosemAttributeDescriptorWithSelections().remove(getDataResult);
	}

	/**
	 *
	 */
	public void clearCosemAttributeDescriptorWithSelections() {
		getCosemAttributeDescriptorWithSelections().clear();
	}

	/**
	 * @param invokeIdAndPriority
	 */
	public void setInvokeIdAndPriority(InvokeIdAndPriority invokeIdAndPriority) {
		this.invokeIdAndPriority = invokeIdAndPriority;
	}

	/**
	 * @return
	 */
	public InvokeIdAndPriority getInvokeIdAndPriority() {
		if (invokeIdAndPriority == null) {
			invokeIdAndPriority = new InvokeIdAndPriority(0);
		}
		return invokeIdAndPriority;
	}

}
