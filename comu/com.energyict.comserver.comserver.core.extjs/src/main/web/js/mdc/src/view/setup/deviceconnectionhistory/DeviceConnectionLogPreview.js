Ext.define('Mdc.view.setup.deviceconnectionhistory.DeviceConnectionLogPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.deviceConnectionLogPreview',
    itemId: 'deviceConnectionLogPreview',
    requires: [

    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    title: 'Details',

    items: [

        {
            xtype: 'form',
            border: false,
            itemId: 'deviceConnectionLogPreviewForm',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'displayfield',
                    name: 'timeStamp',
                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.timeStamp', 'MDC', 'TimeStamp'),
                    itemId: 'timeStamp',
                    renderer: function (value) {
                        if (value) {
                            return new Date(value).toLocaleString();
                        }
                    }
                },
                {
                    xtype: 'displayfield',
                    name: 'details',
                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.details', 'MDC', 'Details'),
                    itemId: 'details'
                },
                {
                    xtype: 'displayfield',
                    name: 'logLevel',
                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.logLevel', 'MDC', 'Log level'),
                    itemId: 'logLevel'
                }
            ]
        }
    ],


    initComponent: function () {
        this.callParent(arguments);
    }
});




