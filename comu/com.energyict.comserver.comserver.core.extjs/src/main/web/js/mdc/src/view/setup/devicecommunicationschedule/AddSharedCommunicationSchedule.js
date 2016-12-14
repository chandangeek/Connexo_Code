Ext.define('Mdc.view.setup.devicecommunicationschedule.AddSharedCommunicationSchedule', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.addSharedCommunicationSchedule',
    itemId: 'addSharedCommunicationSchedule',
    requires: [
        'Mdc.view.setup.devicecommunicationschedule.SharedCommunicationScheduleSelectionGrid',
        'Mdc.view.setup.devicecommunicationschedule.SharedCommunicationSchedulePreview',
        'Uni.util.FormInfoMessage',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
    store: null,

    side: [],
    deviceName: undefined,

    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('deviceCommunicationSchedule.addCommunicationSchedules', 'MDC', 'Add shared communication schedules'),
                items: [
                    {
                        xtype: 'uni-form-info-message',
                        text: Uni.I18n.translate('deviceSharedCommunicationSchedules.empty.list.item2', 'MDC',
                            "Shared communication schedules with a communication task that is already scheduled with a shared communication schedule on this device aren't included in the list."),
                        hidden: false
                    },
                    {
                        itemId: 'form-errors-shared-schedules',
                        xtype: 'uni-form-error-message',
                        name: 'form-errors',
                        hidden: true,
                        width: 380
                    },
                    {
                        xtype: 'preview-container',
                        selectByDefault: false,
                        grid: {
                            xtype: 'sharedCommunicationScheduleSelectionGrid',
                            store: me.store
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'no-shared-communication-schedules',
                            layout: {
                                type: 'vbox',
                                align: 'stretch'
                            },
                            title: Uni.I18n.translate('deviceSharedCommunicationSchedules.empty.title', 'MDC', 'No shared communication schedules found'),
                            reasons: [
                                Uni.I18n.translate('deviceSharedCommunicationSchedules.empty.list.item1', 'MDC', 'No shared communication schedules with communication tasks on this device have been added yet.'),
                                Uni.I18n.translate('deviceSharedCommunicationSchedules.empty.list.item2', 'MDC',
                                    "Shared communication schedules with a communication task that is already scheduled with a shared communication schedule on this device aren't included in the list.")
                            ],
                            stepsText: '',
                            stepItems: [
                                {
                                    xtype: 'button',
                                    ui: 'link',
                                    text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                    href: '#/devices/' + encodeURIComponent(this.deviceName) + '/communicationplanning'
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'sharedCommunicationSchedulePreview',
                            itemId: 'sharedCommunicationSchedulePreview'
                        }
                    },
                    {
                        xtype: 'component',
                        itemId: 'warningMessageSchedules',
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



