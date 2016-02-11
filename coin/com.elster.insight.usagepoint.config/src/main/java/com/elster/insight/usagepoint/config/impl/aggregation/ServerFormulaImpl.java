package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.insight.usagepoint.config.Formula;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.time.Instant;

/**
 * Created by igh on 11/02/2016.
 */
public class ServerFormulaImpl implements ServerFormula {

    private long id;
    private Mode mode;
    private Reference<ExpressionNode> expressionNode = ValueReference.absent();
    private final DataModel dataModel;

    @Inject
    ServerFormulaImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    ServerFormulaImpl init(Mode mode, ExpressionNode expressionNode) {
        this.mode = mode;
        this.expressionNode.set(expressionNode);
        return this;
    }


    @Override
    public ExpressionNode expressionNode() {
        return expressionNode.orNull();
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public long getId() {
        return id;
    }
}
