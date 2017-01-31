/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.registerconfig.RegisterConfigPreview', {
    extend: 'Ext.form.Panel',
    frame: true,
    alias: 'widget.registerConfigPreview',
    itemId: 'registerConfigPreview',

    requires: [
        'Mdc.model.RegisterConfiguration',
        'Mdc.view.setup.registerconfig.RegisterConfigActionMenu',
        'Uni.form.field.ObisDisplay',
        'Uni.form.field.ReadingTypeDisplay'
    ],

    title: Uni.I18n.translate('general.details','MDC','Details'),

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Mdc.privileges.DeviceType.admin,
            menu: {
                xtype: 'register-config-action-menu'
            }
        }
    ],

    initComponent: function () {
        var me = this;

        this.items = [
            {
                xtype: 'container',
                layout: {
                    type: 'column'
                },
                items: [
                    {
                        xtype: 'container',
                        columnWidth: 0.5,
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 160
                        },
                        items: [
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('registerConfig.registerType', 'MDC', 'Register type'),
                                name: 'registerTypeName'
                            },
                            {
                                xtype: 'obis-displayfield',
                                name: 'overruledObisCode',
                                fieldLabel: Uni.I18n.translate('registerConfig.obisCode', 'MDC', 'OBIS code')
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        columnWidth: 0.5,
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 160
                        },
                        items: [
                            {
                                name: 'asText',
                                fieldLabel: Uni.I18n.translate('registerConfig.storeValueAs', 'MDC', 'Store value as'),
                                renderer: function (value) {
                                    return value
                                        ? Uni.I18n.translate('registerConfig.text', 'MDC', 'Text')
                                        : Uni.I18n.translate('registerConfig.number', 'MDC', 'Number')
                                }
                            },
                            {
                                name: 'overflow',
                                fieldLabel: Uni.I18n.translate('registerConfig.overflowValue', 'MDC', 'Overflow value'),
                                itemId: 'mdc-register-config-preview-overflow',
                                renderer: function (value) {
                                    return value ? value : '-';
                                }
                            },
                            {
                                name: 'numberOfFractionDigits',
                                fieldLabel: Uni.I18n.translate('registerConfig.numberOfFractionDigits', 'MDC', 'Number of fraction digits'),
                                itemId: 'mdc-register-config-preview-fractionDigits'
                            },
                            {
                                fieldLabel: Uni.I18n.translate('registerConfig.useMultiplier', 'MDC', 'Use multiplier'),
                                name: 'useMultiplier',
                                renderer: function (value) {
                                    return value
                                        ? Uni.I18n.translate('general.yes', 'MDC', 'Yes')
                                        : Uni.I18n.translate('general.no', 'MDC', 'No')
                                }
                            },
                            {
                                xtype: 'reading-type-displayfield',
                                fieldLabel: Uni.I18n.translate('general.collectedReadingType', 'MDC', 'Collected reading type'),
                                name: 'collectedReadingType'
                            },
                            {
                                xtype: 'reading-type-displayfield',
                                fieldLabel: Uni.I18n.translate('general.calculatedReadingType', 'MDC', 'Calculated reading type'),
                                name: 'calculatedReadingType',
                                itemId: 'mdc-register-config-preview-calculated',
                                hidden: true
                            }
                        ]
                    }
                ]
            }
        ];

        this.callParent(arguments);
    },

    updateRegisterConfig: function (registerConfig) {
        var me = this;

        if (me.rendered) {
            Ext.suspendLayouts();
        }

        me.loadRecord(registerConfig);
        me.setTitle(registerConfig.get('registerTypeName'));

        if (me.rendered) {
            Ext.resumeLayouts(true);
        }
    }
});
