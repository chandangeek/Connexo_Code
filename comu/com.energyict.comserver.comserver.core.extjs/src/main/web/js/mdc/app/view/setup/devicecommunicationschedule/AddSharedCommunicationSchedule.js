Ext.define('Mdc.view.setup.devicecommunicationschedule.AddSharedCommunicationSchedule', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.addSharedCommunicationSchedule',
    itemId: 'AddSharedCommunicationSchedule',
    requires: [
        'Mdc.view.setup.devicecommunicationschedule.AddScharedCommunicationScheduleGrid',
        'Mdc.view.setup.devicecommunicationschedule.AddSharedCommunicationSchedulePreview',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
    store: 'Mdc.store.AvailableCommunicationSchedulesForDevice',
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('deviceCommunicationSchedule.addCommunicationSchedules', 'MDC', 'Add shared communication schedules'),
            items: [
                {
                    xtype: 'component',
                    itemId: 'overlapWarning',
                    html: '<span style="color:red">'+ Uni.I18n.translate('deviceCommunicationSchedule.ComTaskOverlap', 'MDC', 'The current selection has overlapping communication tasks.') +'</span>',
                    hidden: true
                },
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'addSharedCommunicationScheduleGrid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('deviceCommunicationSchedule.empty.title', 'MDC', 'No communication schedules found'),
                        reasons: [
                            Uni.I18n.translate('deviceCommunicationSchedule.empty.list.item1', 'MDC', 'No communication schedules have been created yet.')
                        ]
                    },
                    previewComponent: {
                        xtype: 'addSharedCommunicationSchedulePreview'
                    }

                },
                {
                    xtype: 'form',
                    border: false,
                    itemId: 'addSharedScheduleButtonForm',
                    layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },
                    width: '100%',
                    defaults: {
                        labelWidth: 250
                    },
                    items: [
                        {
                            xtype: 'toolbar',
                            fieldLabel: '&nbsp',
                            layout: {
                                type: 'hbox',
                                align: 'stretch'
                            },
                            width: '100%',
                            items: [
                                {
                                    text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                                    xtype: 'button',
                                    action: 'addAction',
                                    itemId: 'addButton',
                                    ui: 'action',
                                    disabled: true
                                },
                                {
                                    xtype: 'button',
                                    itemId: 'cancelLink',
                                    action: 'cancelAction',
                                    ui: 'link',
                                    text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel')
                                }
                            ]
                        }
                    ]
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



