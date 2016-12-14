Ext.define('Mdc.view.setup.devicecommunicationschedule.RemoveSharedCommunicationSchedule', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.removeSharedCommunicationSchedule',
    itemId: 'removeSharedCommunicationSchedule',
    requires: [
        'Mdc.view.setup.devicecommunicationschedule.SharedCommunicationScheduleSelectionGrid',
        'Mdc.view.setup.devicecommunicationschedule.SharedCommunicationSchedulePreview',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
    store: null,

    side: [],
    deviceName: undefined,

    initComponent: function () {
        var me = this;
        this.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('deviceCommunicationSchedule.removeCommunicationSchedules', 'MDC', 'Remove shared communication schedules'),
                items: [
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
                                Uni.I18n.translate('deviceSharedCommunicationSchedules.remove.empty.list.item1', 'MDC', 'No communication tasks on this device have been scheduled with a shared communication schedule'),
                            ],
                            stepItems: [
                                {
                                    xtype: 'button',
                                    ui: 'link',
                                    text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                    href: '#/devices/' + encodeURIComponent(me.deviceName) + '/communicationplanning'
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
                        itemId: 'removeSharedScheduleButtonForm',
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
                                        text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                                        xtype: 'button',
                                        action: 'removeAction',
                                        itemId: 'removeButton',
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



