package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.util.Objects;

/**
 * Created by igh on 11/02/2016.
 */
public class FormulaImpl implements ServerFormula {

    @SuppressWarnings("unused")// Managed by ORM
    private long id;
    private Mode mode;
    @ValidExpression
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
    public ExpressionNode expressionNode() {
        return expressionNode.orNull();
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

}