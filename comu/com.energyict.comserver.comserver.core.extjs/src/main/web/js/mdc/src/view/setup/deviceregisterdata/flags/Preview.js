Ext.define('Mdc.view.setup.deviceregisterdata.flags.Preview', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainPreview',
    alias: 'widget.deviceregisterreportpreview-flags',
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
                        fieldLabel: Uni.I18n.translate('device.registerData.value', 'MDC', 'Value'),
                        name: 'value'
                    }
                ]
            }
        ]
    }
});