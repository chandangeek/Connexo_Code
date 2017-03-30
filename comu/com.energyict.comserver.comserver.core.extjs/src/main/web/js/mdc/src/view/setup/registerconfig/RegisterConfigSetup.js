/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.registerconfig.RegisterConfigSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerConfigSetup',
    itemId: 'registerConfigSetup',

    deviceTypeId: null,
    deviceConfigId: null,

    requires: [
        'Mdc.view.setup.registerconfig.RegisterConfigGrid',
        'Mdc.view.setup.registerconfig.RegisterConfigFilter',
        'Mdc.view.setup.registerconfig.RegisterConfigPreview',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu',
        'Mdc.view.setup.registerconfig.RegisterConfigAndRulesPreviewContainer'
    ],

    initComponent: function () {
        this.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'device-configuration-menu',
                        itemId: 'stepsMenu',
                        deviceTypeId: this.deviceTypeId,
                        deviceConfigurationId: this.deviceConfigId
                    }
                ]
            }
        ];

        this.content = [
            {
                ui: 'large',
                xtype: 'panel',
                itemId: 'registerConfigSetupPanel',
                title: Uni.I18n.translate('registerConfig.registerConfigs', 'MDC', 'Register configurations'),

                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'previewContainer',
                        grid: {
                            xtype: 'registerConfigGrid',
                            deviceTypeId: this.deviceTypeId,
                            deviceConfigId: this.deviceConfigId
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'ctr-no-register-configuration',
                            title: Uni.I18n.translate('registerConfig.empty.title', 'MDC', 'No register configurations found'),
                            reasons: [
                                Uni.I18n.translate('registerConfig.empty.list.item1', 'MDC', 'No register configurations have been added yet.')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('registerConfig.addRegisterConfiguration', 'MDC', 'Add register configuration'),
                                    privileges: Mdc.privileges.DeviceType.admin,
                                    itemId: 'createRegisterConfigurationButton',
                                    action: 'createRegisterConfig'
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'register-config-and-rules-preview-container',
                            deviceTypeId: this.deviceTypeId,
                            deviceConfigId: this.deviceConfigId
                        }
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});