Ext.define('Mdc.view.setup.deviceregisterdata.numerical.Preview', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainPreview',
    alias: 'widget.deviceregisterreportpreview-numerical',
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
                                return Uni.I18n.translate('general.dateattime', 'MDC', '{0} At {1}',[ Uni.DateTime.formatDateLong(new Date(value)),Uni.DateTime.formatTimeLong(new Date(value))]).toLowerCase();
                            }
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('device.registerData.readingTime', 'MDC', 'Reading time'),
                        name: 'reportedDateTime',
                        renderer: function (value) {
                            if (!Ext.isEmpty(value)) {
                                return Uni.I18n.translate('general.dateattime', 'MDC', '{0} At {1}',[ Uni.DateTime.formatDateLong(new Date(value)),Uni.DateTime.formatTimeLong(new Date(value))]).toLowerCase();
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
                                        return Uni.Number.formatNumber(value, -1) + ' ' + record.get('unitOfMeasure');
                                    } else {
                                        return '-'
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
                                return Uni.Number.formatNumber(value, -1) + ' ' + record.get('unitOfMeasure');
                            } else {
                                return '-'
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