/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicedataestimation.RulesSetActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.deviceDataEstimationRulesSetActionMenu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'changeEstimationRuleSetStateActionMenuItem'
            }
        ];
        this.callParent(arguments);
    }
});