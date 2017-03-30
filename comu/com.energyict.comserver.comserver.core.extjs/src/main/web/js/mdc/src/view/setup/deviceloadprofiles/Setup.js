/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceloadprofiles.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLoadProfilesSetup',
    itemId: 'deviceLoadProfilesSetup',

    router: null,
    device: null,
    requires: [
        'Uni.view.container.PreviewContainer',
        'Uni.util.FormEmptyMessage',
        'Mdc.view.setup.device.DeviceMenu',
        'Mdc.view.setup.deviceloadprofiles.Grid',
        'Mdc.view.setup.deviceloadprofiles.Preview'
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
                        toggleId: 'loadProfilesLink'
                    }
                ]
            }
        ];

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('deviceloadprofiles.loadProfiles', 'MDC', 'Load profiles'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'deviceLoadProfilesGrid',
                        deviceId: me.device.get('name'),
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'uni-form-empty-message',
                        itemId: 'no-load-profile',
                        text: Uni.I18n.translate('deviceloadprofiles.empty', 'MDC', 'No load profiles have been defined yet.')
                    },
                    previewComponent: {
                        xtype: 'deviceLoadProfilesPreview',
                        deviceId: me.device.get('name'),
                        router: me.router
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});