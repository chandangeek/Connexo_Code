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

    tools: [
        {
            xtype: 'uni-button-action',
            menu: {
                xtype: 'cfg-tasks-history-action-menu'
            }
        }
    ],

    items: {
        xtype: 'cfg-tasks-history-preview-form'
    }
});

