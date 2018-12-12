/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.summary.validation.RulesSetActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.validationRulesSetActionMenu',
    itemId: 'validationRulesSetActionMenu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'changeRuleSetStateActionMenuItem'
            }
        ];
        this.callParent(arguments);
    }

});