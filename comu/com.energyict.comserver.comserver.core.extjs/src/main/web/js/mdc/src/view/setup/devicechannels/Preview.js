/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicechannels.Preview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceLoadProfileChannelsPreview',
    itemId: 'deviceLoadProfileChannelsPreview',
    requires: [
        'Mdc.view.setup.devicechannels.PreviewForm',
        'Mdc.view.setup.devicechannels.ActionMenu'
    ],
    layout: 'fit',
    frame: true,
    device: null,
    router: null,

    tools: [
        {
            xtype: 'uni-button-action',
            menu: {
                xtype: 'deviceLoadProfileChannelsActionMenu'
            }
        }
    ],


    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'deviceLoadProfileChannelsPreviewForm',
            router: me.router,
            device: me.device
        };

        me.callParent(arguments);
    }
});
