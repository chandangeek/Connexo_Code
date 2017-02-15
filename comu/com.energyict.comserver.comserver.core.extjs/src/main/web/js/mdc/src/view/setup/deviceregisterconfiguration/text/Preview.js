/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceregisterconfiguration.text.Preview', {
    extend: 'Mdc.view.setup.deviceregisterconfiguration.GeneralPreview',
    alias: 'widget.deviceRegisterConfigurationPreview-text',
    itemId: 'deviceRegisterConfigurationPreview',

    requires: [
        'Mdc.view.setup.deviceregisterconfiguration.ActionMenu',
        'Uni.form.field.ObisDisplay',
        'Uni.form.field.ReadingTypeDisplay'
    ],
    layout: 'column',
    defaults: {
        columnWidth: 0.5
    },
    router: null,
    showDataLoggerSlaveField: false,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'form',
                itemId: 'deviceRegisterConfigurationPreviewForm',
                layout: 'form',
                items: [
                    {
                        xtype:'fieldcontainer',
                        labelAlign: 'top',
                        layout: 'vbox',
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 200
                        },
                        items: [
                            {
                                xtype: 'reading-type-displayfield',
                                fieldLabel: Uni.I18n.translate('general.collectedReadingType', 'MDC', 'Collected reading type'),
                                name: 'readingType'
                            },
                            {
                                xtype: 'reading-type-displayfield',
                                fieldLabel: Uni.I18n.translate('general.calculatedReadingType', 'MDC', 'Calculated reading type'),
                                name: 'calculatedReadingType',
                                hidden: true
                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.dataLoggerSlave', 'MDC', 'Data logger slave'),
                                name: 'dataloggerSlaveName',
                                hidden: !me.showDataLoggerSlaveField,
                                renderer: function(value) {
                                    if (Ext.isEmpty(value)) {
                                        return '-';
                                    }
                                    var href = me.router.getRoute('devices/device/registers').buildUrl({deviceId: encodeURIComponent(value)});
                                    return '<a href="' + href + '">' + Ext.String.htmlEncode(value) + '</a>'
                                }
                            },
                            {
                                xtype: 'obis-displayfield',
                                name: 'overruledObisCode'
                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.dataUntil', 'MDC', 'Data until'),
                                name: 'timeStamp',
                                renderer: function (value) {
                                    if (value) {
                                        var date = new Date(value);
                                        return Uni.DateTime.formatDateLong(date) + ' - ' + Uni.DateTime.formatTimeShort(date);
                                    }
                                    return '-';
                                }
                            },
                            {
                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.latestValue', 'MDC', 'Latest value'),
                                name: 'value'
                            }
                        ]
                    }
                ]
            },
            {
                xtype: 'custom-attribute-sets-placeholder-form',
                itemId: 'custom-attribute-sets-placeholder-form-id',
                actionMenuXtype: 'deviceRegisterConfigurationActionMenu',
                attributeSetType: 'register',
                router: me.router
            }
        ];

        me.callParent(arguments);
    }
});


