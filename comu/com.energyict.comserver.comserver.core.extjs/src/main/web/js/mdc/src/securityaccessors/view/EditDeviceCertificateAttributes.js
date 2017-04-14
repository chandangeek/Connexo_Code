/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.view.EditDeviceCertificateAttributes', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-certificate-attributes-edit',
    requires: [
        'Uni.util.FormErrorMessage'
    ],

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
                    xtype: 'uni-form-error-message',
                    hidden: true
                },
                {
                    xtype: 'container',
                    itemId: 'mdc-device-certificate-attributes-edit-active-attributes-container',
                    items: [
                        {
                            xtype: 'form',
                            border: false,
                            defaults: {
                                labelWidth: 200,
                                labelAlign: 'left'
                            },
                            items: [
                                {
                                    xtype: 'displayfield',
                                    emptyValueDisplay: '',
                                    fieldLabel: Uni.I18n.translate('general.activeCertificate', 'MDC', 'Active certificate')
                                }
                            ]
                        },
                        {
                            xtype: 'property-form',
                            itemId: 'mdc-device-certificate-attributes-edit-active-attributes-property-form',
                            isEdit: true,
                            defaults: {
                                layout: 'form',
                                resetButtonHidden: true,
                                labelWidth: 200
                            }
                        }
                    ]
                },
                {
                    xtype: 'container',
                    itemId: 'mdc-device-certificate-attributes-edit-passive-attributes-container',
                    items: [
                        {
                            xtype: 'form',
                            border: false,
                            defaults: {
                                labelWidth: 200,
                                labelAlign: 'left'
                            },
                            items: [
                                {
                                    xtype: 'displayfield',
                                    emptyValueDisplay: '',
                                    fieldLabel: Uni.I18n.translate('general.passiveCertificate', 'MDC', 'Passive certificate')
                                }
                            ]
                        },
                        {
                            xtype: 'property-form',
                            itemId: 'mdc-device-certificate-attributes-edit-passive-attributes-property-form',
                            isEdit: true,
                            defaults: {
                                layout: 'form',
                                resetButtonHidden: true,
                                labelWidth: 200
                            }
                        }
                    ]
                },
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
