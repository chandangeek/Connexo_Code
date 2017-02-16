/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.generalattributes.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.generalAttributesEdit',
    itemId: 'generalAttributesEdit',

    requires: [
        'Uni.util.FormErrorMessage',
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu',
        'Uni.property.form.Property'
    ],

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'form',
                ui: 'large',
                title: Uni.I18n.translate('deviceconfiguration.generalAttributes.edit', 'MDC', 'Edit general attributes'),
                layout: {
                    type: 'vbox',
                    align: 'left'
                },
                items: [
                    {
                        itemId: 'form-errors',
                        xtype: 'uni-form-error-message',
                        width: 550,
                        margin: '0 0 10 0',
                        hidden: true
                    },
                    {
                        xtype: 'property-form',
                        width: '100%'
                    }
                ],
                dockedItems: {
                    xtype: 'container',
                    dock: 'bottom',
                    margin: '20 0 0 265',
                    layout: {
                        type: 'hbox',
                        align: 'stretch'
                    },

                    items: [
                        {
                            xtype: 'button',
                            itemId: 'generalAttributesSaveBtn',

                            ui: 'action',
                            text: Uni.I18n.translate('general.save', 'MDC', 'Save')
                        },
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('general.restoreToDefaultSettings', 'MDC', 'Restore to default settings'),
                            iconCls: 'icon-rotate-ccw3',
                            itemId: 'generalAttributesRestoreDefaultBtn'
                        },
                        {
                            xtype: 'button',
                            ui: 'link',
                            itemId: 'generalAttributesCancelBtn',
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel')
                        }
                    ]
                }
            }
        ];

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'device-configuration-menu',
                        itemId: 'stepsMenu',
                        deviceTypeId: me.router.arguments.deviceTypeId,
                        deviceConfigurationId: me.router.arguments.deviceConfigurationId
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});