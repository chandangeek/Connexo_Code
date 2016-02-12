package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.insight.usagepoint.config.Formula;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Objects;

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

    public ServerFormulaImpl init(Mode mode, ExpressionNode expressionNode) {
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
    public void update() {
        doSave();
    }

    void doSave() {
        if (id == 0) {
            persist();
        } else {
            doUpdate();
        }
    }

    private void persist() {
        Save.CREATE.save(dataModel, this.expressionNode.get());
        Save.CREATE.save(dataModel, this);
    }

    private void doUpdate() {
        Save.UPDATE.save(dataModel, this.expressionNode.get());
        Save.UPDATE.save(dataModel, this);
    }
}
