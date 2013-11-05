package com.elster.jupiter.metering.impl;

public interface OperationVisitor {

    void visitBracketOperation(BracketOperation bracketOperation);

    void visitBooleanOperation(BooleanOperation booleanOperation);

    void visitSimpleCondition(SimpleConditionOperation simpleConditionOperation);
}
