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
                        format: 'M j, Y \\a\\t G:i',
                        renderer: function (value) {
                            if(!Ext.isEmpty(value)) {
                                return Ext.util.Format.date(new Date(value), this.format);
                            }
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('device.registerData.readingTime', 'MDC', 'Reading time'),
                        name: 'reportedDateTime',
                        format: 'M j, Y \\a\\t G:i',
                        renderer: function (value) {
                            if(!Ext.isEmpty(value)) {
                                return Ext.util.Format.date(value, this.format);
                            }
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('device.registerData.interval', 'MDC', 'Interval'),
                        labelWidth: 200,
                        name: 'interval',
                        renderer: function (value) {
                            if(!Ext.isEmpty(value)) {
                                var startDate = new Date(value.start),
                                    endDate = new Date(value.end),
                                    format = 'M j, Y \\a\\t G:i';
                                return Ext.util.Format.date(startDate, format) + ' - ' + Ext.util.Format.date(endDate, format);
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
                                name: 'editedDateTime'
                            }
                        ]
                    },
                    {
                        fieldLabel: Uni.I18n.translate('device.registerData.multiplier', 'MDC', 'Multiplier'),
                        name: 'multiplier'
                    }
                ]
            },
            {
                xtype: 'deviceregisterreportpreview-validation'
            }
        ]
    }
});