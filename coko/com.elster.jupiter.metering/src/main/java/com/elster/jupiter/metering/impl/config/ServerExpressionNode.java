/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.impl.aggregation.IntermediateDimension;
import com.elster.jupiter.orm.DataModel;

import java.util.List;

/**
 * Adds behavior to {@link ExpressionNode} that is reserved
 * for server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-25 (15:41)
 */
public interface ServerExpressionNode extends ExpressionNode {

    List<ServerExpressionNode> getServerSideChildren();

    IntermediateDimension getIntermediateDimension();

    long getId();

    void save(DataModel dataModel);

    void delete(DataModel dataModel);

    void validate();

}