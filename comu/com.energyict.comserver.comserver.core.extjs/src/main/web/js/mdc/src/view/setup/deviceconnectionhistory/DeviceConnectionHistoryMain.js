Ext.define('Mdc.view.setup.deviceconnectionhistory.DeviceConnectionHistoryMain', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceConnectionHistoryMain',
    itemId: 'deviceConnectionHistoryMain',

    requires: [
        'Uni.view.navigation.SubMenu',
        'Mdc.view.setup.device.DeviceMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
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
                        device: me.device,
                        toggleId: 'connectionMethodsLink'
                    }
                ]
            }
        ];

        this.content = [
            {
                ui: 'large',
                xtype: 'panel',
                itemId: 'deviceConnectionHistoryPanel',
                title: Ext.String.format(Uni.I18n.translate('deviceconnectionhistory.connectionHistory', 'MDC', 'History of \'{0}\''),this.connectionMethodName),
                items: [
                    {
                        xtype: 'preview-container',
                        //itemId: 'previewContainer',
                        grid: {
                            xtype: 'deviceConnectionHistoryGrid',
                            mRID: this.mrid,
                            connectionId: this.connectionMethodId
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('deviceconnectionhistory.empty.title', 'MDC', 'No history found')
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
                                                mrid: this.mrid
                                            },
                                            emptyComponent: {
                                                xtype: 'no-items-found-panel',
                                                title: Uni.I18n.translate('deviceconnectionhistory.empty.title', 'MDC', 'No history found'),
                                                reasons: [
                                                    Uni.I18n.translate('deviceconnectionhistory.empty.list.item1', 'MDC', 'The communication task has never been executed on the device')
                                                ]
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