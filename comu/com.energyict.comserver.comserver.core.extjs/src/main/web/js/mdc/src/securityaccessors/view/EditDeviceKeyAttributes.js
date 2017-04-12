/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.view.EditDeviceKeyAttributes', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-key-attributes-edit',

    device: undefined,
    keyRecord: undefined,

    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'form',
            defaults: {
                labelWidth: 200,
                width: 500
            },
            ui: 'large',
            title: '',

            items: [
                {
                    xtype: 'fieldcontainer',
                    itemId: 'mdc-device-key-attributes-edit-buttons',
                    fieldLabel: ' ',
                    layout: 'hbox',
                    margin: '20 0 0 0',
                    items: [
                        {
                            xtype: 'button',
                            itemId: 'mdc-device-key-attributes-edit-save-button',
                            text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                            ui: 'action'
                        },
                        {
                            xtype: 'button',
                            itemId: 'mdc-device-key-attributes-edit-cancel-link',
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                            ui: 'link'
                        }
                    ]
                }
            ]

        };
        me.callParent(arguments);
    }

});
