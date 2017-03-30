/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.aggregation.IntervalLength;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link ServerFormula} interface.
 *
 * Created by igh on 11/02/2016.
 */
public class FormulaImpl implements ServerFormula {

    @SuppressWarnings("unused")// Managed by ORM
    private long id;
    private Mode mode;
    private Reference<ServerExpressionNode> expressionNode = ValueReference.absent();
    private final DataModel dataModel;

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    @Inject
    FormulaImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public FormulaImpl init(Mode mode, ServerExpressionNode expressionNode) {
        this.mode = mode;
        this.expressionNode.set(expressionNode);
        return this;
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    @Override
    public void updateExpression(ServerExpressionNode nodeValue) {
        ServerExpressionNode oldNode = this.expressionNode.get();
        this.expressionNode = ValueReference.absent();
        this.expressionNode.set(nodeValue);
        this.expressionNode.get().save(dataModel);
        Save.UPDATE.save(dataModel, this);
        oldNode.delete(dataModel);
    }

    @Override
    public ServerExpressionNode getExpressionNode() {
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
        if (this.expressionNode.isPresent()) {
            expressionNode.get().delete(dataModel);
        }
    }

    @Override
    public void save() {
        doSave();
    }

    private void doSave() {
        if (id == 0) {
            this.expressionNode.get().save(dataModel);
            Save.CREATE.save(dataModel, this);
        } else {
            Save.UPDATE.save(dataModel, this);
        }
    }

    @Override
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
        List<ReadingTypeRequirement> reqNodes =
                this.getExpressionNode().accept(new ReadingTypeRequirementsCollector());
        Optional<IntervalLength> intervalLength = reqNodes.stream().map(
                reqNode -> ((ReadingTypeRequirementImpl) reqNode).getIntervalLength())
                .filter(length -> !length.equals(IntervalLength.NOT_SUPPORTED))
                .sorted((a1, a2) -> Long.compare(a2.ordinal(), a1.ordinal())).findFirst();
        return intervalLength.orElse(IntervalLength.NOT_SUPPORTED);
    }

    @Override
    public List<IntervalLength> getIntervalLengths() {
        List<ReadingTypeRequirement> reqNodes =
                this.getExpressionNode().accept(new ReadingTypeRequirementsCollector());
        return reqNodes.stream().map(
                reqNode -> ((ReadingTypeRequirementImpl) reqNode).getIntervalLength())
                .filter(length -> !length.equals(IntervalLength.NOT_SUPPORTED)).collect(Collectors.toList());
    }

}
