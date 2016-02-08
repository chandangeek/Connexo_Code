package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by igh on 5/02/2016.
 */
public abstract class AbstractNode implements ServerExpressionNode {

    // ORM inheritance map
    static final Map<String, Class<? extends ServerExpressionNode>> IMPLEMENTERS =
            ImmutableMap.<String, Class<? extends ServerExpressionNode>>of(
                    ConstantNode.TYPE_IDENTIFIER, ConstantNode.class,
                    FunctionCallNode.TYPE_IDENTIFIER, FunctionCallNode.class,
                    OperationNode.TYPE_IDENTIFIER, OperationNode.class,
                    ReadingTypeDeliverableNode.TYPE_IDENTIFIER, ReadingTypeDeliverableNode.class,
                    ReadingTypeRequirementNode.TYPE_IDENTIFIER, ReadingTypeRequirementNode.class
            );

    private Reference<AbstractNode> parent = ValueReference.absent();
    private List<AbstractNode> children = new ArrayList<>();

    public AbstractNode() {
        super();
    }

    public AbstractNode(List<AbstractNode> children) {
        this();
        this.children.addAll(children);
        children.stream().forEach(child -> child.setParent(this));
    }

    public AbstractNode(List<AbstractNode> children, AbstractNode parentNode) {
        this(children);
        this.parent.set(parentNode);
    }

    public ExpressionNode getParent() {
        return parent.orNull();
    }

    public List<AbstractNode> getChildren() {
        return children;
    }

    public void setParent(AbstractNode parent) {
        this.parent.set(parent);
    }

    public void setChildren(List<AbstractNode> children) {
        this.children.clear();
        this.children.addAll(children);
    }

}