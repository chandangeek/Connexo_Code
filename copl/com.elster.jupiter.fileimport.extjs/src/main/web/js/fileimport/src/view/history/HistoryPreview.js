/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fim.view.history.HistoryPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.fim-history-preview',
    requires: [
        'Fim.view.history.HistoryPreviewForm',
        'Fim.view.history.HistoryActionMenu'
    ],
    tools: [
        {
            xtype: 'uni-button-action',
            menu: {
                xtype: 'fim-history-action-menu'
            }
        }
    ],

    items: {
        xtype: 'fim-history-preview-form'
    }
});

