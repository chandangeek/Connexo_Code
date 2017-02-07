Ext.define('Mdc.view.setup.deviceregisterdata.text.Preview', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainPreview',
    alias: 'widget.deviceregisterreportpreview-text',
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
    }
});