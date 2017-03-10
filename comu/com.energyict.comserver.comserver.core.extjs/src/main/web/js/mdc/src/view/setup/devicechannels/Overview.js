/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicechannels.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLoadProfileChannelOverview',
    itemId: 'deviceLoadProfileChannelOverview',

    requires: [
        'Mdc.view.setup.devicechannels.PreviewForm',
        'Mdc.view.setup.devicechannels.ValidationOverview',
        'Mdc.view.setup.devicechannels.ActionMenu'
    ],

    router: null,
    device: null,
    dataLoggerSlaveHistoryStore: null,

    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'container',
                layout: 'hbox',
                items: [
                    {
                        ui: 'large',
                        flex: 1,
                        items: {
                            xtype: 'deviceLoadProfileChannelsPreviewForm',
                            device: me.device,
                            router: me.router,
                            showDataLoggerSlaveHistory: true,
                            dataLoggerSlaveHistoryStore: me.dataLoggerSlaveHistoryStore,
                            margin: '0 0 0 0',
                            itemId: 'deviceLoadProfileChannelsOverviewForm'
                        }
                    },
                    {
                        xtype: 'uni-button-action',
                        margin: '20 0 0 0',
                        menu: {
                            xtype: 'deviceLoadProfileChannelsActionMenu'
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});