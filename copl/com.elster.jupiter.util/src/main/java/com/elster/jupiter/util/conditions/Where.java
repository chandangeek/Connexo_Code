package com.elster.jupiter.util.conditions;

import java.util.Date;
import java.util.List;

import com.elster.jupiter.util.time.Interval;

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
	
	public Condition isGreaterThan(Object value) {
		return Operator.GREATERTHAN.compare(field,value);
	}
	
	public Condition isGreaterThanOrEqual(Object value) {
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
	
	public Condition likeIgnoreCase(Object value) {
		return Operator.LIKEIGNORECASE.compare(field,value);
	}
	
	public Condition isNotEqual(Object value) {
		return Operator.NOTEQUAL.compare(field,value);
	}
	
	public Condition isNotEqualAndNotBothNull(Object value) {
		return Operator.NOTEQUALANDNOTBOTHNULL.compare(field,value);
	}
	
	public Condition matches(Object value, String options) {
		return Operator.REGEXP_LIKE.compare(field,value,options);
	}
	
	public Condition soundsAs(Object value) {
		return Operator.SOUNDSAS.compare(field, value);
	}
	
	private Condition compare(Date date , Operator operator) {
		return date == null ? Condition.TRUE : operator.compare(field,date);
	}
	private Condition after (Date date) {
		return compare(date,Operator.GREATERTHAN);
	}
	
	private Condition afterOrEqual(Date date) {
		return compare(date,Operator.GREATERTHANOREQUAL);
	}
	
	private Condition before(Date date) {
		return compare(date,Operator.LESSTHAN);
	}
	
	private Condition beforeOrEqual(Date date) {
		return compare(date,Operator.LESSTHANOREQUAL);
	}
	
	public Condition inOpen(Interval interval) {
		return after(interval.getStart()).and(before(interval.getEnd()));
	}
	
	public Condition inClosed(Interval interval) {
		return afterOrEqual(interval.getStart()).and(beforeOrEqual(interval.getEnd()));
	}
	
	public Condition inOpenClosed(Interval interval) {
		return after(interval.getStart()).and(beforeOrEqual(interval.getEnd()));
	}
	
	public Condition inClosedOpen(Interval interval) {
		return afterOrEqual(interval.getStart()).and(before(interval.getEnd()));
	}
	
	public Condition isEffective() {
		return new Effective(field);
	}
 	public Condition isEffective(Date date) {
		return where(field + ".start").isLessThanOrEqual(date.getTime()).and(where(field + ".end").isGreaterThan(date.getTime()));
	}
	
	public Condition isEffective(Interval interval) {
		return where(field + ".start").isLessThan(interval.dbEnd()).and(where(field + ".end").isGreaterThan(interval.dbStart()));
	}
	
	public Condition in(List<?> values) {
		return ListOperator.IN.contains(field, values);
	}
	
	@Deprecated
	@Override
	public boolean equals(Object other) {
		return super.equals(other);
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

