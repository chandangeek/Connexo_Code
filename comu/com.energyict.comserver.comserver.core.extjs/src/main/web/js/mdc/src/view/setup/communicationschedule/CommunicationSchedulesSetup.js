Ext.define('Mdc.view.setup.communicationschedule.CommunicationSchedulesSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.communicationSchedulesSetup',
    itemId: 'CommunicationSchedulesSetup',
    requires: [
        'Mdc.view.setup.devicetype.DeviceTypesGrid',
        'Mdc.view.setup.devicetype.DeviceTypePreview',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('communicationschedule.communicationSchedules', 'MDC', 'Communication schedules'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'communicationSchedulesGrid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('communicationschedule.empty.title', 'MDC', 'No communication schedules found'),
                        reasons: [
                            Uni.I18n.translate('communicationschedule.empty.list.item1', 'MDC', 'No communication schedules have been created yet.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('communicationschedule.add', 'MDC', 'Add communication schedule'),
                                action: 'createCommunicationSchedule'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'communicationSchedulePreview'
                    }
                }
            ]
        }
    ],

    side: [

    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});


