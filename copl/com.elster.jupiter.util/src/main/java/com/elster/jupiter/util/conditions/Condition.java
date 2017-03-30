/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.conditions;


public interface Condition {

    /**
     * Condition that evaluates to true.
     * Useful as a starting pointing for building a composite condition anding 0 to n subconditions 
     *  Condition condition = Condition.TRUE;
     *  for ( Condition each : getSubConditions()) {
     *  	condition = condition.and(each);
     *  }
     *  return condition;
     */
    Condition TRUE = Constant.TRUE;

    /**
     * Condition that evaluates to false.
     * Useful as a starting pointing for building a composite condition oring 0 to n subconditions,
     *  Condition condition = Condition.FALSE;
     *  for ( Condition each : getSubConditions()) {
     *  	condition = condition.or(each);
     *  }
     *  return condition;
     */
    Condition FALSE = Constant.FALSE;
    
    /**
     * @param condition
     * @return a Condition that evaluates to the logical conjunction of this and the given Condition
     */
    Condition and(Condition condition);

    /**
     * @param condition
     * @return a Condition that evaluates to the logical disjunction of this and the given Condition
     */
    Condition or(Condition condition);

    /**
     * @return a condition that evaluates to the logical negation of this Condition.
     */
    Condition not();

    /**
     * Accept the given visitor.
     * @param visitor
     */
    void visit(Visitor visitor);
    
    /*
     * 
     * 
     */
    boolean implies(Condition condition);
  
}
