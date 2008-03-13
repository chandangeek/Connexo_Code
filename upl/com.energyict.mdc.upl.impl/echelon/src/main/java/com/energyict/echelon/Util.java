package com.energyict.echelon;

import java.io.*;
import java.text.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.*;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.*;

class Util {

	public static final String BILLING_DATA_TAG = "BILLINGDATA";
	public static final String CHANNEL_TAG = "CHANNEL";
	public static final String DATETIME_TAG = "DATETIME";
	public static final String DEVICE_TAG = "DEVICE";
	public static final String EVENT_TAG = "EVENT";
	public static final String EVENT_NUMBER_TAG = "EVENTNUMBER";
	public static final String EVENT_DATA_TAG = "EVENTDATA";
	public static final String ID_TAG = "ID";
	public static final String INTERVAL_TAG = "INTERVAL";
	public static final String NUMBEROFCHANNELS_TAG = "NUMBEROFCHANNELS";
	public static final String RAW_DATA_TAG = "RAWDATA";
	public static final String RESULT_TAG = "RESULT";
	public static final String TYPEID_TAG = "TYPEID";

	public static final SimpleDateFormat DATE_FORMAT;
	public static final TimeZone UTC_ZONE = TimeZone.getTimeZone("UTC");

	static {
		DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	}

	/** utility method for getting nodeValue */
	static String getNodeValue(Element e, String tagName) {
		if (e == null)
			return null;
		NodeList nl = e.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0
				&& nl.item(0).getFirstChild() != null)
			return nl.item(0).getFirstChild().getNodeValue();
		return null;
	}

	/** utility method for getting nodeValue */
	static String getNodeValue(Document d, String tagName) {
		NodeList nl = d.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0
				&& nl.item(0).getFirstChild() != null)
			return nl.item(0).getFirstChild().getNodeValue();
		return null;
	}

	/** utility method for getting nodeValue date */
	static Date getNodeDate(Element e, String tagName) throws ParseException {
		return getNodeDate(e, tagName, UTC_ZONE);
	}

	static Date getNodeDate(Element e, String tagName, TimeZone timeZone)
			throws ParseException {
		String s = getNodeValue(e, tagName);
		return parseDate(s, timeZone);
	}

	/** utility method for getting nodeValue */
	static Date getNodeDate(Document d, String tagName, TimeZone timeZone)
			throws ParseException {
		String s = getNodeValue(d, tagName);
		return parseDate(s, timeZone);
	}

	static Date parseDate(String date, TimeZone timeZone) throws ParseException {
		if (timeZone == null) {
			timeZone = UTC_ZONE;
		}
		SimpleDateFormat format = (SimpleDateFormat) DATE_FORMAT.clone();
		format.setTimeZone(timeZone);
		return format.parse(date);
	}

	/** utility method for getting nodeValue */
	static int getNodeInt(Element e, String tagName) throws ParseException {
		return Integer.parseInt(getNodeValue(e, tagName));
	}

	/** utility method for getting nodeValue */
	static int getNodeInt(Document d, String tagName) throws ParseException {
		return Integer.parseInt(getNodeValue(d, tagName));
	}

	static Element getElementByName(Document doc, String tagName) {
		NodeList nl = doc.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0) {
			return (Element) nl.item(0);
		}
		return null;
	}

	/** find the direct child nodes with a certain name */
	static List collectNodes(Node parent, String tagName) {

		ArrayList result = new ArrayList();
		NodeList list = parent.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			String nodeName = list.item(i).getNodeName();
			if (tagName != null && tagName.equals(nodeName))
				result.add(list.item(i));
		}
		return (List) result;

	}

	/**
	 * find the direct child nodes with a certain name, and return them sorted
	 * on sortTag
	 */
	static List collectNodes(Node parent, String tagName, String sortTag) {

		TreeSet result = new TreeSet(new NodeComparator(sortTag));
		NodeList list = parent.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			String nodeName = list.item(i).getNodeName();
			if (tagName != null && tagName.equals(nodeName))
				result.add(list.item(i));
		}
		return new ArrayList(result);

	}

	/** Comparator for sorting XmlNodes based on string value of element */
	static class NodeComparator implements Comparator {

		String tag;

		NodeComparator(String sortedTag) {
			tag = sortedTag;
		}

		public int compare(Object o1, Object o2) {
			Element node1 = (Element) o1;
			Element node2 = (Element) o2;

			try {
				int thisVal = Util.getNodeInt(node1, tag);
				int anotherVal = Util.getNodeInt(node2, tag);
				return thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0
						: 1);
			} catch (ParseException pex) {
				throw new RuntimeException(pex);
			}
		}

	}

	/** find the first child node with a certain name */
	static Node firstNode(Node parent, String tagName) {

		if (parent == null)
			return null;
		NodeList list = parent.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			String nodeName = list.item(i).getNodeName();
			if (tagName != null && tagName.equals(nodeName))
				return list.item(i);
		}
		return null;

	}

	/**
	 * convert a String object into a Dom tree
	 * 
	 * @throws FactoryConfigurationError
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	static Document toDom(String aString) throws SAXException, IOException,
			ParserConfigurationException, FactoryConfigurationError {
		InputSource is = new InputSource(new StringReader(aString));
		return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
				is);
	}

	static String DocumentToString(Document doc) throws TransformerException {
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult(sw);
		transformer.transform(source, result);
		String xmlString = sw.toString();
		return xmlString;
	}

	static Element xPath(Document d, String expression)
			throws TransformerException {
		return (Element) XPathAPI.selectSingleNode(d, expression);
	}

	static NodeList xPathNodeList(Document d, String expression)
			throws TransformerException {
		return (NodeList) XPathAPI.selectNodeList(d, expression);
	}

	static NodeIterator xPathNodeIterator(Document d, String expression)
			throws TransformerException {
		return (NodeIterator) XPathAPI.selectNodeIterator(d, expression);
	}

	static String scrubHeader(String document) {
		return document.substring(43, document.length()).trim();
	}

	static String getStatus(Document d) throws TransformerException {
		Node node = XPathAPI.selectSingleNode(d, "/RETURNS/STATUS");
		if (node != null && node.getFirstChild() != null)
			return node.getFirstChild().getNodeValue();
		return null;
	}

	static boolean isSucceeded(Document d) throws TransformerException {
		return Constants.ExternalServiceReturnCodes.SUCCEEDED
				.equals(getStatus(d));
	}

	static boolean isSucceeded(String status) {
		return Constants.ExternalServiceReturnCodes.SUCCEEDED.equals(status);
	}

	static Document checkStatus(Document d) throws EchelonException,
			TransformerException {
		if (!isSucceeded(d))
			throw new EchelonException(getStatusDescription(getStatus(d)));
		return d;
	}
	
	static int hexToInt(String hex) {
		int integer;
		try {
			if (hex.length() == 4) {
				hex = hex.substring(2,4) + hex.substring(0,2);
			}
			integer = Integer.parseInt(hex, 16);
		}
		catch (Exception ex) {
			integer = 0;
		}
		return integer;
	}

	static String getStatusDescription(String s) {
		if (s == null)
			return null;
		if (s.equals(Constants.ExternalServiceReturnCodes.SUCCEEDED))
			return "SUCCEEDED";
		if (s.equals(Constants.ExternalServiceReturnCodes.FAILURE))
			return "FAILURE";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_DEVICE_ID))
			return "INVALID_DEVICE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DEVICE_ID_ASSOCIATION_INVALID))
			return "INVALID_DEVICE_ID_ASSOCIATION_INVALID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DEVICE_DATAPOINT_ASSIGNMENT))
			return "INVALID_DEVICE_DATAPOINT_ASSIGNMENT";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DEVICE_ID_ASSIGNMENT))
			return "INVALID_DEVICE_ID_ASSIGNMENT";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_PARAMETERS_NODE_MISSING))
			return "INVALID_PARAMETERS_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_HIERARCHY_LEVEL_MEMBER_ID_NODE_MISSING))
			return "INVALID_HIERARCHY_LEVEL_MEMBER_ID_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_NAME_NODE_MISSING))
			return "INVALID_NAME_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.HIERARCHY_LEVEL_MEMBER_ID_BLANK))
			return "HIERARCHY_LEVEL_MEMBER_ID_BLANK";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DEVICE_NAME_BLANK))
			return "INVALID_DEVICE_NAME_BLANK";
		if (s.equals(Constants.ExternalServiceReturnCodes.DUPLICATE_DEVICE))
			return "DUPLICATE_DEVICE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.DUPLICATE_DEVICE_NAME))
			return "DUPLICATE_DEVICE_NAME";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_ATTRIBUTE_ID))
			return "INVALID_ATTRIBUTE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ATTRIBUTE_VALUE))
			return "INVALID_ATTRIBUTE_VALUE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_NUMERIC_ATTRIBUTE_VALUE))
			return "INVALID_NUMERIC_ATTRIBUTE_VALUE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ATTRIBUTE_TYPE_ID))
			return "INVALID_ATTRIBUTE_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ATTRIBUTE_DEFINEDVALUE_ID))
			return "INVALID_ATTRIBUTE_DEFINEDVALUE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ATTRIBUTE_NAME_DUPLICATE))
			return "INVALID_ATTRIBUTE_NAME_DUPLICATE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ATTRIBUTE_DEFINEDVALUE_NAME_DUPLICATE))
			return "INVALID_ATTRIBUTE_DEFINEDVALUE_NAME_DUPLICATE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ATTRIBUTE_NAME_BLANK))
			return "INVALID_ATTRIBUTE_NAME_BLANK";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ATTRIBUTE_DEFINEDVALUE_NAME_BLANK))
			return "INVALID_ATTRIBUTE_DEFINEDVALUE_NAME_BLANK";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ATTRIBUTE_ID_NOT_DEFINED))
			return "INVALID_ATTRIBUTE_ID_NOT_DEFINED";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ATTRIBUTE_ID_NOT_STRING))
			return "INVALID_ATTRIBUTE_ID_NOT_STRING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ATTRIBUTE_ID_NOT_NUMERIC))
			return "INVALID_ATTRIBUTE_ID_NOT_NUMERIC";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.DUPLICATE_HIERARCHY_ASSIGNMENT))
			return "DUPLICATE_HIERARCHY_ASSIGNMENT";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATA_POINT_ID))
			return "INVALID_DATA_POINT_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_HIERARCHY_LEVEL_MEMBER_ID))
			return "INVALID_HIERARCHY_LEVEL_MEMBER_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_VARIABLE_TYPES_XML))
			return "INVALID_VARIABLE_TYPES_XML";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_VARIABLE_TYPES_XML_EMPTY))
			return "INVALID_VARIABLE_TYPES_XML_EMPTY";
		if (s.equals(Constants.ExternalServiceReturnCodes.BLANK_VARIABLE_NAME))
			return "BLANK_VARIABLE_NAME";
		if (s.equals(Constants.ExternalServiceReturnCodes.VARIABLE_TYPE_IN_USE))
			return "VARIABLE_TYPE_IN_USE";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_SECURITY_KEY))
			return "INVALID_SECURITY_KEY";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TYPE_CATEGORY))
			return "INVALID_TYPE_CATEGORY";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_TYPE_ID))
			return "INVALID_TYPE_ID";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_DATA_TYPE_ID))
			return "INVALID_DATA_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_USER_AUTHENTICATION_TYPE_ID))
			return "INVALID_USER_AUTHENTICATION_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_NUMERIC_RANGE))
			return "INVALID_NUMERIC_RANGE";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_NUMERIC_MIN))
			return "INVALID_NUMERIC_MIN";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_NUMERIC_MAX))
			return "INVALID_NUMERIC_MAX";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DELETE_DATA_POINTS_NODE_MISSING))
			return "INVALID_DELETE_DATA_POINTS_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.DELETE_DATA_POINTS_NODE_MISSING))
			return "DELETE_DATA_POINTS_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DELETE_DATA_POINTS_ID))
			return "INVALID_DELETE_DATA_POINTS_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.DELETE_DATA_POINTS_INVALID))
			return "DELETE_DATA_POINTS_INVALID";
		if (s.equals(Constants.ExternalServiceReturnCodes.PARTIAL_SUCCESS))
			return "PARTIAL_SUCCESS";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_HIERARCHY_LEVEL_ID))
			return "INVALID_HIERARCHY_LEVEL_ID";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_HIERARCHY_ID))
			return "INVALID_HIERARCHY_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_HIERARCHY_NAME_DUPLICATE))
			return "INVALID_HIERARCHY_NAME_DUPLICATE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_HIERARCHY_LEVEL_NAME_DUPLICATE))
			return "INVALID_HIERARCHY_LEVEL_NAME_DUPLICATE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_HIERARCHY_LEVEL_MEMBER_NAME_DUPLICATE))
			return "INVALID_HIERARCHY_LEVEL_MEMBER_NAME_DUPLICATE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_HIERARCHY_LEVEL_MULTIPLE_CHILDREN))
			return "INVALID_HIERARCHY_LEVEL_MULTIPLE_CHILDREN";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_HIERARCHY_NAME_BLANK))
			return "INVALID_HIERARCHY_NAME_BLANK";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_HIERARCHY_LEVEL_NAME_BLANK))
			return "INVALID_HIERARCHY_LEVEL_NAME_BLANK";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_HIERARCHY_LEVEL_MEMBER_NAME_BLANK))
			return "INVALID_HIERARCHY_LEVEL_MEMBER_NAME_BLANK";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_HIERARCHY_LEVEL_PARENT_ID))
			return "INVALID_HIERARCHY_LEVEL_PARENT_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_HIERARCHY_LEVEL_MEMBER_PARENT_ID))
			return "INVALID_HIERARCHY_LEVEL_MEMBER_PARENT_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_HIERARCHY_LEVEL_MEMBER_PARENT_LEVEL))
			return "INVALID_HIERARCHY_LEVEL_MEMBER_PARENT_LEVEL";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_HIERARCHY_RESTRICTION_TYPE_ID))
			return "INVALID_HIERARCHY_RESTRICTION_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_HIERARCHY_LEVEL_MEMBER_NAME_CHARACTERS))
			return "INVALID_HIERARCHY_LEVEL_MEMBER_NAME_CHARACTERS";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_HIERARCHY_DELETION_TYPE_ID))
			return "INVALID_HIERARCHY_DELETION_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_HIERARCHY_LEVEL_NAME_CHARACTERS))
			return "INVALID_HIERARCHY_LEVEL_NAME_CHARACTERS";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_HIERARCHY_LEVEL_STRUCTURE))
			return "INVALID_HIERARCHY_LEVEL_STRUCTURE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RECURRENCE_TYPE_ID))
			return "INVALID_RECURRENCE_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_STATUS_TYPE_ID))
			return "INVALID_STATUS_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SOURCE_TYPE_ID))
			return "INVALID_SOURCE_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_CALCULATION_TYPE_ID))
			return "INVALID_CALCULATION_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_VARIABLE_TYPE_ID))
			return "INVALID_VARIABLE_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_VARIABLE_TYPE_COMPONENT_ID))
			return "INVALID_VARIABLE_TYPE_COMPONENT_ID";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_TIMEZONE_ID))
			return "INVALID_TIMEZONE_ID";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_STARTDATE))
			return "INVALID_STARTDATE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DEVICE_ID_DEVICE_NOT_TIED_TO_GATEWAY_ID))
			return "INVALID_DEVICE_ID_DEVICE_NOT_TIED_TO_GATEWAY_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATAPOINT_MINMAX_RANGE))
			return "INVALID_DATAPOINT_MINMAX_RANGE";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_ENDDATE))
			return "INVALID_ENDDATE";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_PRIORITY))
			return "INVALID_PRIORITY";
		if (s.equals(Constants.ExternalServiceReturnCodes.COMPONENT_NOT_FOUND))
			return "COMPONENT_NOT_FOUND";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_STOPPED_DATE))
			return "INVALID_STOPPED_DATE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATAPOINT_RESTRICTION_TYPE_ID))
			return "INVALID_DATAPOINT_RESTRICTION_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATAPOINTVALUE_STATUS_TYPE_ID))
			return "INVALID_DATAPOINTVALUE_STATUS_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATAPOINT_VALUE))
			return "INVALID_DATAPOINT_VALUE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_GATEWAY_COMBINATION))
			return "INVALID_GATEWAY_COMBINATION";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATAPOINT_SCHEDULE_ASSIGNMENT))
			return "INVALID_DATAPOINT_SCHEDULE_ASSIGNMENT";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATAPOINT_SCHEDULE_DELETE))
			return "INVALID_DATAPOINT_SCHEDULE_DELETE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_METER_SOFTWARE_VERSION))
			return "INVALID_METER_SOFTWARE_VERSION";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_TRACKING_ID))
			return "INVALID_TRACKING_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TRACKING_ID_NODE_MISSING))
			return "INVALID_TRACKING_ID_NODE_MISSING";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_GATEWAY_ID))
			return "INVALID_GATEWAY_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_GATEWAY_TYPE_ID))
			return "INVALID_GATEWAY_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_GATEWAY_TEMPLATE_PARAMETER_ID))
			return "INVALID_GATEWAY_TEMPLATE_PARAMETER_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_GATEWAY_TEMPLATE_NAME))
			return "INVALID_GATEWAY_TEMPLATE_NAME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.DUPLICATE_GATEWAY_TEMPLATE_NAME))
			return "DUPLICATE_GATEWAY_TEMPLATE_NAME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_GATEWAY_STATUS_TYPE_ID))
			return "INVALID_GATEWAY_STATUS_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ILON100_DATA_LOG_RETRIEVAL_TYPE_ID))
			return "INVALID_ILON100_DATA_LOG_RETRIEVAL_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ILON100_DATA_LOG_USED_PARAMETER_ID))
			return "INVALID_ILON100_DATA_LOG_USED_PARAMETER_ID";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_GATEWAY_NAME))
			return "INVALID_GATEWAY_NAME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_GATEWAY_VARIABLE_NAME))
			return "INVALID_GATEWAY_VARIABLE_NAME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_GATEWAY_VARIABLE_TYPE_COMPONENT_ID))
			return "INVALID_GATEWAY_VARIABLE_TYPE_COMPONENT_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TAKE_HIERARCHIES_PARAMETER_ID))
			return "INVALID_TAKE_HIERARCHIES_PARAMETER_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ATTRIBUTE_PARAMETER_VALUE_NODE_MISSING))
			return "INVALID_ATTRIBUTE_PARAMETER_VALUE_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ATTRIBUTE_PARAMETER_ID_NODE_MISSING))
			return "INVALID_ATTRIBUTE_PARAMETER_ID_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ATTRIBUTE_VALUE_NODE_MISSING))
			return "INVALID_ATTRIBUTE_VALUE_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ATTRIBUTE_ID_NODE_MISSING))
			return "INVALID_ATTRIBUTE_ID_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_COMMUNICATION_REQUEST_TYPE_ID))
			return "INVALID_COMMUNICATION_REQUEST_TYPE_ID";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_DATETIME))
			return "INVALID_DATETIME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_GATEWAY_TEMPLATE_ID))
			return "INVALID_GATEWAY_TEMPLATE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.REGISTRATION_LIMIT_REACHED))
			return "REGISTRATION_LIMIT_REACHED";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_GATEWAY_NAME_BLANK))
			return "INVALID_GATEWAY_NAME_BLANK";
		if (s.equals(Constants.ExternalServiceReturnCodes.DEVICES_IN_USE))
			return "DEVICES_IN_USE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.UNSUPPORTED_GATEWAY_TYPE_ID))
			return "UNSUPPORTED_GATEWAY_TYPE_ID";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_PPP_LOGIN))
			return "INVALID_PPP_LOGIN";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_PPP_LOGIN_BLANK))
			return "INVALID_PPP_LOGIN_BLANK";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_PPP_LOGIN_DUPLICATE))
			return "INVALID_PPP_LOGIN_DUPLICATE";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_PPP_PASSWORD))
			return "INVALID_PPP_PASSWORD";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_PPP_PASSWORD_BLANK))
			return "INVALID_PPP_PASSWORD_BLANK";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_GATEWAY_COMMUNICATION_HISTORY_ID))
			return "INVALID_GATEWAY_COMMUNICATION_HISTORY_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_GATEWAY_COMMUNICATION_HISTORY_INITIATED_START_DATETIME))
			return "INVALID_GATEWAY_COMMUNICATION_HISTORY_INITIATED_START_DATETIME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_GATEWAY_COMMUNICATION_HISTORY_INITIATED_END_DATETIME))
			return "INVALID_GATEWAY_COMMUNICATION_HISTORY_INITIATED_END_DATETIME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_GATEWAY_COMMUNICATION_HISTORY_INITIATED_DATETIME_RANGE))
			return "INVALID_GATEWAY_COMMUNICATION_HISTORY_INITIATED_DATETIME_RANGE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_GATEWAY_COMMUNICATION_HISTORY_REQUESTED_START_DATETIME))
			return "INVALID_GATEWAY_COMMUNICATION_HISTORY_REQUESTED_START_DATETIME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_GATEWAY_COMMUNICATION_HISTORY_REQUESTED_END_DATETIME))
			return "INVALID_GATEWAY_COMMUNICATION_HISTORY_REQUESTED_END_DATETIME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_GATEWAY_COMMUNICATION_HISTORY_REQUESTED_DATETIME_RANGE))
			return "INVALID_GATEWAY_COMMUNICATION_HISTORY_REQUESTED_DATETIME_RANGE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_GATEWAY_COMMUNICATION_HISTORY_STATUS_TYPE_ID))
			return "INVALID_GATEWAY_COMMUNICATION_HISTORY_STATUS_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_GATEWAY_COMMUNICATION_HISTORY_REQUEST_TYPE_ID))
			return "INVALID_GATEWAY_COMMUNICATION_HISTORY_REQUEST_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_GATEWAY_COMMUNICATION_TYPE_ID))
			return "INVALID_GATEWAY_COMMUNICATION_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.GATEWAY_INITIATED_COMMUNICATION_IN_PROGRESS))
			return "GATEWAY_INITIATED_COMMUNICATION_IN_PROGRESS";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ACTIVATION_DATETIME))
			return "INVALID_ACTIVATION_DATETIME";
		if (s.equals(Constants.ExternalServiceReturnCodes.GATEWAY_NOT_ENABLED))
			return "GATEWAY_NOT_ENABLED";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_IP_ADDRESS))
			return "INVALID_IP_ADDRESS";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_MODEM_TYPE))
			return "INVALID_MODEM_TYPE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_MODEM_INIT_STRING))
			return "INVALID_MODEM_INIT_STRING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SEARCH_CRITERIA))
			return "INVALID_SEARCH_CRITERIA";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ATTRIBUTE_MISSING))
			return "INVALID_ATTRIBUTE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_HIERARCHY_LEVEL_MEMBER_MISSING))
			return "INVALID_HIERARCHY_LEVEL_MEMBER_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DC1000_IP_ADDRESS))
			return "INVALID_DC1000_IP_ADDRESS";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_IP_ADDRESS_DUPLICATE))
			return "INVALID_IP_ADDRESS_DUPLICATE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_IP_ADDRESS_OUT_OF_RANGE))
			return "INVALID_IP_ADDRESS_OUT_OF_RANGE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_GATEWAY_TO_SERVER_PHONE_NUMBER_1))
			return "INVALID_GATEWAY_TO_SERVER_PHONE_NUMBER_1";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_GATEWAY_TO_SERVER_PHONE_NUMBER_2))
			return "INVALID_GATEWAY_TO_SERVER_PHONE_NUMBER_2";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ENABLE_TOTAL_ENERGY_VALUE))
			return "INVALID_ENABLE_TOTAL_ENERGY_VALUE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.NO_GATEWAY_IP_ADDRESS_AVAILABLE))
			return "NO_GATEWAY_IP_ADDRESS_AVAILABLE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_IP_ADDRESS_IN_CONFLICTING_COMMUNICATION_ROUTE))
			return "INVALID_IP_ADDRESS_IN_CONFLICTING_COMMUNICATION_ROUTE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_APPLICATION_LEVEL_AUTHENTICATION_VALUE))
			return "INVALID_APPLICATION_LEVEL_AUTHENTICATION_VALUE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SCHEDULE_NODE_MISSING))
			return "INVALID_SCHEDULE_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SCHEDULE_ID_NODE_MISSING))
			return "INVALID_SCHEDULE_ID_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_GATEWAY_DISCONNECT_COMMAND))
			return "INVALID_GATEWAY_DISCONNECT_COMMAND";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_GATEWAY_DISCONNECT_IN_PROGRESS))
			return "INVALID_GATEWAY_DISCONNECT_IN_PROGRESS";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.UNSUPPORTED_GATEWAY_COMMUNICATION_REQUEST_TYPE_ID))
			return "UNSUPPORTED_GATEWAY_COMMUNICATION_REQUEST_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_GATEWAY_SOFTWARE_VERSION_NOT_SUPPORTED))
			return "INVALID_GATEWAY_SOFTWARE_VERSION_NOT_SUPPORTED";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SECURITY_OPTIONS_NODE_MISSING))
			return "INVALID_SECURITY_OPTIONS_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_PPP_LOGIN_AND_PASSWORD_MISSING))
			return "INVALID_PPP_LOGIN_AND_PASSWORD_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_NEURON_ID_MISSING))
			return "INVALID_NEURON_ID_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_PPP_LOGIN_MISSING))
			return "INVALID_PPP_LOGIN_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_PPP_PASSWORD_MISSING))
			return "INVALID_PPP_PASSWORD_MISSING";
		if (s.equals(Constants.ExternalServiceReturnCodes.GATEWAY_DISABLED))
			return "GATEWAY_DISABLED";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_HIGH_PRIORITY_CONNECTION_REQUEST))
			return "INVALID_HIGH_PRIORITY_CONNECTION_REQUEST";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_STATUS_CHANGE))
			return "INVALID_STATUS_CHANGE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_UPDATE_DC_1000_FIRMWARE_COMMAND))
			return "INVALID_UPDATE_DC_1000_FIRMWARE_COMMAND";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SECONDARY_CONTROL_OUTPUT_RELAY_STATUS_TYPE_ID))
			return "INVALID_SECONDARY_CONTROL_OUTPUT_RELAY_STATUS_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_MAXIMUM_POWER_LEVEL_ENABLE_STATUS_ID))
			return "INVALID_MAXIMUM_POWER_LEVEL_ENABLE_STATUS_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ENABLE_MAXIMUM_POWER_NODE_MISSING))
			return "INVALID_ENABLE_MAXIMUM_POWER_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INITIAL_GATEWAY_COMMUNICATION_NOT_COMPLETE))
			return "INITIAL_GATEWAY_COMMUNICATION_NOT_COMPLETE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_LAST_GATEWAY_TO_DEVICE_COMMUNICATION_STATUS_TYPE_ID))
			return "INVALID_LAST_GATEWAY_TO_DEVICE_COMMUNICATION_STATUS_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATABASE_TYPE_ID))
			return "INVALID_DATABASE_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TRANSFORMER_ID))
			return "INVALID_TRANSFORMER_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_INSTALLATION_DATE_TIME))
			return "INVALID_INSTALLATION_DATE_TIME";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_NEURON_ID))
			return "INVALID_NEURON_ID";
		if (s.equals(Constants.ExternalServiceReturnCodes.DUPLICATE_NEURON_ID))
			return "DUPLICATE_NEURON_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.DUPLICATE_TRANSFORMER_ID))
			return "DUPLICATE_TRANSFORMER_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TEMPLATE_TYPE_ID))
			return "INVALID_TEMPLATE_TYPE_ID";
		if (s.equals(Constants.ExternalServiceReturnCodes.RESULTS_PENDING))
			return "RESULTS_PENDING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.CURRENTLY_COMMUNICATING))
			return "CURRENTLY_COMMUNICATING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RETRIEVE_BY_PARAMETER_ID_TYPE))
			return "INVALID_RETRIEVE_BY_PARAMETER_ID_TYPE";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_ID))
			return "INVALID_ID";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_ID_TYPE))
			return "INVALID_ID_TYPE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_START_INSTALLATION_DATETIME))
			return "INVALID_START_INSTALLATION_DATETIME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_END_INSTALLATION_DATETIME))
			return "INVALID_END_INSTALLATION_DATETIME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_INSTALLATION_DATETIME_RANGE))
			return "INVALID_INSTALLATION_DATETIME_RANGE";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_PHONE_NUMBER))
			return "INVALID_PHONE_NUMBER";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_PHONE_NUMBER_BLANK))
			return "INVALID_PHONE_NUMBER_BLANK";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_IP_ADDRESS_CONFLICTING_SPECIFICATION))
			return "INVALID_IP_ADDRESS_CONFLICTING_SPECIFICATION";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_SOAP_NAME))
			return "INVALID_SOAP_NAME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_FUNCTION_NAME))
			return "INVALID_FUNCTION_NAME";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_SOAP_URI))
			return "INVALID_SOAP_URI";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SOAP_NAMESPACE))
			return "INVALID_SOAP_NAMESPACE";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_EXPRESSION))
			return "INVALID_EXPRESSION";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DEFINITION_ARGUMENT))
			return "INVALID_DEFINITION_ARGUMENT";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SOAPCALL_NAME_BLANK))
			return "INVALID_SOAPCALL_NAME_BLANK";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SOAPCALL_PARAMETER_NAME_BLANK))
			return "INVALID_SOAPCALL_PARAMETER_NAME_BLANK";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_FUNCTIONCALL_NAME_BLANK))
			return "INVALID_FUNCTIONCALL_NAME_BLANK";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_FUNCTIONCALL_PARAMETER_NAME_BLANK))
			return "INVALID_FUNCTIONCALL_PARAMETER_NAME_BLANK";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_FUNCTIONCALL_ID))
			return "INVALID_FUNCTIONCALL_ID";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_SOAPCALL_ID))
			return "INVALID_SOAPCALL_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SOAPCALL_PARAMETER_NAME))
			return "INVALID_SOAPCALL_PARAMETER_NAME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_FUNCTIONCALL_PARAMETER_NAME))
			return "INVALID_FUNCTIONCALL_PARAMETER_NAME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.DUPLICATE_SOAPCALL_PARAMETER_NAME))
			return "DUPLICATE_SOAPCALL_PARAMETER_NAME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.DUPLICATE_FUNCTIONCALL_PARAMETER_NAME))
			return "DUPLICATE_FUNCTIONCALL_PARAMETER_NAME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.DUPLICATE_FUNCTIONCALL_NAME))
			return "DUPLICATE_FUNCTIONCALL_NAME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.DUPLICATE_SOAPCALL_NAME))
			return "DUPLICATE_SOAPCALL_NAME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SOAPCALL_PARAMETER_INDEX))
			return "INVALID_SOAPCALL_PARAMETER_INDEX";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SOAPCALL_PARAMETER_TYPE_ID))
			return "INVALID_SOAPCALL_PARAMETER_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_FUNCTIONCALL_PARAMETER_INDEX))
			return "INVALID_FUNCTIONCALL_PARAMETER_INDEX";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_FUNCTIONCALL_PARAMETER_TYPE_ID))
			return "INVALID_FUNCTIONCALL_PARAMETER_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SOAPCALL_PARAMETER_ID))
			return "INVALID_SOAPCALL_PARAMETER_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_FUNCTIONCALL_PARAMETER_ID))
			return "INVALID_FUNCTIONCALL_PARAMETER_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TIMEOUTINTERVAL_TYPE_ID))
			return "INVALID_TIMEOUTINTERVAL_TYPE_ID";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_SCHEDULE_ID))
			return "INVALID_SCHEDULE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TIMEOUTINTERVAL))
			return "INVALID_TIMEOUTINTERVAL";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SCHEDULE_INTERVAL))
			return "INVALID_SCHEDULE_INTERVAL";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SCHEDULE_MINUTE_INTERVAL))
			return "INVALID_SCHEDULE_MINUTE_INTERVAL";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TASK_PROCESSOR_TYPE_ID))
			return "INVALID_TASK_PROCESSOR_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SCHEDULE_STATUS_TYPEID))
			return "INVALID_SCHEDULE_STATUS_TYPEID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SCHEDULE_TYPE_ID))
			return "INVALID_SCHEDULE_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SCHEDULE_ALREADY_ASSIGNED))
			return "INVALID_SCHEDULE_ALREADY_ASSIGNED";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TIMEOUT_INTERVAL_MINUTE))
			return "INVALID_TIMEOUT_INTERVAL_MINUTE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SCHEDULE_NAME_BLANK))
			return "INVALID_SCHEDULE_NAME_BLANK";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SCHEDULE_NAME_DUPLICATE))
			return "INVALID_SCHEDULE_NAME_DUPLICATE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SCHEDULE_OCCURRENCE_LIST))
			return "INVALID_SCHEDULE_OCCURRENCE_LIST";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SCHEDULE_TYPE_ID_NODE_MISSING))
			return "INVALID_SCHEDULE_TYPE_ID_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATABASE_LOCATION_EMPTY))
			return "INVALID_DATABASE_LOCATION_EMPTY";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATABASE_NAME_EMPTY))
			return "INVALID_DATABASE_NAME_EMPTY";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATABASE_LOGIN_EMPTY))
			return "INVALID_DATABASE_LOGIN_EMPTY";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATABASE_LOGIN_WITHOUT_PASSWORD))
			return "INVALID_DATABASE_LOGIN_WITHOUT_PASSWORD";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.FAILED_SENDING_UPDATE_TO_GLOBAL_TASK_MANAGER))
			return "FAILED_SENDING_UPDATE_TO_GLOBAL_TASK_MANAGER";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.FAILED_DATABASE_CONNECTION))
			return "FAILED_DATABASE_CONNECTION";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.FAILED_GLOBAL_TASK_MANAGER_NOT_FOUND))
			return "FAILED_GLOBAL_TASK_MANAGER_NOT_FOUND";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_PORT))
			return "INVALID_PORT";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATABASE_TIMEOUT_SECONDS))
			return "INVALID_DATABASE_TIMEOUT_SECONDS";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_EMAIL_ADDRESS_FORMAT))
			return "INVALID_EMAIL_ADDRESS_FORMAT";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_MESSAGELOGDEFINITIONID))
			return "INVALID_MESSAGELOGDEFINITIONID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_MESSAGELOG_STATUSID))
			return "INVALID_MESSAGELOG_STATUSID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_MESSAGELOG_TYPE_ID))
			return "INVALID_MESSAGELOG_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_MESSAGELOG_USERID))
			return "INVALID_MESSAGELOG_USERID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_MESSAGELOG_TEXT))
			return "INVALID_MESSAGELOG_TEXT";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_MESSAGELOG_LOCATION))
			return "INVALID_MESSAGELOG_LOCATION";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_LOGTYPE_ENABLE))
			return "INVALID_LOGTYPE_ENABLE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_LOGTYPE_DISABLE))
			return "INVALID_LOGTYPE_DISABLE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_EMAIL_ADDRESS_MUST_BE_BLANK))
			return "INVALID_EMAIL_ADDRESS_MUST_BE_BLANK";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_MESSAGELOG_START_DATETIME))
			return "INVALID_MESSAGELOG_START_DATETIME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_MESSAGELOG_END_DATETIME))
			return "INVALID_MESSAGELOG_END_DATETIME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_MESSAGELOG_DATETIME_RANGE))
			return "INVALID_MESSAGELOG_DATETIME_RANGE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_MESSAGELOG_SOURCEIPADDRESS))
			return "INVALID_MESSAGELOG_SOURCEIPADDRESS";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SMTP_SERVER_LOCATION))
			return "INVALID_SMTP_SERVER_LOCATION";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATAPOINT_NAME_BLANK))
			return "INVALID_DATAPOINT_NAME_BLANK";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATAPOINT_NUMERICMIN_VALUE))
			return "INVALID_DATAPOINT_NUMERICMIN_VALUE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATAPOINT_NUMERICMAX_VALUE))
			return "INVALID_DATAPOINT_NUMERICMAX_VALUE";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_MAX_COUNT))
			return "INVALID_MAX_COUNT";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATAPOINT_NAME))
			return "INVALID_DATAPOINT_NAME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_UNIT_OF_MEASURE))
			return "INVALID_UNIT_OF_MEASURE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATAPOINT_NODE_MISSING))
			return "INVALID_DATAPOINT_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.DATAPOINT_NOT_ENABLED))
			return "DATAPOINT_NOT_ENABLED";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SORT_BY_TYPE_ID))
			return "INVALID_SORT_BY_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SORT_BY_ORDER_TYPE_ID))
			return "INVALID_SORT_BY_ORDER_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SORT_OPTION_NODE_MISSING))
			return "INVALID_SORT_OPTION_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SORT_BY_TYPE_ID_NODE_MISSING))
			return "INVALID_SORT_BY_TYPE_ID_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SORT_BY_ORDER_TYPE_ID_NODE_MISSING))
			return "INVALID_SORT_BY_ORDER_TYPE_ID_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATAPOINTVALUE_ACTUALDATETIME))
			return "INVALID_DATAPOINTVALUE_ACTUALDATETIME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATAPOINTVALUE_ACTUALDATETIME_NODE_MISSING))
			return "INVALID_DATAPOINTVALUE_ACTUALDATETIME_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATAPOINTVALUE_ACTUALTIMEZONEDATETIME))
			return "INVALID_DATAPOINTVALUE_ACTUALTIMEZONEDATETIME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATAPOINTVALUE_ACTUALTIMEZONEDATETIME_NODE_MISSING))
			return "INVALID_DATAPOINTVALUE_ACTUALTIMEZONEDATETIME_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATAPOINTVALUE_CALCULATIONEXECUTIONTIMESPAN))
			return "INVALID_DATAPOINTVALUE_CALCULATIONEXECUTIONTIMESPAN";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATAPOINTVALUE_DATAAVAILABLE))
			return "INVALID_DATAPOINTVALUE_DATAAVAILABLE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATAPOINTVALUE_DATAAVAILABLE_NODE_MISSING))
			return "INVALID_DATAPOINTVALUE_DATAAVAILABLE_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATAPOINT_ID_NODE_MISSING))
			return "INVALID_DATAPOINT_ID_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATAPOINTVALUE_EXPECTEDDATETIME))
			return "INVALID_DATAPOINTVALUE_EXPECTEDDATETIME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATAPOINTVALUE_EXPECTEDDATETIME_NODE_MISSING))
			return "INVALID_DATAPOINTVALUE_EXPECTEDDATETIME_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATAPOINTVALUE_EXPECTEDTIMEZONEDATETIME))
			return "INVALID_DATAPOINTVALUE_EXPECTEDTIMEZONEDATETIME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATAPOINTVALUE_EXPECTEDTIMEZONEDATETIME_NODE_MISSING))
			return "INVALID_DATAPOINTVALUE_EXPECTEDTIMEZONEDATETIME_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATAPOINTVALUE_MANUALLYENTERED_NODE_MISSING))
			return "INVALID_DATAPOINTVALUE_MANUALLYENTERED_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATAPOINTVALUE_MANUALLYENTERED))
			return "INVALID_DATAPOINTVALUE_MANUALLYENTERED";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATAPOINTVALUE_STATUSTYPEID_NODE_MISSING))
			return "INVALID_DATAPOINTVALUE_STATUSTYPEID_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATAPOINTVALUE_VALUE_TYPE_MISMATCH))
			return "INVALID_DATAPOINTVALUE_VALUE_TYPE_MISMATCH";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_HIERARCHY_LEVEL_MEMBER_RETRIEVAL_TYPE_ID))
			return "INVALID_HIERARCHY_LEVEL_MEMBER_RETRIEVAL_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DEVICE_TYPE_ID))
			return "INVALID_DEVICE_TYPE_ID";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_DEVICE_NAME))
			return "INVALID_DEVICE_NAME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_AUTHENTICATION_KEY))
			return "INVALID_AUTHENTICATION_KEY";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SERIAL_NUMBER))
			return "INVALID_SERIAL_NUMBER";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_PHASE_TYPE_ID))
			return "INVALID_PHASE_TYPE_ID";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_RESULT_ID))
			return "INVALID_RESULT_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RESULT_TYPE_ID))
			return "INVALID_RESULT_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_START_DATETIME))
			return "INVALID_START_DATETIME";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_END_DATETIME))
			return "INVALID_END_DATETIME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATETIME_RANGE))
			return "INVALID_DATETIME_RANGE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TIMEOUT_DATETIME))
			return "INVALID_TIMEOUT_DATETIME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.LOAD_PROFILE_IN_PROCESS))
			return "LOAD_PROFILE_IN_PROCESS";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.UNSUPPORTED_DEVICE_TYPE_ID))
			return "UNSUPPORTED_DEVICE_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_MAXIMUM_POWER_LEVEL))
			return "INVALID_MAXIMUM_POWER_LEVEL";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_MAXIMUM_POWER_LEVEL_DURATION))
			return "INVALID_MAXIMUM_POWER_LEVEL_DURATION";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ASSOCIATED_WITH_GATEWAY))
			return "INVALID_ASSOCIATED_WITH_GATEWAY";
		if (s.equals(Constants.ExternalServiceReturnCodes.DEVICE_NOT_ENABLED))
			return "DEVICE_NOT_ENABLED";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ENABLE_MAXIMUM_POWER))
			return "INVALID_ENABLE_MAXIMUM_POWER";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.ENABLE_MAXIMUM_POWER_IN_PROCESS))
			return "ENABLE_MAXIMUM_POWER_IN_PROCESS";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.PERFORM_SELF_BILLING_READ_IN_PROCESS))
			return "PERFORM_SELF_BILLING_READ_IN_PROCESS";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ENABLE_SECONDARY_CONTROL_OUTPUT_TIERS_VALUE))
			return "INVALID_ENABLE_SECONDARY_CONTROL_OUTPUT_TIERS_VALUE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SECONDARY_CONTROL_OUTPUT_TIER_VALUE))
			return "INVALID_SECONDARY_CONTROL_OUTPUT_TIER_VALUE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_LOAD_PROFILE_CHANNEL_SOURCE))
			return "INVALID_LOAD_PROFILE_CHANNEL_SOURCE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_LOAD_PROFILE_INTERVAL_PERIOD))
			return "INVALID_LOAD_PROFILE_INTERVAL_PERIOD";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DEVICE_ID_MISSING))
			return "INVALID_DEVICE_ID_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DEVICE_NODE_MISSING))
			return "INVALID_DEVICE_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_GATEWAY_ID_MISSING))
			return "INVALID_GATEWAY_ID_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_GATEWAY_MISSING))
			return "INVALID_GATEWAY_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_POWER_QUALITY_THRESHOLD))
			return "INVALID_POWER_QUALITY_THRESHOLD";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_PROGRAM_ID))
			return "INVALID_PROGRAM_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.UPDATE_METER_FIRMWARE_IN_PROGRESS))
			return "UPDATE_METER_FIRMWARE_IN_PROGRESS";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_FORCE_DELETE))
			return "INVALID_FORCE_DELETE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ALARM_TYPE_NODE_MISSING))
			return "INVALID_ALARM_TYPE_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SET_ALARM_DISPLAY_CONFIGURATION_COMMAND))
			return "INVALID_SET_ALARM_DISPLAY_CONFIGURATION_COMMAND";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_ALARM_INDEX))
			return "INVALID_ALARM_INDEX";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ALARM_DISPLAY_OPTION))
			return "INVALID_ALARM_DISPLAY_OPTION";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.DUPLICATE_ALARM_INDEX))
			return "DUPLICATE_ALARM_INDEX";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ENABLE_ALL_SEGMENTS_LIT))
			return "INVALID_ENABLE_ALL_SEGMENTS_LIT";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SECONDS_TO_DISPLAY))
			return "INVALID_SECONDS_TO_DISPLAY";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_METER_DISPLAY_CATEGORY_TYPE_ID))
			return "INVALID_METER_DISPLAY_CATEGORY_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_METER_DISPLAY_CATEGORY_INDEX))
			return "INVALID_METER_DISPLAY_CATEGORY_INDEX";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_METER_DISPLAY_SOURCE_CODE_ID))
			return "INVALID_METER_DISPLAY_SOURCE_CODE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_METER_DISPLAY_ID_TEXT))
			return "INVALID_METER_DISPLAY_ID_TEXT";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.DUPLICATE_METER_DISPLAY_ID_TEXT))
			return "DUPLICATE_METER_DISPLAY_ID_TEXT";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_FIELDS_AFTER_DECIMAL_POINT))
			return "INVALID_FIELDS_AFTER_DECIMAL_POINT";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_FIELDS_BEFORE_DECIMAL_POINT))
			return "INVALID_FIELDS_BEFORE_DECIMAL_POINT";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DECIMAL_POINT_FIELDS))
			return "INVALID_DECIMAL_POINT_FIELDS";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SUPPRESS_ZEROS))
			return "INVALID_SUPPRESS_ZEROS";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_NUMBER_OF_METER_DISPLAY_ITEMS))
			return "INVALID_NUMBER_OF_METER_DISPLAY_ITEMS";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SERIAL_NUMBER_NEURON_ID_MISMATCH))
			return "INVALID_SERIAL_NUMBER_NEURON_ID_MISMATCH";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_UPDATE_METER_FIRMWARE_COMMAND))
			return "INVALID_UPDATE_METER_FIRMWARE_COMMAND";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DISPLAY_ITEMS_NODE_MISSING))
			return "INVALID_DISPLAY_ITEMS_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DISPLAY_ITEM_NODE_MISSING))
			return "INVALID_DISPLAY_ITEM_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_METER_DISPLAY_SOURCE_NODE_MISSING))
			return "INVALID_METER_DISPLAY_SOURCE_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_METER_DISPLAY_ID_NODE_MISSING))
			return "INVALID_METER_DISPLAY_ID_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_METER_DISPLAY_VALUE_NODE_MISSING))
			return "INVALID_METER_DISPLAY_VALUE_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_METER_DISPLAY_CONFIGURATION_NODE_MISSING))
			return "INVALID_METER_DISPLAY_CONFIGURATION_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_CHANNEL_INDEX_NODE_MISSING))
			return "INVALID_CHANNEL_INDEX_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_PULSE_INPUT_CONFIGURATION_NODE_MISSING))
			return "INVALID_PULSE_INPUT_CONFIGURATION_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_CHANNEL_INDEX))
			return "INVALID_CHANNEL_INDEX";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_PULSE_INPUT_CHANNEL_STATUS))
			return "INVALID_PULSE_INPUT_CHANNEL_STATUS";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_PULSE_INPUT_IDLE_STATE_STATUS))
			return "INVALID_PULSE_INPUT_IDLE_STATE_STATUS";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_PULSE_INPUT_CHANNEL_STATUS_MISSING))
			return "INVALID_PULSE_INPUT_CHANNEL_STATUS_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_PULSE_INPUT_TAMPER_URGENT_ALARM_STATUS))
			return "INVALID_PULSE_INPUT_TAMPER_URGENT_ALARM_STATUS";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_PULSE_INPUT_REGULAR_ALARM_STATUS))
			return "INVALID_PULSE_INPUT_REGULAR_ALARM_STATUS";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_CHANNEL_MINIMUM_PULSE_WIDTH))
			return "INVALID_CHANNEL_MINIMUM_PULSE_WIDTH";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_INFORMATION_RETURN_TYPE_ID))
			return "INVALID_INFORMATION_RETURN_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SERVICE_STATUS_TYPE_ID))
			return "INVALID_SERVICE_STATUS_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_CALENDAR_ID_NODE_MISSING))
			return "INVALID_CALENDAR_ID_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TOU_RECURRING_DATES_NODE_MISSING))
			return "INVALID_TOU_RECURRING_DATES_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RECURRING_DATES_SCHEDULE_NODE_MISSING))
			return "INVALID_RECURRING_DATES_SCHEDULE_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RECURRING_DATES_INDEX_NODE_MISSING))
			return "INVALID_RECURRING_DATES_INDEX_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RECURRING_DATE_NODE_MISSING))
			return "INVALID_RECURRING_DATE_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.DUPLICATE_RECURRING_DATES_SCHEDULE_INDEX))
			return "DUPLICATE_RECURRING_DATES_SCHEDULE_INDEX";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RECURRING_DATE_MONTH_NODE_MISSING))
			return "INVALID_RECURRING_DATE_MONTH_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RECURRING_DATE_OFFSET_NODE_MISSING))
			return "INVALID_RECURRING_DATE_OFFSET_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RECURRING_DATE_WEEKDAY_NODE_MISSING))
			return "INVALID_RECURRING_DATE_WEEKDAY_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RECURRING_DATE_PERIOD_NODE_MISSING))
			return "INVALID_RECURRING_DATE_PERIOD_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RECURRING_DATE_DAY_NODE_MISSING))
			return "INVALID_RECURRING_DATE_DAY_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RECURRING_DATE_DELTA_NODE_MISSING))
			return "INVALID_RECURRING_DATE_DELTA_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RECURRING_DATE_ACTION_NODE_MISSING))
			return "INVALID_RECURRING_DATE_ACTION_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ACTIVATE_PENDING_TOU_CALENDAR_DATE_TIME_NODE_MISSING))
			return "INVALID_ACTIVATE_PENDING_TOU_CALENDAR_DATE_TIME_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RECURRING_DATE_PERFORM_BILLING_READ_NODE_MISSING))
			return "INVALID_RECURRING_DATE_PERFORM_BILLING_READ_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RECURRING_DATES_INDEX_MISSING))
			return "INVALID_RECURRING_DATES_INDEX_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RECURRING_DATES_DST_ACTION))
			return "INVALID_RECURRING_DATES_DST_ACTION";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RECURRING_DATES_SPECIAL_SCHEDULE_ACTION))
			return "INVALID_RECURRING_DATES_SPECIAL_SCHEDULE_ACTION";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RECURRING_DATES_SCHEDULE_INDEX))
			return "INVALID_RECURRING_DATES_SCHEDULE_INDEX";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RECURRING_DATES_MONTH))
			return "INVALID_RECURRING_DATES_MONTH";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RECURRING_DATES_OFFSET))
			return "INVALID_RECURRING_DATES_OFFSET";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RECURRING_DATES_WEEKDAY))
			return "INVALID_RECURRING_DATES_WEEKDAY";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RECURRING_DATES_DAY))
			return "INVALID_RECURRING_DATES_DAY";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RECURRING_DATES_PERIOD))
			return "INVALID_RECURRING_DATES_PERIOD";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RECURRING_DATES_DELTA))
			return "INVALID_RECURRING_DATES_DELTA";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.DUPLICATE_RECURRING_DATES_ACTION))
			return "DUPLICATE_RECURRING_DATES_ACTION";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RECURRING_DATES_ACTION))
			return "INVALID_RECURRING_DATES_ACTION";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RECURRING_DATES_MONTH_ORDER))
			return "INVALID_RECURRING_DATES_MONTH_ORDER";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RECURRING_DATES_PERFORM_BILLING_READ))
			return "INVALID_RECURRING_DATES_PERFORM_BILLING_READ";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ACTIVATE_PENDING_TOU_CALENDAR_DATE_TIME))
			return "INVALID_ACTIVATE_PENDING_TOU_CALENDAR_DATE_TIME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DAY_SCHEDULES_NODE_MISSING))
			return "INVALID_DAY_SCHEDULES_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DAY_SCHEDULES_SCHEDULE_NODE_MISSING))
			return "INVALID_DAY_SCHEDULES_SCHEDULE_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DAY_SCHEDULE_INDEX_NODE_MISSING))
			return "INVALID_DAY_SCHEDULE_INDEX_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DAY_SCHEDULE_SWITCH_NODE_MISSING))
			return "INVALID_DAY_SCHEDULE_SWITCH_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DAY_SCHEDULE_SWITCH_INDEX_NODE_MISSING))
			return "INVALID_DAY_SCHEDULE_SWITCH_INDEX_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DAY_SCHEDULE_TIER_NODE_MISSING))
			return "INVALID_DAY_SCHEDULE_TIER_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DAY_SCHEDULE_START_HOUR_NODE_MISSING))
			return "INVALID_DAY_SCHEDULE_START_HOUR_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DAY_SCHEDULE_START_MINUTE_NODE_MISSING))
			return "INVALID_DAY_SCHEDULE_START_MINUTE_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.DUPLICATE_DAY_SCHEDULE_SWITCH_INDEX))
			return "DUPLICATE_DAY_SCHEDULE_SWITCH_INDEX";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_CALENDAR_ID))
			return "INVALID_CALENDAR_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.DUPLICATE_DAY_SCHEDULE_INDEX))
			return "DUPLICATE_DAY_SCHEDULE_INDEX";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DAY_SCHEDULE_INDEX))
			return "INVALID_DAY_SCHEDULE_INDEX";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DAY_SCHEDULE_SWITCH_INDEX))
			return "INVALID_DAY_SCHEDULE_SWITCH_INDEX";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DAY_SCHEDULE_START_HOUR_START_MINUTE))
			return "INVALID_DAY_SCHEDULE_START_HOUR_START_MINUTE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DAY_SCHEDULE_START_HOUR_START_MINUTE_NON_ZERO))
			return "INVALID_DAY_SCHEDULE_START_HOUR_START_MINUTE_NON_ZERO";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DAY_SCHEDULE_TIER))
			return "INVALID_DAY_SCHEDULE_TIER";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DAY_SCHEDULE_START_HOUR))
			return "INVALID_DAY_SCHEDULE_START_HOUR";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DAY_SCHEDULE_START_MINUTE))
			return "INVALID_DAY_SCHEDULE_START_MINUTE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SEASON_SCHEDULE_NODE_MISSING))
			return "INVALID_SEASON_SCHEDULE_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SEASON_SCHEDULE_SCHEDULE_NODE_MISSING))
			return "INVALID_SEASON_SCHEDULE_SCHEDULE_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SEASON_SCHEDULE_INDEX_NODE_MISSING))
			return "INVALID_SEASON_SCHEDULE_INDEX_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SEASON_SCHEDULE_SATURDAY_NODE_MISSING))
			return "INVALID_SEASON_SCHEDULE_SATURDAY_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SEASON_SCHEDULE_SUNDAY_NODE_MISSING))
			return "INVALID_SEASON_SCHEDULE_SUNDAY_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SEASON_SCHEDULE_WEEKDAY_NODE_MISSING))
			return "INVALID_SEASON_SCHEDULE_WEEKDAY_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SEASON_SCHEDULE_SPECIAL_0_NODE_MISSING))
			return "INVALID_SEASON_SCHEDULE_SPECIAL_0_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SEASON_SCHEDULE_SPECIAL_1_NODE_MISSING))
			return "INVALID_SEASON_SCHEDULE_SPECIAL_1_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SEASON_SCHEDULE_INDEX))
			return "INVALID_SEASON_SCHEDULE_INDEX";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SEASON_SCHEDULE_SATURDAY))
			return "INVALID_SEASON_SCHEDULE_SATURDAY";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SEASON_SCHEDULE_SUNDAY))
			return "INVALID_SEASON_SCHEDULE_SUNDAY";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SEASON_SCHEDULE_WEEKDAY))
			return "INVALID_SEASON_SCHEDULE_WEEKDAY";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SEASON_SCHEDULE_SPECIAL_0))
			return "INVALID_SEASON_SCHEDULE_SPECIAL_0";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SEASON_SCHEDULE_SPECIAL_1))
			return "INVALID_SEASON_SCHEDULE_SPECIAL_1";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_EXPRESSION_DATA_POINT_PARAMETER))
			return "INVALID_EXPRESSION_DATA_POINT_PARAMETER";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_EXPRESSION_LENGTH))
			return "INVALID_EXPRESSION_LENGTH";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_FUNCTION_CALL_EXPRESSION_SYNTAX))
			return "INVALID_FUNCTION_CALL_EXPRESSION_SYNTAX";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SOAP_CALL_EXPRESSION_SYNTAX))
			return "INVALID_SOAP_CALL_EXPRESSION_SYNTAX";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_EXPRESSION_DATA_POINT_PARAMETER_NO_RESTRICTIONS))
			return "INVALID_EXPRESSION_DATA_POINT_PARAMETER_NO_RESTRICTIONS";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_SYNTAX))
			return "INVALID_SYNTAX";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_EXPRESSION_CALL_PARAMETER))
			return "INVALID_EXPRESSION_CALL_PARAMETER";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATETIME_SYNTAX))
			return "INVALID_DATETIME_SYNTAX";
		if (s.equals(Constants.ExternalServiceReturnCodes.TYPE_MISMATCH))
			return "TYPE_MISMATCH";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SETTING_NOT_NUMERIC))
			return "INVALID_SETTING_NOT_NUMERIC";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SETTING_OUT_OF_RANGE))
			return "INVALID_SETTING_OUT_OF_RANGE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SETTING_EMAIL))
			return "INVALID_SETTING_EMAIL";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SETTING_VALUE_LENGTH))
			return "INVALID_SETTING_VALUE_LENGTH";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_SETTING_ID))
			return "INVALID_SETTING_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SETTING_TYPE_ID))
			return "INVALID_SETTING_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SETTING_VALUE))
			return "INVALID_SETTING_VALUE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SOLUTION_SETTING_VALUE_TYPE_ID))
			return "INVALID_SOLUTION_SETTING_VALUE_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.UNSUPPORTED_SOLUTION_SETTING_VALUE_TYPE_ID))
			return "UNSUPPORTED_SOLUTION_SETTING_VALUE_TYPE_ID";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_TABLE_NAME))
			return "INVALID_TABLE_NAME";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_COLUMN_NAME))
			return "INVALID_COLUMN_NAME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_EXPIRED_INTERVAL_TYPE_ID))
			return "INVALID_EXPIRED_INTERVAL_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_EXPIRED_INTERVAL))
			return "INVALID_EXPIRED_INTERVAL";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SERVER_HOSTNAME))
			return "INVALID_SERVER_HOSTNAME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SERVER_HOSTNAME_DUPLICATE))
			return "INVALID_SERVER_HOSTNAME_DUPLICATE";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_NETMASK))
			return "INVALID_NETMASK";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.UNSUPPORTED_SETTING_TYPE_ID))
			return "UNSUPPORTED_SETTING_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.COMMUNICATION_SETTING_ROUTE_NOT_AVAILABLE))
			return "COMMUNICATION_SETTING_ROUTE_NOT_AVAILABLE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.COMMUNICATION_SETTING_ROUTE_NOT_SET_UP))
			return "COMMUNICATION_SETTING_ROUTE_NOT_SET_UP";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.COMMUNICATION_SETTING_NOT_SET_UP))
			return "COMMUNICATION_SETTING_NOT_SET_UP";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.COMMUNICATION_SETTING_IN_USE))
			return "COMMUNICATION_SETTING_IN_USE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_COMMUNICATION_SETTING))
			return "INVALID_COMMUNICATION_SETTING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_COMMUNICATION_SETTING_DUPLICATE))
			return "INVALID_COMMUNICATION_SETTING_DUPLICATE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_GATEWAY_COMMUNICATION_ROUTE_SETTING_ID))
			return "INVALID_GATEWAY_COMMUNICATION_ROUTE_SETTING_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_IP_ADDRESS_RANGE_NOT_AVAILABLE))
			return "INVALID_IP_ADDRESS_RANGE_NOT_AVAILABLE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_IP_ADDRESS_RANGE_OVERLAP))
			return "INVALID_IP_ADDRESS_RANGE_OVERLAP";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_OUTBOUND_CONNECTIONS))
			return "INVALID_OUTBOUND_CONNECTIONS";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_PHONE_NUMBER_DUPLICATE))
			return "INVALID_PHONE_NUMBER_DUPLICATE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.GATEWAY_IN_IP_ADDRESS_RANGE))
			return "GATEWAY_IN_IP_ADDRESS_RANGE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SCHEDULE_ASSIGNMENT_ID))
			return "INVALID_SCHEDULE_ASSIGNMENT_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SCHEDULE_NULL))
			return "INVALID_SCHEDULE_NULL";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RETRIEVE_ENCRYPTED_VALUE))
			return "INVALID_RETRIEVE_ENCRYPTED_VALUE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_COMMUNICATION_SETTING_TYPE))
			return "INVALID_COMMUNICATION_SETTING_TYPE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SETTING_NODE_MISSING))
			return "INVALID_SETTING_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SETTING_ID_NODE_MISSING))
			return "INVALID_SETTING_ID_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_COMM_SETTING_GATEWAY_COMM_TYPE_MISMATCH))
			return "INVALID_COMM_SETTING_GATEWAY_COMM_TYPE_MISMATCH";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_SETTING_VALUE_RANGE))
			return "INVALID_SETTING_VALUE_RANGE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.CATEGORY_NODE_NOT_FOUND))
			return "CATEGORY_NODE_NOT_FOUND";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_FIRMWARE_IMAGE))
			return "INVALID_FIRMWARE_IMAGE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_VERSION_NUMBER))
			return "INVALID_VERSION_NUMBER";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ENTITY_TYPE_ID))
			return "INVALID_ENTITY_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ENTITY_TYPE_TYPE_ID))
			return "INVALID_ENTITY_TYPE_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_VERSION_NUMBER_DUPLICATE))
			return "INVALID_VERSION_NUMBER_DUPLICATE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_BUILD_DATETIME))
			return "INVALID_BUILD_DATETIME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_FIRMWARE_VERSION_ID))
			return "INVALID_FIRMWARE_VERSION_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_FIRMWARE_VERSION_NUMBER_NOT_DEFINED))
			return "INVALID_FIRMWARE_VERSION_NUMBER_NOT_DEFINED";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TASK_PROCESSOR_PORT))
			return "INVALID_TASK_PROCESSOR_PORT";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TASK_PROCESSOR_PORT_DUPLICATE))
			return "INVALID_TASK_PROCESSOR_PORT_DUPLICATE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_HEARTBEAT_INTERVAL))
			return "INVALID_HEARTBEAT_INTERVAL";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TASK_PROCESSOR_PATH_MISSING))
			return "INVALID_TASK_PROCESSOR_PATH_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TASK_PROCESSOR_PATH))
			return "INVALID_TASK_PROCESSOR_PATH";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_LOCAL_TASK_MANAGER_ID))
			return "INVALID_LOCAL_TASK_MANAGER_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_LOCAL_TASK_MANAGER_ID_MISSING))
			return "INVALID_LOCAL_TASK_MANAGER_ID_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ENGINE_RECEIVER_PORT))
			return "INVALID_ENGINE_RECEIVER_PORT";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ENGINE_RECEIVER_PORT_DUPLICATE))
			return "INVALID_ENGINE_RECEIVER_PORT_DUPLICATE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TASK_PROCESSOR_ID))
			return "INVALID_TASK_PROCESSOR_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DELETE_TASK_PROCESSOR_RUNNING))
			return "INVALID_DELETE_TASK_PROCESSOR_RUNNING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TASK_PROCESSOR_COMMAND_ID))
			return "INVALID_TASK_PROCESSOR_COMMAND_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TASK_PROCESSOR_COMMAND_ID_MISSING))
			return "INVALID_TASK_PROCESSOR_COMMAND_ID_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TASK_PROCESSOR_COMMAND))
			return "INVALID_TASK_PROCESSOR_COMMAND";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TASK_PROCESSOR_ID_MISSING))
			return "INVALID_TASK_PROCESSOR_ID_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TASK_PROCESSOR_STOP_COMMAND))
			return "INVALID_TASK_PROCESSOR_STOP_COMMAND";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TASK_PROCESSOR_START_COMMAND))
			return "INVALID_TASK_PROCESSOR_START_COMMAND";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_COMMAND_HISTORY_ID))
			return "INVALID_COMMAND_HISTORY_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ROUTING_ENTITY_TYPE_ID))
			return "INVALID_ROUTING_ENTITY_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_START_REQUEST_DATETIME))
			return "INVALID_START_REQUEST_DATETIME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_END_REQUEST_DATETIME))
			return "INVALID_END_REQUEST_DATETIME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_REQUEST_DATETIME_RANGE))
			return "INVALID_REQUEST_DATETIME_RANGE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_START_COMPLETE_DATETIME))
			return "INVALID_START_COMPLETE_DATETIME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_END_COMPLETE_DATETIME))
			return "INVALID_END_COMPLETE_DATETIME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_COMPLETE_DATETIME_RANGE))
			return "INVALID_COMPLETE_DATETIME_RANGE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TASK_PROCESSOR_TYPE_ID_DUPLICATE))
			return "INVALID_TASK_PROCESSOR_TYPE_ID_DUPLICATE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TASK_PROCESSOR_NAME_DUPLICATE))
			return "INVALID_TASK_PROCESSOR_NAME_DUPLICATE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TASK_PROCESSOR_NAME_BLANK))
			return "INVALID_TASK_PROCESSOR_NAME_BLANK";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_MAX_CONCURRENT_TASKS))
			return "INVALID_MAX_CONCURRENT_TASKS";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_COMMAND_HISTORY_TYPE))
			return "INVALID_COMMAND_HISTORY_TYPE";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_COMMAND_ID))
			return "INVALID_COMMAND_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_COMMAND_NODE_MISSING))
			return "INVALID_COMMAND_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_COMMAND_ID_NODE_MISSING))
			return "INVALID_COMMAND_ID_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ROUTING_ENTITY_NODE_MISSING))
			return "INVALID_ROUTING_ENTITY_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_STATUS_TYPE_NODE_MISSING))
			return "INVALID_STATUS_TYPE_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ROUTING_ENTITY_TYPE_NODE_MISSING))
			return "INVALID_ROUTING_ENTITY_TYPE_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ROUTING_ENTITY_ID_NODE_MISSING))
			return "INVALID_ROUTING_ENTITY_ID_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_STATUS_TYPE_ID_NODE_MISSING))
			return "INVALID_STATUS_TYPE_ID_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RESULT_TYPE_NODE_MISSING))
			return "INVALID_RESULT_TYPE_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RESULT_TYPE_ID_NODE_MISSING))
			return "INVALID_RESULT_TYPE_ID_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_COMMAND_HISTORY_NODE_MISSING))
			return "INVALID_COMMAND_HISTORY_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_COMMAND_HISTORY_ID_NODE_MISSING))
			return "INVALID_COMMAND_HISTORY_ID_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_EVENT_DEFINITION_DELIVERY_TYPE_ID))
			return "INVALID_EVENT_DEFINITION_DELIVERY_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_EVENT_DEFINITION_ID))
			return "INVALID_EVENT_DEFINITION_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_EVENT_DEFINITION_NAME_BLANK))
			return "INVALID_EVENT_DEFINITION_NAME_BLANK";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_EVENT_DEFINITION_NAME_DUPLICATE))
			return "INVALID_EVENT_DEFINITION_NAME_DUPLICATE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_EVENT_DEFINITION_PRIORITY))
			return "INVALID_EVENT_DEFINITION_PRIORITY";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_EVENT_DEFINITION_STATUS_TYPE_ID))
			return "INVALID_EVENT_DEFINITION_STATUS_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_EVENT_HISTORY_DATETIME_RANGE))
			return "INVALID_EVENT_HISTORY_DATETIME_RANGE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_EVENT_HISTORY_END_DATETIME))
			return "INVALID_EVENT_HISTORY_END_DATETIME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_EVENT_HISTORY_ID))
			return "INVALID_EVENT_HISTORY_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_EVENT_HISTORY_START_DATETIME))
			return "INVALID_EVENT_HISTORY_START_DATETIME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_HISTORY_NODE_MISSING))
			return "INVALID_HISTORY_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_HISTORY_ID_NODE_MISSING))
			return "INVALID_HISTORY_ID_NODE_MISSING";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_TASK_ID))
			return "INVALID_TASK_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TASK_PRIORITY))
			return "INVALID_TASK_PRIORITY";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TASK_STATUS_TYPE_ID))
			return "INVALID_TASK_STATUS_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TASK_TIMEOUT_DATETIME))
			return "INVALID_TASK_TIMEOUT_DATETIME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TASK_TO_DELETE))
			return "INVALID_TASK_TO_DELETE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TASK_TO_REQUEUE))
			return "INVALID_TASK_TO_REQUEUE";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_TASK_TYPE_ID))
			return "INVALID_TASK_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_CREATION_DATETIME_RANGE))
			return "INVALID_CREATION_DATETIME_RANGE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_CREATION_END_DATETIME))
			return "INVALID_CREATION_END_DATETIME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_CREATION_START_DATETIME))
			return "INVALID_CREATION_START_DATETIME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_EXECUTION_DATETIME_RANGE))
			return "INVALID_EXECUTION_DATETIME_RANGE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_EXECUTION_END_DATETIME))
			return "INVALID_EXECUTION_END_DATETIME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_EXECUTION_START_DATETIME))
			return "INVALID_EXECUTION_START_DATETIME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TASK_TYPE_ID_NODE_MISSING))
			return "INVALID_TASK_TYPE_ID_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ENTITY_NODE_MISSING))
			return "INVALID_ENTITY_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ENTITY_TYPE_ID_NODE_MISSING))
			return "INVALID_ENTITY_TYPE_ID_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ENTITY_ID_NODE_MISSING))
			return "INVALID_ENTITY_ID_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_PARAMETER_NULL))
			return "INVALID_PARAMETER_NULL";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_GATEWAY_NAME_NODE_MISSING))
			return "INVALID_GATEWAY_NAME_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DATA_POINT_NAME_NODE_MISSING))
			return "INVALID_DATA_POINT_NAME_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DEVICE_NAME_NODE_MISSING))
			return "INVALID_DEVICE_NAME_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_NAME_SEARCH_TYPE_ID))
			return "INVALID_NAME_SEARCH_TYPE_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RESULT_NODE_MISSING))
			return "INVALID_RESULT_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_RESULT_ID_NODE_MISSING))
			return "INVALID_RESULT_ID_NODE_MISSING";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_EXECUTION_DATETIME))
			return "INVALID_EXECUTION_DATETIME";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_DEVICE_MISSING))
			return "INVALID_DEVICE_MISSING";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_RETURN_CODE))
			return "INVALID_RETURN_CODE";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TOU_RECURRING_DATE_MONTH))
			return "INVALID_TOU_RECURRING_DATE_MONTH";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TOU_RECURRING_DATE_OFFSET))
			return "INVALID_TOU_RECURRING_DATE_OFFSET";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TOU_RECURRING_DATE_DAY))
			return "INVALID_TOU_RECURRING_DATE_DAY";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TOU_RECURRING_DATE_WEEKDAY))
			return "INVALID_TOU_RECURRING_DATE_WEEKDAY";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TOU_RECURRING_DATE_PERIOD))
			return "INVALID_TOU_RECURRING_DATE_PERIOD";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TOU_RECURRING_DATE_DELTA))
			return "INVALID_TOU_RECURRING_DATE_DELTA";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_TOU_ACTION))
			return "INVALID_TOU_ACTION";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TOU_PERFORM_BILLING_READ))
			return "INVALID_TOU_PERFORM_BILLING_READ";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TOU_SCHEDULE_INDEX))
			return "INVALID_TOU_SCHEDULE_INDEX";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TOU_SWITCH_INDEX))
			return "INVALID_TOU_SWITCH_INDEX";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_TOU_TIER))
			return "INVALID_TOU_TIER";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TOU_START_HOUR))
			return "INVALID_TOU_START_HOUR";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_TOU_START_MINUTE))
			return "INVALID_TOU_START_MINUTE";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_TOU_SATURDAY))
			return "INVALID_TOU_SATURDAY";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_TOU_SUNDAY))
			return "INVALID_TOU_SUNDAY";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_TOU_SPECIAL0))
			return "INVALID_TOU_SPECIAL0";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_TOU_SPECIAL1))
			return "INVALID_TOU_SPECIAL1";
		if (s.equals(Constants.ExternalServiceReturnCodes.INVALID_TOU_WEEKDAY))
			return "INVALID_TOU_WEEKDAY";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.DUPLICATE_ILON100_NEURON_ID))
			return "DUPLICATE_ILON100_NEURON_ID";
		if (s
				.equals(Constants.ExternalServiceReturnCodes.INVALID_ILON100_NEURON_ID))
			return "INVALID_ILON100_NEURON_ID";

		if (s.equals(Constants.CommandHistoryStatus.SUCCESS))
			return "SUCCESS";
		if (s.equals(Constants.CommandHistoryStatus.FAILURE))
			return "FAILURE";
		if (s.equals(Constants.CommandHistoryStatus.WAITING))
			return "WAITING";
		if (s.equals(Constants.CommandHistoryStatus.DELETED))
			return "DELETED";
		if (s.equals(Constants.CommandHistoryStatus.IN_PROGRESS))
			return "IN_PROGRESS";

		return null;
	}

}
