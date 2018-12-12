/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.view.EditDeviceKeyAttributes', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-key-attributes-edit',
    requires: [
        'Uni.util.FormErrorMessage'
    ],

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
                    xtype: 'uni-form-error-message',
                    hidden: true
                },
                {
                    xtype: 'container',
                    itemId: 'mdc-device-key-attributes-edit-active-attributes-container',
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
                                    fieldLabel: Uni.I18n.translate('general.activeKey', 'MDC', 'Active key')
                                }
                            ]
                        },
                        {
                            xtype: 'property-form',
                            itemId: 'mdc-device-key-attributes-edit-active-attributes-property-form',
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
                    itemId: 'mdc-device-key-attributes-edit-passive-attributes-container',
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
                                    fieldLabel: Uni.I18n.translate('general.passiveKey', 'MDC', 'Passive key')
                                }
                            ]
                        },
                        {
                            xtype: 'property-form',
                            itemId: 'mdc-device-key-attributes-edit-passive-attributes-property-form',
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
