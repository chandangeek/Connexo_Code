/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation',
        'Uni.store.PortalItems',
        'Mdc.dynamicprivileges.DeviceState',
        'Mdc.dynamicprivileges.Stores',
        'Mdc.dynamicprivileges.DeviceTypeCapability'
    ],

    controllers: [
        'Fwc.controller.History',
        'Fwc.controller.Firmware',
        'Fwc.devicefirmware.controller.DeviceFirmware',
        'Fwc.devicefirmware.controller.FirmwareLog',
        'Fwc.firmwarecampaigns.controller.Overview',
        'Fwc.firmwarecampaigns.controller.Add',
        'Fwc.firmwarecampaigns.controller.Detail',
        'Fwc.firmwarecampaigns.controller.Devices'
    ],

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        }
    ],

    init: function () {
        var me = this,
            historian = me.getController('Fwc.controller.History'); // Forces route registration.

        if (Fwc.privileges.FirmwareCampaign.canView()) {
            Uni.store.PortalItems.add(Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('firmware.firmwareManagement', 'FWC', 'Firmware management'),
                portal: 'workspace',
                route: 'firmwarecampaigns',
                items: [
                    {
                        text: Uni.I18n.translate('firmware.campaigns.firmwareCampaigns', 'FWC', 'Firmware campaigns'),
                        itemId:'firmware-campaigns-link-mnu',
                        href: '#/workspace/firmwarecampaigns',
                        route: 'workspace',
                        privileges: Fwc.privileges.FirmwareCampaign.view
                    }
                ]
            }));
        }

        me.getApplication().fireEvent('cfginitialized');
    }
});