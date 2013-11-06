package com.elster.jupiter.metering.impl;

public interface OperationVisitor {

    void visitBooleanOperation(BooleanOperation booleanOperation);

    void visitSimpleCondition(SimpleConditionOperation simpleConditionOperation);

    void visitCloseBracketOperation(CloseBracketOperation bracketOperation);

    void visitOpenBracketOperation(OpenBracketOperation bracketOperation);
}
