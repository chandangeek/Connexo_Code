/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicelogbooks.Preview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceLogbooksPreview',
    itemId: 'deviceLogbooksPreview',
    requires: [
        'Mdc.view.setup.devicelogbooks.PreviewForm',
        'Mdc.view.setup.devicelogbooks.ActionMenu',
        'Mdc.view.setup.devicelogbooks.TabbedDeviceLogBookView'
    ],
    layout: 'fit',
    frame: true,

    initComponent: function () {
        var me = this;
        me.items = {
            xtype: 'deviceLogbooksPreviewForm'
        };

        if (Mdc.privileges.Device.canAdministrateDeviceData()) {
            me.tools = [
                {
                    xtype: 'uni-button-action',
                    menu: {
                        xtype: 'deviceLogbooksActionMenu'
                    }
                }
            ];
        }

        me.callParent(arguments)
    },

    setLogbook: function(logbookRecord) {
        this.setTitle(logbookRecord.get('name'));
        this.down('#deviceLogbooksActionMenu').record = logbookRecord;
        this.down('#deviceLogbooksPreviewForm').loadRecord(logbookRecord);
    }
});
