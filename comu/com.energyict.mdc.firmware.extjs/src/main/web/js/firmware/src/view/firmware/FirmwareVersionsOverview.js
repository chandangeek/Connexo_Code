/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.view.firmware.FirmwareVersionsOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.firmware-versions-overview',

    requires: [
        'Mdc.view.setup.devicetype.SideMenu',
        'Fwc.view.firmware.FirmwareVersions',
        'Fwc.view.firmware.FirmwareOptions'
    ],

    router: null,
    deviceTypeId: null,
    deviceType: null,
    firmwareManagementAllowed: null,
    tab2Activate: undefined,

    initComponent: function () {
        var me = this;
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceTypeSideMenu',
                        itemId: 'deviceTypeSideMenu',
                        deviceTypeId: me.deviceTypeId,
                        toggle: 0
                    }
                ]
            }
        ];

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('general.firmwareVersions', 'FWC', 'Firmware versions'),
            items: [
                {
                    xtype: 'tabpanel',
                    itemId: 'fwc-firmware-versions-tabpanel',
                    deferredRender: false,
                    ui: 'large',
                    activeTab: !Ext.isEmpty(me.tab2Activate) ? me.tab2Activate : (me.firmwareManagementAllowed ? 1 : 0),
                    items: [
                        {
                            title: Uni.I18n.translate('general.specifications', 'FWC', 'Specifications'),
                            itemId: 'mdc-options-tab',
                            items: [
                                {
                                    xtype: 'firmware-options',
                                    deviceType: me.deviceType
                                }
                            ]
                        },
                        {
                            title: Uni.I18n.translate('general.firmwareVersions', 'FWC', 'Firmware versions'),
                            itemId: 'mdc-versions-tab',
                            items: [
                                {
                                    xtype: 'firmware-versions',
                                    deviceType: me.deviceType,
                                    router: me.router
                                }
                            ]
                        }
                    ]
                }
            ]
        };

        me.callParent(arguments);
    }
});