/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceattributes.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceAttributesSetup',
    itemId: 'device-attributes-setup',

    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Mdc.view.setup.device.DeviceAttributesForm',
        'Mdc.customattributesonvaluesobjects.view.AttributeSetsPlaceholderForm',
        'Mdc.view.setup.deviceattributes.DeviceAttributesActionMenu'
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
                        device: me.device
                    }
                ]
            }
        ];

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('devicemenu.deviceAttributes', 'MDC', 'Device attributes'),
                tools: [
                    {
                        xtype: 'uni-button-action',
                        itemId: 'device-attributes-actions-button',
                        menu: {
                            xtype: 'device-attributes-action-menu',
                            router: me.router
                        }
                    }
                ]
            },
            {
                xtype: 'container',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'fieldcontainer',
                        labelAlign: 'top',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        flex: 1,
                        items: [
                            {
                                xtype: 'deviceAttributesForm',
                                itemId: 'device-attributes-view-form',
                                router: me.router,
                                fullInfo: true
                            }
                        ]
                    },
                    {
                        xtype: 'custom-attribute-sets-placeholder-form',
                        itemId: 'custom-attribute-sets-placeholder-form-id',
                        actionMenuXtype: 'device-attributes-action-menu',
                        attributeSetType: 'device',
                        router: me.router
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});