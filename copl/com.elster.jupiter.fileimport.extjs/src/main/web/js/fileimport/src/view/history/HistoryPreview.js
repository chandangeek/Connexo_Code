/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fim.view.history.HistoryPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.fim-history-preview',
    requires: [
        'Fim.view.history.HistoryPreviewForm'
    ],

    items: {
        xtype: 'fim-history-preview-form'
    }
});

