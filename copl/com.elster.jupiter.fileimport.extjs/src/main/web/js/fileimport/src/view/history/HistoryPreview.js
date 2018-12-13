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

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'fim-history-preview-form',
            router: me.router
        };

        me.callParent(arguments);
    }
});

