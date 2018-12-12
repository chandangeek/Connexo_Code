/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.customtask.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.ctk-tasks-preview',

    requires: [
        'Apr.view.customtask.PreviewForm'
    ],

    tools: [
        {
            xtype: 'uni-button-action',
            menu: {
                xtype: 'ctk-tasks-action-menu'
            }
        }
    ],

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'ctk-task-preview-form',
            itemId: 'pnl-task-preview-form',
            appName: me.appName
        };

        me.callParent(arguments);
    }
});
