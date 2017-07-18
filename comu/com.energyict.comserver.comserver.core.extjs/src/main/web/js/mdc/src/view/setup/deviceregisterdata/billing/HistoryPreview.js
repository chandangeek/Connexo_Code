/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceregisterdata.billing.HistoryPreview', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainPreview',
    alias: 'widget.preview-device-registers-history-billing',
    requires: [
        'Mdc.view.setup.deviceregisterdata.HistoryValidationPreview'
    ],
    title: '',

    getGeneralItems: function () {
        var me = this;
        return [
            {
                xtype: 'container',
                layout: {
                    type: 'column',
                    align: 'stretch'
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
                            labelWidth: 150
                        },
                        items: [
                            {
                                fieldLabel: Uni.I18n.translate('device.registerData.measurementTime', 'MDC', 'Measurement time'),
                                name: 'timeStamp',
                                renderer: me.renderDateTimeLong
                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.interval', 'MDC', 'Interval'),
                                labelWidth: 200,
                                name: 'interval',
                                renderer: function (value) {
                                    if (!Ext.isEmpty(value)) {
                                        var startDate = new Date(value.start),
                                            endDate = new Date(value.end);
                                        return Uni.DateTime.formatDateTimeLong(startDate)
                                            + ' - '
                                            + Uni.DateTime.formatDateTimeLong(endDate)
                                    }
                                }
                            },
                            {
                                fieldLabel: Uni.I18n.translate('device.registerData.changedOn', 'MDC', 'Changed on'),
                                name: 'reportedDateTime',
                                renderer: me.renderDateTimeLong
                            },
                            {
                                fieldLabel: Uni.I18n.translate('device.registerData.changedBy', 'MDC', 'Changed by'),
                                name: 'userName'
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
                            labelWidth: 150
                        },
                        items: [
                            {
                                xtype: 'fieldcontainer',
                                fieldLabel: Uni.I18n.translate('device.registerData.value', 'MDC', 'Value'),
                                layout: {
                                    type: 'hbox'
                                },
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        margin: '0 10 0 0',
                                        name: 'value',
                                        renderer: function (v) {
                                            var form = this.up('form'),
                                                record = form.getRecord();
                                            if (Ext.isEmpty(record)) {
                                                return '-';
                                            }
                                            var value = Ext.isEmpty(record.get('value')) ? record.get('calculatedValue') : record.get('value');
                                            if (Ext.isEmpty(value)) {
                                                return '-';
                                            }
                                            var unit = Ext.isEmpty(record.get('unit')) ? record.get('calculatedUnit') : record.get('unit');
                                            return value + ' ' + unit;
                                        }
                                    },
                                    {
                                        xtype: 'edited-displayfield',
                                        name: 'modificationState'
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ];
    },

    getValidationItems: function () {
        return [
            {
                xtype: 'devicehistoryregisterreportpreview-validation',
                router: this.router,
                fieldLabel: ''
            }
        ];
    }

});