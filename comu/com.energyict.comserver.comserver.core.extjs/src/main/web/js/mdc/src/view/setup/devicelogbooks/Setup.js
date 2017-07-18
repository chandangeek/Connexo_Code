/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicelogbooks.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLogbooksSetup',
    itemId: 'deviceLogbooksSetup',

    device: null,
    router: null,
    toggleId: null,
    requires: [
        'Uni.view.container.PreviewContainer',
        'Uni.util.FormEmptyMessage',
        'Mdc.view.setup.device.DeviceMenu',
        'Mdc.view.setup.devicelogbooks.Grid',
        'Mdc.view.setup.devicelogbooks.Preview'
    ],

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        device: me.device,
                        toggleId: me.toggleId
                    }
                ]
            }
        ];

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.logbooks', 'MDC', 'Logbooks'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'deviceLogbooksGrid',
                        router: me.router,
                        itemId: 'deviceLogbooksGrid'
                    },
                    emptyComponent: {
                        xtype: 'uni-form-empty-message',
                        text: Uni.I18n.translate('devicelogbooks.empty', 'MDC', 'No logbooks have been defined yet.')
                    },
                    previewComponent: {
                        xtype: 'deviceLogbooksPreview',
                        device: me.device
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});