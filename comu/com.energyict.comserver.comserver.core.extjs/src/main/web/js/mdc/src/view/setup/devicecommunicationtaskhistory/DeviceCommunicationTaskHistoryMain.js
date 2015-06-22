Ext.define('Mdc.view.setup.devicecommunicationtaskhistory.DeviceCommunicationTaskHistoryMain', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceCommunicationTaskHistoryMain',
    itemId: 'deviceCommunicationTaskHistoryMain',

    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        device: me.device,
                        toggleId: 'communicationTasksLink'
                    }
                ]
            }
        ];

        me.content = [
            {
                ui: 'large',
                xtype: 'panel',
                itemId: 'deviceConnectionHistoryPanel',
                title: Ext.String.format(Uni.I18n.translate('devicecommunicationtaskhistory.deviceCommunicationTaskHistory', 'MDC', 'History of \'{0}\''), this.comTaskName),
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'deviceCommunicationTaskHistoryGrid',
                            mRID: me.mrid,
                            comTaskId: me.comTaskId
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

        me.callParent(arguments);
    }
});
