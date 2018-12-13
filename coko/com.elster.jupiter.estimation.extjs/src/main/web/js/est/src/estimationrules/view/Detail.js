/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.estimationrules.view.Detail', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Est.estimationrules.view.DetailForm',
        'Est.estimationrules.view.SideMenu'
    ],
    alias: 'widget.estimation-rule-detail',
    itemId: 'estimation-rule-detail',
    router: null,
    actionMenuItemId: null,

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'estimation-rule-side-menu',
                        itemId: 'estimation-rule-side-menu',
                        router: me.router
                    }
                ]
            }
        ];

        me.content = [
            {
                items: [
                    {
                        xtype: 'estimation-rules-detail-form',
                        itemId: 'estimation-rules-detail-form',
                        ui: 'large',
                        actionMenuItemId: me.actionMenuItemId,
                        staticTitle: true
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});