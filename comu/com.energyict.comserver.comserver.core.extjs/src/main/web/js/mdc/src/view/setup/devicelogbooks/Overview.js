/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicelogbooks.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLogbookOverview',
    itemId: 'deviceLogbookOverview',
    requires: [
        'Mdc.view.setup.devicelogbooks.PreviewForm'
    ],
    router: null,
    content: {
        xtype: 'deviceLogbooksPreviewForm',
        ui: 'large'
    },

    initComponent: function () {
        var me = this;
        me.callParent(arguments);
    }
});

