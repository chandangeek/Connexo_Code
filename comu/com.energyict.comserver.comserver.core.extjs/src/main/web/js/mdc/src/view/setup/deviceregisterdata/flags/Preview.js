/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceregisterdata.flags.Preview', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainPreview',
    alias: 'widget.deviceregisterreportpreview-flags',
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
            //{
            //    fieldLabel: Uni.I18n.translate('device.registerData.readingTime', 'MDC', 'Reading time'),
            //    name: 'reportedDateTime',
            //    renderer: me.renderDateTimeLong
            //},
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('device.registerData.collectedValue', 'MDC', 'Collected value'),
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
                        return Uni.DateTime.formatDateTimeShort(date);
                    }
                }
            }
        ];
    }

});