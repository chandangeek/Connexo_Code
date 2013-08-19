package com.elster.jupiter.util.conditions;


public interface Condition {

    /**
     * Condition that evaluates to true.
     */
    Condition TRUE = Constant.TRUE;

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
}
