/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.view.tasks.HistoryPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.dxp-tasks-history-preview',

    requires: [
        'Dxp.view.tasks.HistoryPreviewForm',
        'Dxp.view.tasks.HistoryActionMenu'
    ],

    tools: [
        {
            xtype: 'uni-button-action',
            menu: {
                xtype: 'tasks-history-action-menu'
            }
        }
    ],

    items: {
        xtype: 'dxp-tasks-history-preview-form'//,
        //itemId: 'pnl-data-export-task-preview'
    }
});

