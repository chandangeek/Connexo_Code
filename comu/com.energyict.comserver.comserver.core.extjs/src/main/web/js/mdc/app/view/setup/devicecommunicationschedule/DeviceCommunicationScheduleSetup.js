Ext.define('Mdc.view.setup.devicecommunicationschedule.DeviceCommunicationScheduleSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceCommunicationScheduleSetup',
    itemId: 'deviceCommunicationScheduleSetup',

    requires: [
        'Uni.view.navigation.SubMenu',
        'Mdc.view.setup.device.DeviceMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    initComponent: function () {
        this.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        mRID: this.mrid,
                        toggle: 7
                    }
                ]
            }
        ];

        this.content = [
            {
                ui: 'large',
                xtype: 'panel',
                title: Uni.I18n.translate('deviceCommunicationSchedule.communicationSchedules', 'MDC', 'Communication schedules'),
                items: [
                    {
                        xtype: 'panel',
                        title: Uni.I18n.translate('deviceCommunicationSchedule.sharedCommunicationSchedules', 'MDC', 'Shared communication schedules'),
                        ui: 'medium',
                        header:{
                            titlePosition: 0,
                            items:[{
                                xtype:'button',
                                text: Uni.I18n.translate('deviceCommunicationSchedule.addSharedCommunicationSchedule', 'MDC', 'Add shared communication schedule'),
                                ui: 'action',
                                action: 'addSharedCommunicationSchedule',
                                itemId: 'addSharedCommunicationScheduleButton'
                            }]
                        },
                        items: [
                            {
                                xtype: 'contentcontainer',
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
                        ui: 'medium',
                        items: [
                            {
                                xtype: 'contentcontainer',
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                itemId: 'individualDeviceCommunicationScheduleSetupPanel'
                            }
                        ]
                    },
                    {
                        xtype: 'panel',
                        title: Uni.I18n.translate('deviceCommunicationSchedule.onRequestCommunicationSchedules', 'MDC', 'Only on request communication schedules'),
                        ui: 'medium',
                        items: [
                            {
                                xtype: 'contentcontainer',
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


