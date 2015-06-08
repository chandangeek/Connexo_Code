Ext.define('Mdc.view.setup.deviceregisterdata.billing.Preview', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainPreview',
    alias: 'widget.deviceregisterreportpreview-billing',
    requires: [
        'Mdc.view.setup.deviceregisterdata.ValidationPreview'
    ],
    itemId: 'deviceregisterreportpreview',
    title: '',

    items: {
        xtype: 'form',
        defaults: {
            xtype: 'container',
            layout: 'form'
        },
        items: [
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.general', 'MDC', 'General'),
                labelAlign: 'top',
                layout: 'vbox',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200,
                    width: 1000
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('device.registerData.measurementTime', 'MDC', 'Measurement time'),
                        name: 'timeStamp',
                        renderer: function (value) {
                            if (!Ext.isEmpty(value)) {
                                return Uni.DateTime.formatDateLong(new Date(value))
                                    + ' ' + Uni.I18n.translate('general.at', 'MDC', 'At').toLowerCase() + ' '
                                    + Uni.DateTime.formatTimeLong(new Date(value));
                            }
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('device.registerData.readingTime', 'MDC', 'Reading time'),
                        name: 'reportedDateTime',
                        renderer: function (value) {
                            if (!Ext.isEmpty(value)) {
                                return Uni.DateTime.formatDateLong(value)
                                    + ' ' + Uni.I18n.translate('general.at', 'MDC', 'At').toLowerCase() + ' '
                                    + Uni.DateTime.formatTimeLong(value);
                            }
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('device.registerData.interval', 'MDC', 'Interval'),
                        labelWidth: 200,
                        name: 'interval',
                        renderer: function (value) {
                            if (!Ext.isEmpty(value)) {
                                var startDate = new Date(value.start),
                                    endDate = new Date(value.end);
                                return Uni.DateTime.formatDateLong(startDate)
                                    + ' ' + Uni.I18n.translate('general.at', 'MDC', 'At').toLowerCase() + ' '
                                    + Uni.DateTime.formatTimeLong(startDate)
                                    + ' - '
                                    + Uni.DateTime.formatDateLong(endDate)
                                    + ' ' + Uni.I18n.translate('general.at', 'MDC', 'At').toLowerCase() + ' '
                                    + Uni.DateTime.formatTimeLong(endDate);
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
                                renderer: function (value) {
                                    var form = this.up('form'),
                                        record = form.getRecord();
                                    if (record && value) {
                                        return value + ' ' + record.get('unitOfMeasure');
                                    } else {
                                        return null
                                    }

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
                                return Ext.String.htmlEncode(value) + ' ' + record.get('unitOfMeasure');
                            } else {
                                return null
                            }
                        }
                    }
                ]
            },
            {
                xtype: 'deviceregisterreportpreview-validation'
            }
        ]
    }
});