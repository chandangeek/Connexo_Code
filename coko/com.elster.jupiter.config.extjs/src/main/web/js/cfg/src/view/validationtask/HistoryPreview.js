/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.validationtask.HistoryPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.cfg-tasks-history-preview',

    requires: [
        'Cfg.view.validationtask.HistoryPreviewForm',
        'Cfg.view.validationtask.HistoryActionMenu'
    ],
    historyActionItemId: 'cfg-tasks-history-action-menu',

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'uni-button-action',
                menu: {
                    xtype: 'cfg-tasks-history-action-menu',
                    itemId: me.historyActionItemId
                }
            }
        ];

        me.items = {
            xtype: 'cfg-tasks-history-preview-form'
        }
        me.callParent(arguments);
    }
});

