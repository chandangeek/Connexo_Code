/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('general.collectedValue', 'MDC', 'Collected value'),
                layout: {
                    type: 'hbox',
                    align : 'stretch'
                },
                items: [
                    {
                        xtype: 'displayfield',
                        margin: '0 10 0 0',
                        width: 450,
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