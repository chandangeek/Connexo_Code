/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import com.elster.jupiter.util.units.Dimension;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

/**
 * Models a node of expression that comply with the BNF below.
 * <pre><code>
 * &lt;expression&gt;         ::= &lt;term&gt; | &lt;expression&gt; &lt;term-operator&gt; &lt;term&gt;
 * &lt;term-operator&gt;      ::= + | -
 * &lt;term&gt;               ::= &lt;factor&gt; | &lt;term&gt; &lt;factor-operator&gt; &lt;factor&gt;
 * &lt;factor-operator&gt;    ::= * | /
 * &lt;factor&gt;             ::= &lt;constant&gt; | &lt;variable&gt; | &lt;function-call&gt; | ( &lt;expression&gt; )
 * &lt;constant&gt;           ::= &lt;digit-sequence&gt; | &lt;digit-sequence&gt; . &lt;digit-sequence&gt;
 * &lt;digit-sequence&gt;     ::= &lt;digit&gt; | &lt;digit-sequence&gt; &lt;digit&gt;
 * &lt;variable&gt;           ::= &lt;identifier&gt;
 * &lt;identifier&gt;         ::= &lt;character-sequence&gt;
 * &lt;character-sequence&gt; ::= &lt;character-sequence&gt; | &lt;character-sequence&gt; &lt;character&gt;
 * &lt;function-call&gt;      ::= &lt;identifier&gt; ( &lt;argument-list&gt; )
 * &lt;argument-list&gt;      ::= &lt;expression&gt; | &lt;argument-list&gt; , &lt;expression&gt;
 * </code></pre>
 * Or the following abstract grammer:
 * <pre><code>
 * ExpressionNode             ::= ConstantNode | ReadingTypeRequirementNode | ReadingTypeDeliverableNode | VariableNode | OperationNode | FunctionCallNode
 * ConstantNode               ::= LeafNode(BigDecimal)
 * ReadingTypeRequirementNode ::= LeafNode(ReadingTypeRequirement)
 * ReadingTypeDeliverableNode ::= LeafNode(ReadingTypeDeliverable)
 * CustomPropertyNode         ::= LeafNode(CustomPropertySet, PropertySpec)
 * IdentifierNode             ::= LeafNode(Function)
 * OperationNode              ::= InternalNode(Operator)
 * Operator                   ::= + | - | * | /
 * Function                   ::= AVG | MIN | MAX | SUM
 * FunctionCallNode           ::= InternalNode(IdentifierNode, ArgumentListNode)
 * </code></pre>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-04 (15:08)
 */
@ProviderType
public interface ExpressionNode {

    interface Visitor<T> {
        T visitConstant(ConstantNode constant);

        T visitRequirement(ReadingTypeRequirementNode requirement);

        T visitDeliverable(ReadingTypeDeliverableNode deliverable);

        T visitProperty(CustomPropertyNode property);

        T visitOperation(OperationNode operationNode);

        T visitFunctionCall(FunctionCallNode functionCall);

        T visitNull(NullNode nullNode);
    }

    <T> T accept(Visitor<T> visitor);

    Dimension getDimension();

    ExpressionNode getParent();

    List<ExpressionNode> getChildren();

}