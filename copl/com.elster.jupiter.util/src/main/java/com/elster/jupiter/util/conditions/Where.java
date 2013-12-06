package com.elster.jupiter.util.conditions;

public final class Where {
	
	private final String field;
	
	private Where(String field) {
		this.field = field;
	}
	
	public static Where where(String field) {
		return new Where(field);
	}

	public BetweenBuilder between(Object value ) {
		return new BetweenBuilder(value);
	}
	
	public Condition isEqualTo(Object value) {
		return Operator.EQUAL.compare(field,value);
	}
	
	public Condition isEqualToIgnoreCase(Object value) {
		return Operator.EQUALIGNORECASE.compare(field,value);
	}
	
	public Condition isEqualOrBothNull(Object value) {
		return Operator.EQUALORBOTHNULL.compare(field,value);
	}
	
	public Condition isGreatherThan(Object value) {
		return Operator.GREATERTHAN.compare(field,value);
	}
	
	public Condition isGreatherThanOrEqual(Object value) {
		return Operator.GREATERTHANOREQUAL.compare(field,value);
	}
	
	public Condition isNotNull() {
		return Operator.ISNOTNULL.compare(field);
	}
	
	public Condition isNull() {
		return Operator.ISNULL.compare(field);
	}
	
	public Condition isLessThan(Object value) {
		return Operator.LESSTHAN.compare(field,value);
	}
	
	public Condition isLessThanOrEqual(Object value) {
		return Operator.LESSTHANOREQUAL.compare(field, value);
	}
	
	public Condition like(Object value) {
		return Operator.LIKE.compare(field,value);
	}
	
	public Condition isNotEqual(Object value) {
		return Operator.NOTEQUAL.compare(field,value);
	}
	
	public Condition isNotEqualAndNotBothNull(Object value) {
		return Operator.NOTEQUALANDNOTBOTHNULL.compare(field,value);
	}
	
	public Condition matches(Object value) {
		return Operator.REGEXP_LIKE.compare(field,value);
	}
	
	public Condition soundsAs(Object value) {
		return Operator.SOUNDSAS.compare(field, value);
	}
	
	public class BetweenBuilder {
	
		private final Object lowerValue;
		
		private BetweenBuilder(Object value) {
			this.lowerValue = value;
		}
		
		public Condition and(Object value) {
			return Operator.BETWEEN.compare(field,lowerValue,value);
		}
	}
}

