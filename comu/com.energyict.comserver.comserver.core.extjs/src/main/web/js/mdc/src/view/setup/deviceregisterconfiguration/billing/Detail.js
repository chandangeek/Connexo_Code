/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceregisterconfiguration.billing.Detail', {
    extend: 'Mdc.view.setup.deviceregisterconfiguration.GeneralDetail',
    alias: 'widget.deviceRegisterConfigurationDetail-billing',
    itemId: 'deviceRegisterConfigurationDetail',

    requires: [
        'Mdc.view.setup.deviceregisterconfiguration.ActionMenu',
        'Uni.form.field.ReadingTypeDisplay',
        'Uni.form.field.ObisDisplay',
        'Mdc.view.setup.deviceregisterconfiguration.DataLoggerSlaveHistory'
    ],
    showDataLoggerSlaveField: false,
    showDataLoggerSlaveHistory: false,
    dataLoggerSlaveHistoryStore: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'form',
                border: false,
                itemId: 'deviceRegisterConfigurationDetailForm',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'container',
                        layout: {
                            type: 'hbox'
                        },
                        flex: 1,
                        items: [
                            {
                                xtype:'fieldcontainer',
                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.general', 'MDC', 'General'),
                                labelAlign: 'top',
                                layout: 'vbox',
                                defaults: {
                                    xtype: 'displayfield',
                                    labelWidth: 250
                                },
                                items: [
                                    {
                                        xtype: 'reading-type-displayfield',
                                        name: 'readingType'
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
                                        xtype: 'displayfield',
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
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.interval', 'MDC', 'Interval'),
                                        name: 'interval',
                                        renderer: function (value) {
                                            return Ext.isEmpty(value)
                                                ? '-'
                                                : Uni.DateTime.formatDateTimeShort(new Date(value.start)) + ' - ' + Uni.DateTime.formatDateTimeShort(new Date(value.end));
                                        }
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.numberOfFractionDigits', 'MDC', 'Number of fraction digits'),
                                        name: 'overruledNumberOfFractionDigits'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('channelConfig.useMultiplier', 'MDC', 'Use multiplier'),
                                        name: 'useMultiplier',
                                        renderer: function(value) {
                                            return value
                                                ? Uni.I18n.translate('general.yes', 'MDC', 'Yes')
                                                : Uni.I18n.translate('general.no', 'MDC', 'No');
                                        }
                                    },
                                    {
                                        xtype: 'deviceregisterdetailspreview-validation',
                                        inputLabelWidth: 250,
                                        router: me.router
                                    }
                                ]
                            },
                            {
                                xtype: 'custom-attribute-sets-placeholder-form',
                                itemId: 'custom-attribute-sets-placeholder-form-id',
                                actionMenuXtype: 'deviceRegisterConfigurationActionMenu',
                                attributeSetType: 'register',
                                router: me.router
                            },
                            {
                                xtype: 'uni-button-action',
                                itemId: 'detailActionMenu',
                                menu: {
                                    xtype: 'deviceRegisterConfigurationActionMenu'
                                }
                            }
                        ]
                    }
                ]
            }
        ];

        if (me.showDataLoggerSlaveHistory) {
            me.on('afterrender', function() {
                me.down('#deviceRegisterConfigurationDetailForm').add(
                    {
                        xtype: 'dataLogger-slaveRegisterHistory',
                        dataLoggerSlaveHistoryStore: me.dataLoggerSlaveHistoryStore
                    }
                );
            }, me, {single:true});
        }

        me.callParent(arguments);
    }
});