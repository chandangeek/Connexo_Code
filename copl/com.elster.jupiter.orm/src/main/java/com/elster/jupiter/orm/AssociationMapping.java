package com.elster.jupiter.orm;

/**
 * 
 * Describes the various mapping of a foreign key to instance fields
 * 
 * Terminology:
 *   Child object: object matching tuple in referencing table. That is the table that has the foreign key constraint
 *   Parent object: object matching tuple in referenced table.
 * 
 * fieldName: The name of the field in the child object that contains the reference to the parent object
 * reverseFieldName: Name of the field in the parent object that contains the reference to the child objects. This must be of type java.util.List
 * reverseOrderFieldName: Name of the field in the child object used to order the list in the parent object
 * reverseCurrentFieldName: Name of the field in the parent object that holds the current instance of the child object. Only
 * applicable if the child has interval columns (STARTTIME and ENDTIME), and if a parent can have only one child at a given time.   
 *
 */
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
