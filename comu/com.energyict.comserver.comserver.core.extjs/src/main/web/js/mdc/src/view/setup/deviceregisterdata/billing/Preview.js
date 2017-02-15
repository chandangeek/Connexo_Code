/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceregisterdata.billing.Preview', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainPreview',
    alias: 'widget.deviceregisterreportpreview-billing',
    requires: [
        'Mdc.view.setup.deviceregisterdata.ValidationPreview'
    ],
    itemId: 'deviceregisterreportpreview',
    title: '',

    getGeneralItems: function () {
        var me = this;
        return [
            {
                fieldLabel: Uni.I18n.translate('device.registerData.measurementTime', 'MDC', 'Measurement time'),
                name: 'timeStamp',
                renderer: me.renderDateTimeLong
            },
            {
                fieldLabel: Uni.I18n.translate('device.registerData.readingTime', 'MDC', 'Reading time'),
                name: 'reportedDateTime',
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
                        return Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}', [Uni.DateTime.formatDateLong(startDate), Uni.DateTime.formatTimeLong(startDate)])
                            + ' - '
                            + Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}', [Uni.DateTime.formatDateLong(endDate), Uni.DateTime.formatTimeLong(endDate)])
                    }
                }
            },
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
            },
            {
                fieldLabel: Uni.I18n.translate('device.registerData.deltaValue', 'MDC', 'Delta value'),
                name: 'deltaValue',
                renderer: function (value) {
                    var form = this.up('form'),
                        record = form.getRecord();
                    if (record && value) {
                        return Ext.String.htmlEncode(value) + ' ' + record.get('unit');
                    } else {
                        return null
                    }
                }
            }
        ];
    },

    getValidationItems: function () {
        return [
            {
                xtype: 'deviceregisterreportpreview-validation',
                router: this.router,
                fieldLabel: ''
            }
        ];
    }

});