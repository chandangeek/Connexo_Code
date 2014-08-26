Ext.define('Mdc.view.setup.deviceregisterdata.flags.Preview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceregisterreportpreview-flags',
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
                        fieldLabel: Uni.I18n.translate('device.registerData.value', 'MDC', 'Value'),
                        name: 'value'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('device.registerData.validationStatus', 'MDC', 'Validation status'),
                        name: 'validationStatus',
                        renderer: function (value, metaData, record) {
                            return Uni.I18n.translate(value, 'MDC', value)
                        }
                    }
                ]
            }
        ]
    }
});