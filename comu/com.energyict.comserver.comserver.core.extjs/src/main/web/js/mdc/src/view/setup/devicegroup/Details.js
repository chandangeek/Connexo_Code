Ext.define('Mdc.view.setup.devicegroup.Details', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'device-groups-details',
    requires: [
        'Mdc.view.setup.devicegroup.Menu',
        'Mdc.view.setup.devicegroup.DeviceGroupPreview',
        'Mdc.view.setup.devicegroup.DeviceGroupActionMenu',
        'Mdc.view.setup.devicegroup.PreviewForm',
        'Mdc.view.setup.devicegroup.DevicesOfDeviceGroupGrid'
    ],

    router: null,
    deviceGroupId: null,

    content: {

        xtype: 'container',
        layout: {
            type: 'vbox',
            align: 'stretch'
        },

        items: [
            {
                xtype: 'container',
                layout: 'hbox',
                items: [
                    {
                        ui: 'large',
                        title: Uni.I18n.translate('general.overview', 'MDC', 'Overview'),
                        flex: 1,
                        items: {
                            xtype: 'devicegroups-preview-form',
                            itemId: 'deviceGroupdetailsForm',
                            deviceGroupId: this.deviceGroupId
                        }
                    },
                    {
                        xtype: 'button',
                        itemId: 'deviceGroupDetailsActionMenu',
                        text: Uni.I18n.translate('general.actions', 'DES', 'Actions'),
                        iconCls: 'x-uni-action-iconD',
                        margin: '20 0 0 0',
                        menu: {
                            xtype: 'device-group-action-menu'
                        }
                    }
                ]
            },
            {
                xtype: 'preview-container',
                grid: {
                    xtype: 'devicesOfDeviceGroupGrid'/*,
                    groupId: this.deviceGroupId*/
                },
                emptyComponent: {
                    xtype: 'no-items-found-panel',
                    title: Uni.I18n.translate('devicesearch.empty.title', 'MDC', 'No devices found'),
                    reasons: [
                        Uni.I18n.translate('devicegroup.empty.list.message', 'MDC', 'There are no devices in your group.')
                    ]
                }
            }
        ]
    },


    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                title: Uni.I18n.translate('general.deviceGroups', 'DES', 'Device groups'),
                ui: 'medium',
                items: [
                    {
                        xtype: 'devicegroups-menu',
                        itemId: 'devicegroups-view-menu',
                        router: me.router,
                        deviceGroupId: me.deviceGroupId,
                        toggle: 0
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }

});


