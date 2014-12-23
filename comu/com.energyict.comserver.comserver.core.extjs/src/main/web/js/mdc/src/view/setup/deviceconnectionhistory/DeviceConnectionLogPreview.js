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
                    name: 'timestamp',
                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.timeStamp', 'MDC', 'Timestamp'),
                    itemId: 'timestamp',
                    renderer: function (value) {
                        if (value) {
                            return Uni.DateTime.formatDateTimeLong(new Date(value));
                        }
                    }
                },
                {
                    xtype: 'displayfield',
                    name: 'details',
                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.descriiption', 'MDC', 'Description'),
                    itemId: 'details',
                    renderer: function(value,field){
                        if(!value){
                            field.hide();
                        } else {
                            field.show();
                            return value;
                        }
                    }
                },
                {
                    xtype: 'displayfield',
                    name: 'errorDetails',
                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.details', 'MDC', 'Details'),
                    itemId: 'error',
                    renderer: function(value,field){
                        if(!value){
                            field.hide();
                        } else {
                            field.show();
                            return value;
                        }
                    }
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




