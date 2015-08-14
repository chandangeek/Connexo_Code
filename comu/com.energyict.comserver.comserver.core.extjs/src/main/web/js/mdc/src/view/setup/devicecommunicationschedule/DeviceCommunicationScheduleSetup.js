Ext.define('Mdc.view.setup.devicecommunicationschedule.DeviceCommunicationScheduleSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceCommunicationScheduleSetup',
    itemId: 'deviceCommunicationScheduleSetup',

    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
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
                        toggleId: 'communicationSchedulesLink'
                    }
                ]
            }
        ];

        this.content = [
            {
                ui: 'large',
                xtype: 'panel',
                cls: 'no-side-padding',
                title: Uni.I18n.translate('deviceCommunicationSchedule.communicationPlanning', 'MDC', 'Communication planning'),
                items: [
                    {
                        xtype: 'panel',
                        title: Uni.I18n.translate('deviceCommunicationSchedule.recurringCommunication', 'MDC', 'Recurring communication'),
                        ui: 'medium',
                        cls: 'no-side-padding',
                        items: [
                            {
                                xtype: 'panel',
                                title: Uni.I18n.translate('general.comSchedules', 'MDC', 'Shared communication schedules'),
                                ui: 'small',
                                cls: 'no-side-padding',
                                header:{
                                    titlePosition: 0,
                                    items:[{
                                        xtype:'button',
                                        text: Uni.I18n.translate('communicationSchedule.add', 'MDC', 'Add shared communication schedule'),
                                        privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                                        ui: 'action',
                                        action: 'addSharedCommunicationSchedule',
                                        itemId: 'addSharedCommunicationScheduleButton'
                                    }]
                                },
                                items: [
                                    {
                                        xtype: 'container',
                                        layout: {
                                            type: 'vbox',
                                            align: 'stretch'
                                        },
                                        itemId: 'sharedDeviceCommunicationScheduleSetupPanel'
                                    }
                                ]
                            },
                            {
                                xtype: 'panel',
                                title: Uni.I18n.translate('deviceCommunicationSchedule.individualCommunicationSchedules', 'MDC', 'Individual communication schedules'),
                                ui: 'small',
                                cls: 'no-side-padding',
                                items: [
                                    {
                                        xtype: 'container',
                                        layout: {
                                            type: 'vbox',
                                            align: 'stretch'
                                        },
                                        itemId: 'individualDeviceCommunicationScheduleSetupPanel'
                                    }
                                ]
                            }
                        ]
                    },

                    {
                        xtype: 'panel',
                        title: Uni.I18n.translate('deviceCommunicationSchedule.nonRecurringCommunication', 'MDC', 'Non recurring communication'),
                        ui: 'medium',
                        cls: 'no-side-padding',
                        items: [
                            {
                                xtype: 'container',
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                itemId: 'onRequestDeviceCommunicationScheduleSetupPanel'
                            }
                        ]
                    }
                ]
            }

        ];
        this.callParent(arguments);
    },

    getEmptyComponent: function () {
//        if (this.isDirectlyAddressable) {
//            return  {
//                xtype: 'no-items-found-panel',
//                title: Uni.I18n.translate('deviceconnectionmethod.empty.title', 'MDC', 'No connection methods found'),
//                reasons: [
//                    Uni.I18n.translate('deviceconnectionmethod.empty.list.item1', 'MDC', 'No connection methods have been defined yet.')
//                ],
//                stepItems: [
//                    {
//                        text: Uni.I18n.translate('deviceconnectionmethod.addOutboundConnectionMethod', 'MDC', 'Add outbound connection method'),
//                        itemId: 'createDeviceOutboundConnectionButton',
//                        action: 'createDeviceOutboundConnectionMethod'
//                    },
//                    {
//                        text: Uni.I18n.translate('deviceconnectionmethod.addInboundConnectionMethod', 'MDC', 'Add inbound connection method'),
//                        itemId: 'createDeviceInboundConnectionButton',
//                        action: 'createDeviceInboundConnectionMethod'
//                    }
//                ]
//            };
//        } else {
//            return  {
//                xtype: 'no-items-found-panel',
//                title: Uni.I18n.translate('deviceconnectionmethod.empty.detailNotAdressable', 'MDC', 'No connection methods can be added'),
//                reasons: [
//                    Uni.I18n.translate('deviceconnectionmethod.empty.list.detailNotAdressableItem1', 'MDC', 'This device configuration is not directly addressable.')
//                ]
//            };
//        }
    }
});


