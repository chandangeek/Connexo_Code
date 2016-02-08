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

    public AbstractNode(Reference<ExpressionNode> parent, List<ExpressionNode> children) {
        this.parent = parent;
        this.children = children;
    }

    public Reference<ExpressionNode> getParent() {
        return parent;
    }

    public List<ExpressionNode> getChildren() {
        return children;
    }
}
