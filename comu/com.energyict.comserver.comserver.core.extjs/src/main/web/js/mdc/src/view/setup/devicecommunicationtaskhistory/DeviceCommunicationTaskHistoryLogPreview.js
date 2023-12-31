/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicecommunicationtaskhistory.DeviceCommunicationTaskHistoryLogPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.deviceCommunicationTaskHistoryLogPreview',
    itemId: 'deviceCommunicationTaskHistoryLogPreview',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    title: Uni.I18n.translate('general.details','MDC','Details'),

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
                    name: 'details',
                    fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.message', 'MDC', 'Message'),
                    htmlEncode: false,
                    renderer: function(value,field){
                        if(!value){
                            field.hide();
                        } else {
                            field.show();
                            return value.replace(/(?:\r\n|\r|\n)/g, '<br>');
                        }
                    }
                },
                {
                    xtype: 'displayfield',
                    name: 'errorDetails',
                    fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.details', 'MDC', 'Details'),
                    htmlEncode: false,
                    renderer: function(value,field){
                        if(!value){
                            field.hide();
                        } else {
                            field.show();
                            return value.replace(/(?:\r\n|\r|\n)/g, '<br>');
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




