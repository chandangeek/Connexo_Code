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
                fieldLabel: Uni.I18n.translate('device.registerData.measurementPeriod', 'MDC', 'Measurement period'),
                labelWidth: 200,
                name: 'interval',
                renderer: function (value) {
                    var startDate,endDate;
                    if (!Ext.isEmpty(value) && !!value.start) {
                        startDate = new Date(value.start);
                        endDate = new Date(value.end);
                        return Uni.DateTime.formatDateTimeShort(startDate) + ' - ' + Uni.DateTime.formatDateTimeShort(endDate);
                    } else if (!Ext.isEmpty(value) && !!value.end){
                        endDate = new Date(value.end);
                        return Uni.DateTime.formatDateTimeShort(endDate)
                    }
                    return '-';
                }
            },
            {
                fieldLabel: Uni.I18n.translate('device.registerData.eventTime', 'MDC', 'Event time'),
                name: 'eventDate',
                renderer: me.renderDateTimeLong
            },
            {
                fieldLabel: Uni.I18n.translate('device.registerData.eventTime', 'MDC', 'Event time'),
                name: 'eventDate',
                renderer: me.renderDateTimeLong
            },{
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('device.registerData.collectedValue', 'MDC', 'Collected value'),
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
                            var value = record.get('value');
                            if (Ext.isEmpty(value)) {
                                return '-';
                            }
                            var unit = record.get('unit');
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
                xtype: 'fieldcontainer',
                itemId: 'mdc-calculated-value-field',
                fieldLabel: Uni.I18n.translate('general.calculatedValue', 'MDC', 'Calculated'),
                layout: {
                    type: 'hbox'
                },
                items: [
                    {
                        xtype: 'displayfield',
                        margin: '0 10 0 0',
                        name: 'calculatedValue',
                        renderer: function (value) {
                            var record = this.up('form').getRecord();
                            if (record && value) {
                                var unit = record.get('calculatedUnit');
                                return Uni.Number.formatNumber(value, -1) + ' ' + (unit ? unit : '');
                            } else {
                                return '-'
                            }
                        }
                    },
                    {
                        xtype: 'edited-displayfield',
                        name: 'calculatedModificationState'
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
                        return Ext.String.htmlEncode(value);
                    } else {
                        return null
                    }
                }
            },
            {
                fieldLabel: Uni.I18n.translate('device.registerData.lastUpdated', 'MDC', 'Last updated'),
                name: 'reportedDateTime',
                renderer: function(value){
                    if(value) {
                        var date = new Date(value);
                        return Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}', [Uni.DateTime.formatDateShort(date), Uni.DateTime.formatTimeShort(date)]);
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