Ext.define('Mdc.view.setup.deviceregisterdata.text.Preview', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainPreview',
    alias: 'widget.deviceregisterreportpreview-text',
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
                                return Uni.DateTime.formatDateLong(new Date(value))
                                    + ' ' + Uni.I18n.translate('general.at', 'MDC', 'At').toLowerCase() + ' '
                                    + Uni.DateTime.formatTimeLong(new Date(value));
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
                                name: 'value'
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
});