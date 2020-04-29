/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.view.tasks.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.dxp-tasks-preview',

    requires: [
        'Dxp.view.tasks.PreviewForm'
    ],

    tools: [
        {
            xtype: 'uni-button-action',
            menu: {
                xtype: 'dxp-tasks-action-menu'
            }
        }
    ],

    items: {
        xtype: 'dxp-tasks-preview-form',
        maxWidth: 900,
        itemId: 'pnl-data-export-task-preview-form'
    }
});
