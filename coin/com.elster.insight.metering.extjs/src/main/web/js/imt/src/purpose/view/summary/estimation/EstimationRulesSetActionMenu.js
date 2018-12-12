/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.summary.estimation.EstimationRulesSetActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.estimationCfgRulesSetActionMenu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'changeEstimationRuleSetStateActionMenuItem'
            }
        ];
        this.callParent(arguments);
    }
});