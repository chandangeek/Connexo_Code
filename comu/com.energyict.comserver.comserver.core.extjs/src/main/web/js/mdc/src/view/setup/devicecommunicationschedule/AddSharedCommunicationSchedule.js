Ext.define('Mdc.view.setup.devicecommunicationschedule.AddSharedCommunicationSchedule', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.addSharedCommunicationSchedule',
    itemId: 'AddSharedCommunicationSchedule',
    requires: [
        'Mdc.view.setup.devicecommunicationschedule.AddSharedCommunicationScheduleGrid',
        'Mdc.view.setup.devicecommunicationschedule.AddSharedCommunicationSchedulePreview',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
    store: 'Mdc.store.AvailableCommunicationSchedulesForDevice',

    side: [],

    initComponent: function () {
        this.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('deviceCommunicationSchedule.addCommunicationSchedules', 'MDC', 'Add shared communication schedules'),
                items: [
                    {
                        itemId: 'form-errors',
                        xtype: 'uni-form-error-message',
                        name: 'form-errors',
                        hidden: true,
                        width: 380
                    },
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'addSharedCommunicationScheduleGrid'
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'no-shared-communication-schedules',
                            layout: {
                                type: 'vbox',
                                align: 'stretch'
                            },
                            title: Uni.I18n.translate('deviceCommunicationSchedule.empty.title', 'MDC', 'No shared communication schedules found'),
                            reasons: [
                                Uni.I18n.translate('deviceCommunicationSchedule.empty.list.item1', 'MDC', 'No shared communication schedules have been added yet'),
                                Uni.I18n.translate('deviceCommunicationSchedule.empty.list.item2', 'MDC', 'There are no communication tasks on the device/device config'),
                                Uni.I18n.translate('deviceCommunicationSchedule.empty.list.item3', 'MDC', 'There are shared communication schedules defined in administration but a mismatch between device configuration and the communication schedule (one or more communication tasks defined in the shared communication schedule is not available on the device configuration of the device)'),
                                Uni.I18n.translate('deviceCommunicationSchedule.empty.list.item4', 'MDC', 'There are shared communication schedules defined in administration but one or more communication tasks in the communication schedule are already scheduled on the device with a shared communication schedule (a communication task on device level can only be scheduled in maximum one communication schedule)'),
                                Uni.I18n.translate('deviceCommunicationSchedule.empty.list.item4', 'MDC', 'There are shared communication schedules defined in administration but the communication tasks in the communication schedule doesn\'t have the same connection method, security set, protocol dialect and/or urgency')
                            ],
                            stepsText: '',
                            stepItems: [
                                {
                                    xtype: 'button',
                                    ui: 'link',
                                    text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                    href: '#/devices/' + encodeURIComponent(this.mRID) + '/communicationplanning'
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'addSharedCommunicationSchedulePreview'
                        }

                    },
                    {
                        xtype: 'component',
                        itemId: 'warningMessage',
                        html: '',
                        hidden: true
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
                                        ui: 'action'
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
        ];
        this.callParent(arguments);
    }
});



