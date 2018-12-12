/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.loadProfileConfigurationSetup',
    itemId: 'loadProfileConfigurationSetup',

    requires: [
        'Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationGrid',
        'Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationPreview',
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    router: null,
    deviceConfigurationId: null,
    deviceTypeId: null,

    initComponent: function () {
        var me = this;

        me.side = {
            xtype: 'panel',
                ui: 'medium',
                items: [
                {
                    xtype: 'device-configuration-menu',
                    itemId: 'stepsMenu',
                    deviceTypeId: me.deviceTypeId,
                    deviceConfigurationId: me.deviceConfigurationId
                }
            ]
        };

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('loadProfileConfigurations.title', 'MDC', 'Load profile configurations'),
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'loadProfileConfigurationGrid',
                            router: me.router
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'no-load-profile-configs',
                            title: Uni.I18n.translate('loadProfileConfigurations.empty.title', 'MDC', 'No load profile configurations found'),
                            reasons: [
                                Uni.I18n.translate('loadProfileConfigurations.empty.list.item1', 'MDC', 'No load profile configurations have been defined yet.')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('loadProfileConfigurations.add', 'MDC', 'Add load profile configuration'),
                                    privileges: Mdc.privileges.DeviceType.admin,
                                    action: 'addloadprofileconfiguration',
                                    href: me.router.getRoute('administration/devicetypes/view/deviceconfigurations/view/loadprofiles/add').buildUrl()
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'loadProfileConfigurationPreview',
                            itemId: 'loadProfileConfigAndRulesPreviewContainer',
                            router: me.router
                        }
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});