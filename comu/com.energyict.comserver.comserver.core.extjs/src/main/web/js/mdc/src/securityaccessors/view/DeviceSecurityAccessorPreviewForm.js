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
            statusField = {
                fieldLabel: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                name: 'status'
            },
            validUntilField = {
                fieldLabel: Uni.I18n.translate('general.validUntil', 'MDC', 'Valid until'),
                name: 'expirationTime',
                renderer: function (value) {
                    if (Ext.isEmpty(value) || value === 0) {
                        return '-';
                    }
                    return Uni.DateTime.formatDateShort(new Date(value));
                }
            },
            lastReadDateField = {
                fieldLabel: Uni.I18n.translate('general.lastReadDate', 'MDC', 'Last read date'),
                name: 'lastReadDate'
            },
            manageCentrallyField = {
                fieldLabel: Uni.I18n.translate('general.manageCentrally', 'MDC', 'Manage centrally'),
                name: 'editable',
                renderer: function (value) {
                   return value ? Uni.I18n.translate('general.no', 'MDC', 'No')
                       : Uni.I18n.translate('general.yes', 'MDC', 'Yes')
                }
            },
            leftContainer = {
                xtype: 'container',
                layout: 'form',
                columnWidth: 0.5,
                itemId: 'mdc-device-security-accessor-preview-form-leftContainer',
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
            rightContainer = {
                xtype: 'container',
                layout: 'form',
                columnWidth: 0.5,
                itemId: 'mdc-device-security-accessor-preview-form-rightContainer',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: []
            },
            items = [
                {
                    xtype: 'form',
                    layout: 'column',
                    items: [
                        leftContainer,
                        rightContainer
                    ]
                },
                {
                    xtype: 'container',
                    itemId: 'mdc-device-security-accessor-preview-active-attributes-container',
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
                                    fieldLabel: me.keyMode
                                        ? Uni.I18n.translate('general.activeKey', 'MDC', 'Active key')
                                        : Uni.I18n.translate('general.activeCertificate', 'MDC', 'Active certificate')
                                },
                                {
                                    xtype: 'displayfield',
                                    itemId: 'mdc-device-security-accessor-preview-active-info',
                                    emptyValueDisplay: '',
                                    fieldLabel: '',
                                    htmlEncode: false,
                                    value: '',
                                    setInfo: function(value) {
                                        this.setValue('<span style="color: #686868; font-style: italic; margin-top: 0px">' + value + '</span>');
                                    }
                                }
                            ]
                        },
                        {
                            xtype: 'property-form',
                            isEdit: false,
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
                    itemId: 'mdc-device-security-accessor-preview-passive-attributes-container',
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
                                    fieldLabel: me.keyMode
                                        ? Uni.I18n.translate('general.passiveKey', 'MDC', 'Passive key')
                                        : Uni.I18n.translate('general.passiveCertificate', 'MDC', 'Passive certificate')
                                },
                                {
                                    xtype: 'displayfield',
                                    itemId: 'mdc-device-security-accessor-preview-passive-info',
                                    emptyValueDisplay: '',
                                    fieldLabel: '',
                                    htmlEncode: false,
                                    value: '',
                                    setInfo: function(value) {
                                        this.setValue('<span style="color: #686868; font-style: italic; margin-top: 0px">' + value + '</span>');
                                    },
                                    clearInfo: function() {
                                        this.setValue('');
                                    }
                                }
                            ]
                        },
                        {
                            xtype: 'property-form',
                            isEdit: false,
                            defaults: {
                                layout: 'form',
                                resetButtonHidden: true,
                                labelWidth: 200
                            }
                        }
                    ]
                }
            ];

        if (me.keyMode) {
            rightContainer.items.push(statusField);
            rightContainer.items.push(validUntilField);
        } else {
            leftContainer.items.push(statusField);
            rightContainer.items.push(lastReadDateField);
            rightContainer.items.push(validUntilField);
            rightContainer.items.push(manageCentrallyField);
        }

        me.items = items;
        me.callParent(arguments);
    }

});
