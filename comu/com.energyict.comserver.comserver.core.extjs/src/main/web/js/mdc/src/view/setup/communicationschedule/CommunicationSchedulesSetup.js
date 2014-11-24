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
                        xtype: 'communicationSchedulesGrid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('communicationschedule.empty.title', 'MDC', 'No shared communication schedules found'),
                        reasons: [
                            Uni.I18n.translate('communicationschedule.empty.list.item1', 'MDC', 'No shared communication schedules have been created yet.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('communicationschedule.add', 'MDC', 'Add shared communication schedule'),
                                privileges: ['privilege.administrate.schedule'],
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


