/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.customtask.HistoryPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.ctk-tasks-history-preview',

    requires: [
        'Apr.view.customtask.HistoryPreviewForm',
        'Apr.view.customtask.HistoryActionMenu'
    ],
    historyActionItemId: 'ctk-tasks-history-action-menu',

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'uni-button-action',
                privileges: me.canHistoryLog,
                menu: {
                    xtype: 'ctk-tasks-history-action-menu',
                    itemId: me.historyActionItemId
                }
            }
        ];

        me.items = {
            xtype: 'ctk-tasks-history-preview-form'
        }
        me.callParent(arguments);
    }
});

