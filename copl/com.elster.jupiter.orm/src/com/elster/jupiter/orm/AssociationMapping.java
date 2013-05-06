package com.elster.jupiter.orm;

public final class AssociationMapping {
	private final String fieldName;
	private final String reverseFieldName;
	private final String reverseOrderFieldName;
	private final String reverseCurrentFieldName;
	
	public AssociationMapping(String fieldName , String reverseFieldName , String reverseOrderFieldName, String reverseCurrentFieldName) {
		this.fieldName = fieldName;
		this.reverseFieldName = reverseFieldName;
		this.reverseOrderFieldName = reverseOrderFieldName;
		this.reverseCurrentFieldName = reverseCurrentFieldName;
	}

	public AssociationMapping(String fieldName , String reverseFieldName , String reverseOrderFieldName) {
		this(fieldName,reverseFieldName,reverseOrderFieldName,null);
	}
	
	public AssociationMapping(String fieldName, String reverseFieldName) {
		this(fieldName,reverseFieldName,null);
	}
	
	public AssociationMapping(String fieldName) {
		this(fieldName,null);		
	}
	
	public String getFieldName() {
		return fieldName;
	}

	public String getReverseFieldName() {
		return reverseFieldName;
	}

	public String getReverseOrderFieldName() {
		return reverseOrderFieldName;
	}

	public String getReverseCurrentFieldName() {
		return reverseCurrentFieldName;
	}
}
