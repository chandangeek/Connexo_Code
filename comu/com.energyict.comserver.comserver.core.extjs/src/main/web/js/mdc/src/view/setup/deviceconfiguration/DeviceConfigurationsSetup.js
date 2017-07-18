/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceconfiguration.DeviceConfigurationsSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceConfigurationsSetup',
    itemId: 'deviceConfigurationsSetup',

    deviceTypeId: null,

    requires: [
        'Mdc.view.setup.devicetype.SideMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    initComponent: function () {
        this.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceTypeSideMenu',
                        itemId: 'stepsMenu',
                        deviceTypeId: this.deviceTypeId,
                        toggle: 4
                    }
                ]
            }
        ];

        this.content = [
            {
                ui: 'large',
                xtype: 'panel',
                itemId: 'connectionMethodSetupPanel',
                title: Uni.I18n.translate('deviceconfiguration.deviceConfigurations', 'MDC', 'Device configurations'),

                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'previewContainer',
                        grid: {
                            xtype: 'deviceConfigurationsGrid',
                            deviceTypeId: this.deviceTypeId
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'ctr-no-device-config',
                            title: Uni.I18n.translate('deviceConfiguration.empty.title', 'MDC', 'No device configurations found'),
                            reasons: [
                                Uni.I18n.translate('deviceConfiguration.empty.list.item1', 'MDC', 'No device configurations have been added yet.')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('deviceConfiguration.addDeviceConfiguration', 'MDC', 'Add device configuration'),
                                    privileges: Mdc.privileges.DeviceType.admin,
                                    itemId: 'createDeviceConfigurationButton',
                                    action: 'createDeviceConfiguration'
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'deviceConfigurationPreview'
                        }
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});