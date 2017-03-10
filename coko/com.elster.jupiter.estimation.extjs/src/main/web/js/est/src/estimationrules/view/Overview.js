/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.estimationrules.view.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Est.estimationrules.view.PreviewContainer',
        'Est.main.view.RuleSetSideMenu'
    ],
    alias: 'widget.estimation-rules-overview',
    itemId: 'estimation-rules-overview',
    router: null,
    actionMenuItemId: null,
    editOrder: false,

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'estimation-rule-set-side-menu',
                        itemId: 'estimation-rule-set-side-menu',
                        router: me.router
                    }
                ]
            }
        ];

        me.content = [
            {
                title: Uni.I18n.translate('general.estimationRules', 'EST', 'Estimation rules'),
                ui: 'large',
                items: [
                    {
                        xtype: 'estimation-rules-preview-container',
                        itemId: 'estimation-rules-preview-container',
                        router: me.router,
                        actionMenuItemId: me.actionMenuItemId,
                        editOrder: me.editOrder
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});