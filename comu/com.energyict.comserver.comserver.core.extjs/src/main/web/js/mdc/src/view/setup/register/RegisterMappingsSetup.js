/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.register.RegisterMappingsSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerMappingsSetup',
    itemId: 'registerMappingSetup',

    deviceTypeId: null,

    requires: [
        'Mdc.view.setup.register.RegisterMappingsGrid',
        'Mdc.view.setup.register.RegisterMappingsFilter',
        'Mdc.view.setup.register.RegisterMappingPreview',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.devicetype.SideMenu'
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
                        deviceTypeId: this.deviceTypeId
                    }
                ]
            }
        ];

        this.content = [
            {
                ui: 'large',
                xtype: 'panel',
                itemId: 'registerMappingsSetupPanel',
                title: Uni.I18n.translate('general.registerTypes', 'MDC', 'Register types'),

                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'previewContainer',
                        grid: {
                            xtype: 'registerMappingsGrid',
                            deviceTypeId: this.deviceTypeId
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'register-mappings-empty-msg',
                            title: Uni.I18n.translate('registerMapping.empty.title', 'MDC', 'No register types found'),
                            reasons: [
                                Uni.I18n.translate('registerMapping.empty.list.item1', 'MDC', 'No register types have been defined yet.')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('registerMapping.addRegisterMapping', 'MDC', 'Add register types'),
                                    privileges: Mdc.privileges.DeviceType.admin,
                                    itemId: 'empty-msg-add-register-mapping-btn',
                                    action: 'addRegisterMapping'
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'registerMappingPreview',
                            deviceTypeId: this.deviceTypeId
                        }
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});


