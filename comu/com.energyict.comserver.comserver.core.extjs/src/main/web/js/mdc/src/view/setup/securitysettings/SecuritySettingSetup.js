/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.securitysettings.SecuritySettingSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.securitySettingSetup',
    itemId: 'securitySettingSetup',
    deviceTypeId: null,
    deviceConfigId: null,

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.securitysettings.SecuritySettingGrid',
        'Mdc.view.setup.securitysettings.SecuritySettingPreview',
        'Uni.view.container.PreviewContainer'
    ],

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'device-configuration-menu',
                        itemId: 'stepsMenu',
                        deviceTypeId: me.deviceTypeId,
                        deviceConfigurationId: me.deviceConfigId
                    }
                ]
            }
        ];

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('securitySetting.title','MDC','Security settings'),
                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'security-settings-grid-preview-container',
                        grid: {
                            xtype: 'securitySettingGrid',
                            deviceTypeId: me.deviceTypeId,
                            deviceConfigId: me.deviceConfigId
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'device-configuration-security-settings-empty-msg',
                            title: Uni.I18n.translate('securitySetting.NoSecuritySettingsFound','MDC','No security settings found'),
                            reasons: [
                                Uni.I18n.translate('securitySetting.reason1','MDC','No security settings have been defined yet.')
                            ],
                            stepItems: [
                                {
                                    text:  Uni.I18n.translate('securitySetting.addSecuritySetting','MDC','Add security setting'),
                                    privileges: Mdc.privileges.DeviceType.admin,
                                    href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigId + '/securitysettings/add'
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'securitySettingPreview',
                            itemId: 'security-setting-preview'
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});


