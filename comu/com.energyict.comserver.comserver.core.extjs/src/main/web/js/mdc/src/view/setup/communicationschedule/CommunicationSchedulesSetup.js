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
            title: Uni.I18n.translate('communicationschedule.communicationSchedules', 'MDC', 'Shared communication schedules'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'communicationSchedulesGrid',
                        itemId: 'communicationSchedulesGrid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'no-shared-communication-schedule',
                        title: Uni.I18n.translate('communicationschedule.empty.title', 'MDC', 'No shared communication schedules found'),
                        reasons: [
                            Uni.I18n.translate('communicationschedule.empty.list.item1', 'MDC', 'No shared communication schedules have been created yet.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('communicationschedule.add', 'MDC', 'Add shared communication schedule'),
                                privileges: Mdc.privileges.CommunicationSchedule.admin,
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

    initComponent: function () {
        this.callParent(arguments);
    }
});


