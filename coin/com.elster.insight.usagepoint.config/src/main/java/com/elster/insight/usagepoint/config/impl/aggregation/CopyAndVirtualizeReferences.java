package com.elster.insight.usagepoint.config.impl.aggregation;

/**
 * Provides an implementation for the {@link ExpressionNode.Visitor} interface
 * that copies {@link ExpressionNode}s but applies the following replacements:
 * <ul>
 * <li>{@link ReadingTypeRequirementNode} -&gt; {@link VirtualRequirementNode}</li>
 * <li>{@link ReadingTypeDeliverableNode} -&gt; {@link VirtualDeliverableNode}</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-05 (13:04)
 */
class CopyAndVirtualizeReferences implements ExpressionNode.Visitor {

    private final VirtualNodeFactory virtualNodeFactory;
    private ExpressionNode underConstruction;

    CopyAndVirtualizeReferences(VirtualNodeFactory virtualNodeFactory) {
        super();
        this.virtualNodeFactory = virtualNodeFactory;
    }

    /**
     * Returns the copy of the ExpressionNode.
     * If this is called during the visit,
     * an incomplete copy will be returned.
     *
     * @return The copy of the ExpressionNode
     */
    ExpressionNode getCopy() {
        return this.underConstruction;
    }

}