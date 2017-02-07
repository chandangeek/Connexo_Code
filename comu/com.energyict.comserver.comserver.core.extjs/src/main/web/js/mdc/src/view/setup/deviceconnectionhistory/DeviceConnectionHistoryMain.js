/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceconnectionhistory.DeviceConnectionHistoryMain', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceConnectionHistoryMain',
    itemId: 'deviceConnectionHistoryMain',

    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.util.FormEmptyMessage'
    ],

    initComponent: function () {
        var me = this;

        this.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        device: me.device
                    }
                ]
            }
        ];

        this.content = [
            {
                ui: 'large',
                xtype: 'panel',
                itemId: 'deviceConnectionHistoryPanel',
                title: Uni.I18n.translate('deviceconnectionhistory.connectionHistory', 'MDC', "History of '{0}'",[this.connectionMethodName]),
                items: [
                    {
                        xtype: 'preview-container',
                        //itemId: 'previewContainer',
                        grid: {
                            xtype: 'deviceConnectionHistoryGrid',
                            deviceId: this.deviceId,
                            connectionId: this.connectionMethodId
                        },
                        emptyComponent: {
                            xtype: 'uni-form-empty-message',
                            text: Uni.I18n.translate('deviceconnectionhistory.empty', 'MDC', 'The connection method has never been used on the device')
                        },
                        previewComponent: {
                            xtype: 'container',
                            layout: {
                                type: 'vbox',
                                align: 'stretch'

                            },
                            items: [
                                {
                                    xtype: 'deviceConnectionHistoryPreview'
                                },
                                {
                                   xtype: 'panel',
                                    itemId: 'titlePanel',
                                    layout: {
                                        type: 'vbox',
                                       align: 'stretch'
                                    },
                                    ui: 'medium',
                                    padding: '16 0 0 0',
                                    items: [
                                        {
                                            xtype: 'preview-container',

                                            //itemId: 'previewContainer',
                                            grid: {
                                                xtype: 'deviceCommunicationTaskExecutionGrid',
                                                deviceId: this.deviceId
                                            },
                                            emptyComponent: {
                                                xtype: 'uni-form-empty-message',
                                                text: Uni.I18n.translate('devicecommunicationtaskhistory.empty', 'MDC', 'The communication task has never been executed on the device')
                                            },
                                            previewComponent:   {
                                                xtype: 'deviceCommunicationTaskExecutionPreview'
                                            }
                                        }
                                    ]
                                }


                            ]

                        }
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});