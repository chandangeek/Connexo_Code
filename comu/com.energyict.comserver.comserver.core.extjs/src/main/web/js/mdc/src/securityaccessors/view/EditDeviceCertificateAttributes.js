/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.view.EditDeviceCertificateAttributes', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-certificate-attributes-edit',

    device: undefined,
    certificateRecord: undefined,

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
                    itemId: 'mdc-device-certificate-attributes-edit-buttons',
                    fieldLabel: ' ',
                    layout: 'hbox',
                    margin: '20 0 0 0',
                    items: [
                        {
                            xtype: 'button',
                            itemId: 'mdc-device-certificate-attributes-edit-save-button',
                            text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                            ui: 'action'
                        },
                        {
                            xtype: 'button',
                            itemId: 'mdc-device-certificate-attributes-edit-cancel-link',
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
