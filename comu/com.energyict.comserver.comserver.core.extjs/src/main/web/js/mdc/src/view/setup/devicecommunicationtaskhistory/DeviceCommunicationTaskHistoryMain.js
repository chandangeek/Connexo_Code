Ext.define('Mdc.view.setup.devicecommunicationtaskhistory.DeviceCommunicationTaskHistoryMain', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceCommunicationTaskHistoryMain',
    itemId: 'deviceCommunicationTaskHistoryMain',

    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.util.FormEmptyMessage'
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
                title: Uni.I18n.translate('devicecommunicationtaskhistory.deviceCommunicationTaskHistory', 'MDC', "History of '{0}'", this.comTaskName, false),
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'deviceCommunicationTaskHistoryGrid',
                            deviceId: me.deviceId,
                            comTaskId: me.comTaskId
                        },
                        emptyComponent: {
                            xtype: 'uni-form-empty-message',
                            text: Uni.I18n.translate('devicecommunicationtaskhistory.empty', 'MDC', 'The communication task has never been executed on the device')
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
