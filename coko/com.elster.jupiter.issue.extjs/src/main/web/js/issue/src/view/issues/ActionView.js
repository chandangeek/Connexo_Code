/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.issues.ActionView', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Isu.view.issues.ActionForm'
    ],
    alias: 'widget.issue-action-view',
    router: null,
    actionItemId: null,
    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'issue-action-form',
                itemId: 'issue-action-view-form',
                title: ' ',
                ui: 'large',
                router: me.router,
                actionItemId: me.actionItemId
            }
        ];

        me.callParent(arguments);
    }
});