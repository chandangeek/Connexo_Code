Ext.define('Mdc.view.setup.devicecommunicationtaskhistory.DeviceCommunicationTaskHistoryLogPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.deviceCommunicationTaskHistoryLogPreview',
    itemId: 'deviceCommunicationTaskHistoryLogPreview',
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
            itemId: 'DeviceCommunicationTaskHistoryLogPreviewForm',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'displayfield',
                    name: 'timestamp',
                    fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.timeStamp', 'MDC', 'Timestamp'),
                    renderer: function (value) {
                        if (value) {
                            return new Date(value).toLocaleString();
                        }
                    }
                },
                {
                    xtype: 'displayfield',
                    name: 'details',
                    fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.details', 'MDC', 'Details'),
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
                    fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.logLevel', 'MDC', 'Log level')
                },
                {
                    xtype: 'displayfield',
                    name: 'errorDetails',
                    fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.errorDetails', 'MDC', 'Error'),
                    renderer: function(value,field){
                        if(!value){
                            field.hide();
                        } else {
                            field.show();
                            return value;
                        }
                    }
                }
            ]
        }
    ],


    initComponent: function () {
        this.callParent(arguments);
    }
});




