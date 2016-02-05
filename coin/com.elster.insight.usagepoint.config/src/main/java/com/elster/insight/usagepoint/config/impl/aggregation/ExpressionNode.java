package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.insight.usagepoint.config.ReadingTypeDeliverable;
import com.elster.insight.usagepoint.config.ReadingTypeRequirement;

/**
 * Models a node of expression that comply with the BNF below.
 * <pre><code>
 *     &lt;expression&gt;         ::= &lt;term&gt; | &lt;expression&gt; &lt;term-operator&gt; &lt;term&gt;
 *     &lt;term-operator&gt;      ::= + | -
 *     &lt;term&gt;               ::= &lt;factor&gt; | &lt;term&gt; &lt;factor-operator&gt; &lt;factor&gt;
 *     &lt;factor-operator&gt;    ::= * | /
 *     &lt;factor&gt;             ::= &lt;constant&gt; | &lt;variable&gt; | &lt;function-call&gt; | ( &lt;expression&gt; )
 *     &lt;constant&gt;           ::= &lt;digit-sequence&gt; | &lt;digit-sequence&gt; . &lt;digit-sequence&gt;
 *     &lt;digit-sequence&gt;     ::= &lt;digit&gt; | &lt;digit-sequence&gt; &lt;digit&gt;
 *     &lt;variable&gt;           ::= &lt;identifier&gt;
 *     &lt;identifier&gt;         ::= &lt;character-sequence&gt;
 *     &lt;character-sequence&gt; ::= &lt;character-sequence&gt; | &lt;character-sequence&gt; &lt;character&gt;
 *     &lt;function-call&gt;      ::= &lt;identifier&gt; ( &lt;argument-list&gt; )
 *     &lt;argument-list&gt;      ::= &lt;expression&gt; | &lt;argument-list&gt; , &lt;expression&gt;
 * </code></pre>
 * Or the following abstract grammer:
 * <pre><code>
 * ExpressionNode             ::= ConstantNode | ReadingTypeRequirementNode | ReadingTypeDeliverableNode | VariableNode | BinaryNode(ExpressionNode, ExpressionNode, Operator) | FunctionCallNode
 * ConstantNode               ::= LeafNode(BigDecimal)
 * ReadingTypeRequirementNode ::= LeafNode(ReadingTypeRequirement)
 * ReadingTypeDeliverableNode ::= LeafNode(ReadingTypeDeliverable)
 * VariableNode               ::= LeafNode(CustomPropertySet, PropertySpec)
 * IdentifierNode             ::= LeafNode(String)
 * OperationNode              ::= BinaryNode(Operator, ExpressionNode, ExpressionNode)
 * Operator                   ::= + | - | * | /
 * FunctionCallNode           ::= BinaryNode(IdentifierNode, ArgumentListNode)
 * ArgumentListNode           ::= List<LeafNode>
 * </code></pre>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-04 (15:08)
 */
public interface ExpressionNode {

    interface Visitor {
        void visitConstant(ConstantNode constant);
        void visitRequirement(ReadingTypeRequirement requirement);
        void visitDeliverable(ReadingTypeDeliverable deliverable);
        //void visitVariable(ExpressionNode variable);
        void visitIdentifier(IdentifierNode identifier);
        void visitOperation(OperationNode operatorNode);
        void visitFunctionCall(FunctionCallNode functionCall);
        void visitArgumentList(ArgumentListNode argumentList);
    }

    void accept(Visitor visitor);

}