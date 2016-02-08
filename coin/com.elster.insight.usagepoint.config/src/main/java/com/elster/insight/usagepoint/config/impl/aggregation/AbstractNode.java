package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by igh on 5/02/2016.
 */
public class AbstractNode {

    private Reference<ExpressionNode> parent = ValueReference.absent();
    private List<ExpressionNode> children = new ArrayList<>();

    public AbstractNode() {
    }

    public AbstractNode(List<ExpressionNode> children) {
        this.children = children;
    }


    public AbstractNode(List<ExpressionNode> children, ExpressionNode parentNode) {
        this(children);
        this.parent.set(parentNode);
    }

    public Reference<ExpressionNode> getParent() {
        return parent;
    }

    public List<ExpressionNode> getChildren() {
        return children;
    }

    public void setParent(Reference<ExpressionNode> parent) {
        this.parent = parent;
    }

    public void setChildren(List<ExpressionNode> children) {
        this.children = children;
    }
}
