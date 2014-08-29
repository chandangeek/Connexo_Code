Ext.define('Mdc.view.setup.deviceregisterdata.numerical.Preview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceregisterreportpreview-numerical',
    itemId: 'deviceregisterreportpreview',
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
        defaults: {
            xtype: 'container',
            layout: 'form'
        },
        items: [
            {
                xtype:'fieldcontainer',
                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.general', 'MDC', 'General'),
                labelAlign: 'top',
                layout: 'vbox',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('device.registerData.measurementTime', 'MDC', 'Measurement time'),
                        name: 'timeStamp',
                        format: 'M j, Y \\a\\t G:i',
                        renderer: function (value) {
                                return Ext.util.Format.date(value, this.format);
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('device.registerData.readingtTime', 'MDC', 'Reading time'),
                        name: 'reportedDateTime',
                        format: 'M j, Y \\a\\t G:i',
                        renderer: function (value) {
                            return Ext.util.Format.date(value, this.format);
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('device.registerData.value', 'MDC', 'Value'),
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
                        fieldLabel: Uni.I18n.translate('device.registerData.multiplier', 'MDC', 'Multiplier'),
                        name: 'multiplier'
                    }
                ]
            }
        ]
    }
});