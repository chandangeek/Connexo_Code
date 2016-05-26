Ext.define('Mdc.view.setup.devicecommunicationtask.DeviceCommunicationTaskSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceCommunicationTaskSetup',
    itemId: 'deviceCommunicationTaskSetup',

    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.util.FormEmptyMessage',
        'Mdc.view.setup.devicecommunicationtask.DeviceCommunicationTaskGrid',
        'Mdc.view.setup.devicecommunicationtask.DeviceCommunicationTaskPreview'
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
                        toggleId: 'communicationTasksLink'
                    }
                ]
            }
        ];

        this.content = [
            {
                ui: 'large',
                xtype: 'panel',
                itemId: 'deviceCommunicatioTaskSetupPanel',
                title: Uni.I18n.translate('general.communicationTasks', 'MDC', 'Communication tasks'),

                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'previewContainer',
                        grid: {
                            xtype: 'deviceCommunicationTaskGrid'
                        },
                        emptyComponent: this.getEmptyComponent(),
                        previewComponent: {
                            xtype: 'deviceCommunicationTaskPreview'
                        }
                    }
                ]
            }
        ];

        this.callParent(arguments);
    },

    getEmptyComponent: function () {
            return  {
                xtype: 'uni-form-empty-message',
                itemId: 'no-device-communication-tasks',
                text: Uni.I18n.translate('devicecommunicationTask.empty', 'MDC', 'No communication tasks have been added to the device configuration.')
            };
        }
});


