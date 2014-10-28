Ext.define('Mdc.view.setup.devicecommunicationtaskhistory.DeviceCommunicationTaskHistoryMain', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceCommunicationTaskHistoryMain',
    itemId: 'deviceCommunicationTaskHistoryMain',

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
                        toggle: 6
                    }
                ]
            }
        ];

        this.content = [
            {
                ui: 'large',
                xtype: 'panel',
                itemId: 'deviceConnectionHistoryPanel',
                title: Ext.String.format(Uni.I18n.translate('devicecommunicationtaskhistory.deviceCommunicationTaskHistory', 'MDC', 'History of communication task \'{0}\''), this.comTaskName),
                items: [
                    {
                        xtype: 'preview-container',
                        //itemId: 'previewContainer',
                        grid: {
                            xtype: 'deviceCommunicationTaskHistoryGrid',
                            mRID: this.mrid,
                            comTaskId: this.comTaskId
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('devicecommunicationtaskhistory.empty.title', 'MDC', 'No communication history found'),
                            reasons: [
                                Uni.I18n.translate('devicecommunicationtaskhistory.empty.reason1', 'MDC', 'The communication task has never been executed on the device')
                            ]
                        },
                        previewComponent: {
                            xtype: 'container',
                            layout: {
                                type: 'vbox',
                                align: 'stretch'

                            },
                            items: [
                                {
                                    xtype: 'deviceCommunicationTaskHistoryPreview'
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
