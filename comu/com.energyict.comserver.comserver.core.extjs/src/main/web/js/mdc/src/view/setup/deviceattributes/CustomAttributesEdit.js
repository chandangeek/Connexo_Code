/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceattributes.CustomAttributesEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-custom-attributes-edit',
    itemId: 'device-custom-attributes-edit-id',

    requires: [
        'Mdc.view.setup.deviceattributes.DeviceAttributesEditForm',
        'Uni.property.form.Property'
    ],

    content: [
        {
            xtype: 'panel',
            itemId: 'custom-attribute-set-edit-panel',
            ui: 'large',
            items: [
                {
                    itemId: 'device-custom-attributes-error-msg',
                    xtype: 'uni-form-error-message',
                    width: 521,
                    hidden: true
                },
                {
                    xtype: 'property-form',
                    itemId: 'device-custom-attributes-property-form'
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
                        itemId: 'device-custom-attributes-save-btn',
                        ui: 'action',
                        text: Uni.I18n.translate('general.save', 'MDC', 'Save')
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.restoretodefaults', 'MDC', 'Restore to defaults'),
                        iconCls: 'icon-rotate-ccw3',
                        itemId: 'device-custom-attributes-restore-default-btn'
                    },
                    {
                        xtype: 'button',
                        ui: 'link',
                        itemId: 'device-custom-attributes-cancel-btn',
                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel')
                    }
                ]
            }
        }
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

        me.callParent(arguments);
    }
});