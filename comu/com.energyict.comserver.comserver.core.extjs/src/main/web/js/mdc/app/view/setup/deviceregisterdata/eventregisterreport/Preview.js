Ext.define('Mdc.view.setup.deviceregisterdata.eventregisterreport.Preview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceeventregisterreportpreview',
    itemId: 'deviceEventRegisterReportPreview',
    title: '',
    frame: true,
    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
            iconCls: 'x-uni-action-iconD'
        }
    ],
    items: {
        xtype: 'form',
        layout: 'column',
        defaults: {
            xtype: 'container',
            layout: 'form',
            columnWidth: 0.5
        },
        items: [
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('device.registerData.readingTime', 'MDC', 'Reading time'),
                        name: 'timeStamp',
                        format: 'M j, Y \\a\\t G:i',
                        renderer: function (value) {
                            return Ext.util.Format.date(value, this.format);
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('device.registerData.amount', 'MDC', 'Amount'),
                        name: 'value',
                        renderer: function (value) {
                            var form  = this.up('form'),
                                record = form.getRecord();
                            if (record) {
                                return value + ' ' + record.get('unitOfMeasure');
                            }
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('device.registerData.validationStatus', 'MDC', 'Validation status'),
                        name: 'validationStatus',
                        renderer: function (value, metaData, record) {
                            return Uni.I18n.translate(value, 'MDC', value)
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('device.registerData.multiplier', 'MDC', 'Multiplier'),
                        name: 'multiplier'
                    }
                ]
            },
            {
                defaults: {
                    xtype: 'displayfield'
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('device.registerData.interval', 'MDC', 'Interval'),
                        labelWidth: 200,
                        name: 'interval',
                        renderer: function (value) {
                            if (value) {
                                var startDate = new Date(value.start),
                                    endDate = new Date(value.end),
                                    format = 'M j, Y \\a\\t G:i';
                                return Ext.util.Format.date(startDate, format) + ' - ' + Ext.util.Format.date(endDate, format);
                            }
                        }
                    }
                ]
            }
        ]
    }
});