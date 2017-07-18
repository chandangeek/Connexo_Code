/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceregisterdata.text.HistoryPreview', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainPreview',
    alias: 'widget.preview-device-registers-history-text',
    title: '',

    getGeneralItems: function () {
        var me = this;
        return [
            {
                xtype: 'container',
                layout: {
                    type: 'column',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'container',
                        columnWidth: 0.5,
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 150
                        },
                        items: [
                            {
                                fieldLabel: Uni.I18n.translate('device.registerData.measurementTime', 'MDC', 'Measurement time'),
                                name: 'timeStamp',
                                renderer: me.renderDateTimeLong
                            },
                            {
                                fieldLabel: Uni.I18n.translate('device.registerData.changedOn', 'MDC', 'Changed on'),
                                name: 'reportedDateTime',
                                renderer: me.renderDateTimeLong
                            },
                            {
                                fieldLabel: Uni.I18n.translate('device.registerData.changedBy', 'MDC', 'Changed by'),
                                name: 'userName'
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        columnWidth: 0.5,
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 150
                        },
                        items: [
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
                            }
                        ]
                    }
                ]
            }


        ];
    }
});