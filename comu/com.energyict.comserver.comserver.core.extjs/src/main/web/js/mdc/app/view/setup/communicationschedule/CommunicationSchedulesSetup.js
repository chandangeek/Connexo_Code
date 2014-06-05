Ext.define('Mdc.view.setup.communicationschedule.CommunicationSchedulesSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.communicationSchedulesSetup',
    itemId: 'CommunicationSchedulesSetup',
    requires: [
        'Mdc.view.setup.devicetype.DeviceTypesGrid',
        'Mdc.view.setup.devicetype.DeviceTypePreview'
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
                        xtype: 'container',
                        layout: {
                            type: 'hbox',
                            align: 'left'
                        },
                        minHeight: 20,
                        items: [
                            {
                                xtype: 'image',
                                margin: '0 10 0 0',
                                src: '../ext/packages/uni-theme-skyline/build/resources/images/shared/icon-info-small.png',
                                height: 20,
                                width: 20
                            },
                            {
                                xtype: 'container',
                                items: [
                                    {
                                        xtype: 'component',
                                        html: '<b>' + Uni.I18n.translate('communicationschedule.empty.title', 'MDC', 'No communication schedules found') + '</b><br>' +
                                            Uni.I18n.translate('communicationschedule.empty.detail', 'MDC', 'There are no communication schedules. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                            Uni.I18n.translate('communicationschedule.empty.list.item1', 'MDC', 'No communication schedules have been created yet.') + '</li></lv><br>' +
                                            Uni.I18n.translate('communicationschedule.empty.steps', 'MDC', 'Possible steps:')
                                    },
                                    {
                                        xtype: 'button',
                                        margin: '10 0 0 0',
                                        text: Uni.I18n.translate('communicationschedule.add', 'MDC', 'Add communication schedule'),
                                        action: 'createDeviceType'
                                    }
                                ]
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


