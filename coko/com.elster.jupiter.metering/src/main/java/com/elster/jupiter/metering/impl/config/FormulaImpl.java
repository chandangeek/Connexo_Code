package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;
import com.elster.jupiter.metering.impl.aggregation.IntervalLength;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by igh on 11/02/2016.
 */
public class FormulaImpl implements ServerFormula {

    @SuppressWarnings("unused")// Managed by ORM
    private long id;
    private Mode mode;
    private Reference<ExpressionNode> expressionNode = ValueReference.absent();
    private final DataModel dataModel;

    @Inject
    FormulaImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public FormulaImpl init(Mode mode, ExpressionNode expressionNode) {
        this.mode = mode;
        this.expressionNode.set(expressionNode);
        return this;
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    @Override
    public void updateExpression(ExpressionNode nodeValue) {
        ExpressionNode oldNode = this.expressionNode.get();
        this.expressionNode = ValueReference.absent();
        this.expressionNode.set(nodeValue);
        this.expressionNode.get().save(dataModel);
        Save.UPDATE.save(dataModel, this);
        oldNode.delete(dataModel);
    }

    @Override
    public String getDescription() {
        if (expressionNode.isPresent()) {
            return expressionNode.get().toString();
        } else {
            return "";
        }
    }

    @Override
    public ExpressionNode getExpressionNode() {
        return expressionNode.get();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerFormula)) {
            return false;
        }
        ServerFormula serverFormula = (ServerFormula) o;
        return id == serverFormula.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public void delete() {
        dataModel.remove(this);
    }

    @Override
    public void save() {
        doSave();
    }

    void doSave() {
        if (id == 0) {
            this.expressionNode.get().save(dataModel);
            Save.CREATE.save(dataModel, this);
        } else {
            Save.UPDATE.save(dataModel, this);
        }
    }

    public String toString() {
        String result = "id: " + this.id + ", mode: " + this.mode;
        if (expressionNode.isPresent()) {
            result = result + ", formula: " + expressionNode.get().toString();
        }
        return result;
    }

    @Override
    //return the largest requirement interval or NOT_SUPPORTED if no requirements found or if only requirements with wildcards (for interval) found
    public IntervalLength getIntervalLength() {
        List<ReadingTypeRequirementNode> reqNodes =
                ((AbstractNode) this.getExpressionNode()).getRequirements();
        Optional<IntervalLength> intervalLength = reqNodes.stream().map(
                reqNode -> ((ReadingTypeRequirementImpl) reqNode.getReadingTypeRequirement()).getIntervalLength())
                .filter(lentgh -> !lentgh.equals(IntervalLength.NOT_SUPPORTED))
                .sorted((a1, a2) -> Long.compare(a2.ordinal(), a1.ordinal())).findFirst();
        if (intervalLength.isPresent()) {
            return intervalLength.get();
        }
        return IntervalLength.NOT_SUPPORTED;
    }

}