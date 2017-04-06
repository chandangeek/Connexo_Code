/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.view.DeviceSecurityAccessorPreviewForm', {
    extend: 'Ext.panel.Panel',
    frame: false,
    alias: 'widget.device-security-accessor-preview-form',
    requires: [
        'Uni.property.form.Property'
    ],

    keyMode: undefined,

    initComponent: function () {
        var me = this,
            items = [
                {
                    xtype: 'form',
                    layout: 'column',
                    defaults: {
                        xtype: 'container',
                        layout: 'form',
                        columnWidth: 0.5
                    },
                    items: [
                        {
                            defaults: {
                                xtype: 'displayfield',
                                labelWidth: 200
                            },
                            items: [
                                {
                                    fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                                    name: 'name'
                                },
                                {
                                    fieldLabel: Uni.I18n.translate('general.description', 'MDC', 'Description'),
                                    name: 'description'
                                }
                            ]
                        },
                        {
                            defaults: {
                                xtype: 'displayfield',
                                labelWidth: 200
                            },
                            items: [
                                {
                                    fieldLabel: Uni.I18n.translate('general.lastReadDate', 'MDC', 'Last read date'),
                                    hidden: me.keyMode,
                                    name: 'lastReadDate'
                                },
                                {
                                    fieldLabel: Uni.I18n.translate('general.validUntil', 'MDC', 'Valid until'),
                                    name: 'expirationTime',
                                    renderer: function (value) {
                                        if (Ext.isEmpty(value)) {
                                            return '-';
                                        }
                                        return Uni.DateTime.formatDateShort(new Date(value));
                                    }
                                }
                            ]
                        }
                    ]
                },
                {
                    xtype: 'container',
                    itemId: 'mdc-device-security-accessor-preview-current-attributes-container',
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
                                    fieldLabel: Uni.I18n.translate('general.currentAttributes', 'MDC', 'Current attributes')
                                }
                            ]
                        },
                        {
                            xtype: 'property-form',
                            isEdit: false,
                            layout: 'column',
                            defaults: {
                                layout: 'form',
                                resetButtonHidden: true,
                                labelWidth: 200,
                                columnWidth: 0.5
                            }
                        }
                    ]
                }
            ];

        if (me.keyMode) {
            items.push(
                {
                    xtype: 'container',
                    itemId: 'mdc-device-security-accessor-preview-temp-attributes-container',
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
                                    fieldLabel: Uni.I18n.translate('general.otherAttributes', 'MDC', 'Other attributes TBD')
                                }
                            ]
                        },
                        {
                            xtype: 'property-form',
                            isEdit: false,
                            layout: 'column',
                            defaults: {
                                layout: 'form',
                                resetButtonHidden: true,
                                labelWidth: 200,
                                columnWidth: 0.5
                            }
                        }
                    ]
                }
            );
        }

        me.items = items;
        me.callParent(arguments);
    }

});
