/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceconnectionhistory.DeviceConnectionLogPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.deviceConnectionLogPreview',
    itemId: 'deviceConnectionLogPreview',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    title: Uni.I18n.translate('general.details','MDC','Details'),

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
                    name: 'details',
                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.message', 'MDC', 'Message'),
                    itemId: 'details',
                    htmlEncode: false,
                    renderer: function(value,field){
                        if(!value){
                            field.hide();
                        } else {
                            field.show();
                            return value.replace(/(?:\r\n|\r|\n)/g, '<br />');
                        }
                    }
                },
                {
                    xtype: 'displayfield',
                    name: 'errorDetails',
                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.details', 'MDC', 'Details'),
                    itemId: 'error',
                    htmlEncode: false,
                    renderer: function(value,field){
                        if(!value){
                            field.hide();
                        } else {
                            field.show();
                            return value.replace(/(?:\r\n|\r|\n)/g, '<br />');
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




