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
                        return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                    }
                },
                {
                    xtype: 'displayfield',
                    name: 'details',
                    fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.description', 'MDC', 'Description'),
                    renderer: function(value,field){
                        if(!value){
                            field.hide();
                        } else {
                            field.show();
                            return Ext.String.htmlEncode(value);
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
                    fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.details', 'MDC', 'Details'),
                    renderer: function(value,field){
                        if(!value){
                            field.hide();
                        } else {
                            field.show();
                            return Ext.String.htmlEncode(value);
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




