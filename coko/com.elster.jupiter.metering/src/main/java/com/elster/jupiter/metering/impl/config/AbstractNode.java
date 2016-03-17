package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by igh on 5/02/2016.
 */
public abstract class AbstractNode implements ExpressionNode {

    // ORM inheritance map
    public static final Map<String, Class<? extends ExpressionNode>> IMPLEMENTERS =
            ImmutableMap.<String, Class<? extends ExpressionNode>>of(
                    ConstantNode.TYPE_IDENTIFIER, ConstantNode.class,
                    FunctionCallNode.TYPE_IDENTIFIER, FunctionCallNode.class,
                    OperationNode.TYPE_IDENTIFIER, OperationNode.class,
                    ReadingTypeDeliverableNode.TYPE_IDENTIFIER, ReadingTypeDeliverableNode.class,
                    ReadingTypeRequirementNode.TYPE_IDENTIFIER, ReadingTypeRequirementNode.class
            );

    private long id;
    private Reference<AbstractNode> parent = ValueReference.absent();
    private List<ExpressionNode> children = new ArrayList<>();
    private long argumentIndex;

    public AbstractNode() {
        super();
    }

    public AbstractNode(List<? extends ExpressionNode> children) {
        this();
        this.children.addAll(children);
        children.stream()
                .map(AbstractNode.class::cast)
                .forEach(child -> child.setParent(this));
    }

    public AbstractNode(List<AbstractNode> children, AbstractNode parentNode) {
        this(children);
        this.parent.set(parentNode);
    }

    public ExpressionNode getParent() {
        return parent.orNull();
    }

    public List<ExpressionNode> getChildren() {
        return children;
    }

    /*public List<ExpressionNode> getChildren(DataModel dataModel) {
        return dataModel.mapper(ExpressionNode.class).find("PARENTID", this.id);
    }*/

    public void setParent(AbstractNode parent) {
        this.parent.set(parent);
    }

    public long getId() {
        return id;
    }

    public void setChildren(List<AbstractNode> children) {
        this.children.clear();
        this.children.addAll(children);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractNode)) {
            return false;
        }
        AbstractNode node = (AbstractNode) o;
        return id == node.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


    @Override
    public void save(DataModel dataModel) {
        doSave(dataModel);
        for (ExpressionNode node : children) {
            node.save(dataModel);
        }
    }

    @Override
    public void validate() {

    }

    void doSave(DataModel dataModel) {
        if (id == 0) {
            persist(dataModel);
        } else {
            doUpdate(dataModel);
        }
    }


    private void persist(DataModel dataModel) {
        Save.CREATE.save(dataModel, this);
    }

    private void doUpdate(DataModel dataModel) {
        Save.UPDATE.save(dataModel, this);
    }

    @Override
    public void delete(DataModel dataModel) {
        dataModel.remove(this);
    }

}