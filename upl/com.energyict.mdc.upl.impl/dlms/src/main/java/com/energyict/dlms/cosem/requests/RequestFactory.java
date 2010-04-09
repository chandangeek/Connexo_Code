package com.energyict.dlms.cosem.requests;

import java.util.ArrayList;
import java.util.List;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.ProtocolLink;

/**
 * @author jme
 *
 */
public class RequestFactory {

	private final ProtocolLink protocolLink;

	public RequestFactory(ProtocolLink protocolLink) {
		this.protocolLink = protocolLink;
	}

	public ProtocolLink getProtocolLink() {
		return protocolLink;
	}

	private DLMSConnection getDLMSConnenction() {
		return getProtocolLink().getDLMSConnection();
	}

	public InvokeIdAndPriority getInvokeIdAndPriority() {
		return new InvokeIdAndPriority(getDLMSConnenction().getInvokeIdAndPriority().getInvokeIdAndPriorityData());
	}

	public XDlmsLNApdu createGetWithListRequest(List<DLMSAttribute> dlmsAttributes) {
		ArrayList<CosemAttributeDescriptorWithSelection> requestedData = new ArrayList<CosemAttributeDescriptorWithSelection>();
		for (DLMSAttribute dlmsAttribute : dlmsAttributes) {
			CosemAttributeDescriptorWithSelection descriptor = new CosemAttributeDescriptorWithSelection(dlmsAttribute.getObisCode(), dlmsAttribute.getAttribute(), dlmsAttribute.getClassId().getClassId());
			requestedData.add(descriptor);
		}

		GetRequestWithList getRequestWithList = new GetRequestWithList(getInvokeIdAndPriority(), requestedData);
		GetRequest getRequest = new GetRequest();
		getRequest.setChoiceObject(getRequestWithList);
		XDlmsLNApdu apdu = new XDlmsLNApdu();
		apdu.setChoiceObject(getRequest);
		return apdu;
	}


}
