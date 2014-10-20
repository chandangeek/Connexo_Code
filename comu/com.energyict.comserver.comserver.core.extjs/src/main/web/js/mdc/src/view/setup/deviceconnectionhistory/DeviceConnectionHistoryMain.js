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
        this.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        mRID: this.mrid,
                        toggle: 4
                    }
                ]
            }
        ];

        this.content = [
            {
                ui: 'large',
                xtype: 'panel',
                itemId: 'deviceConnectionHistoryPanel',
                title: Ext.String.format(Uni.I18n.translate('deviceconnectionhistory.connectionHistory', 'MDC', 'History of connection {0}'),this.connectionMethodName),
                items: [
                    {
                        xtype: 'preview-container',
                        //itemId: 'previewContainer',
                        grid: {
                            xtype: 'deviceConnectionHistoryGrid',
                            mrid: this.mrid
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
                                                title: Uni.I18n.translate('deviceconnectionhistory.empty.title', 'MDC', 'No history found')
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