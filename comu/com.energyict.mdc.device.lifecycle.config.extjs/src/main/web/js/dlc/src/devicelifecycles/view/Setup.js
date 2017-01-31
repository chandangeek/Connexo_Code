/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecycles.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-life-cycles-setup',

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Dlc.devicelifecycles.view.Grid',
        'Dlc.devicelifecycles.view.Preview'
    ],

    router: null,

    initComponent: function () {
        var me = this;

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('general.deviceLifeCycles', 'DLC', 'Device life cycles'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'device-life-cycles-grid',
                        itemId: 'device-life-cycles-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'device-life-cycles-empty-panel',
                        title: Uni.I18n.translate('deviceLifeCycles.empty.title', 'DLC', 'No device life cycles found'),
                        reasons: [
                            Uni.I18n.translate('deviceLifeCycles.empty.list.item1', 'DLC', 'No device life cycles have been defined yet.'),
                            Uni.I18n.translate('deviceLifeCycles.empty.list.item2', 'DLC', 'Device life cycles exist, but you do not have permission to view them.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('general.addDeviceLifeCycle', 'DLC', 'Add device life cycle'),
                                href: me.router.getRoute('administration/devicelifecycles/add').buildUrl(),
                                privileges: Dlc.privileges.DeviceLifeCycle.configure,
                                itemId: 'add-device-life-cycle-button'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'device-life-cycles-preview',
                        itemId: 'device-life-cycles-preview'
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});

