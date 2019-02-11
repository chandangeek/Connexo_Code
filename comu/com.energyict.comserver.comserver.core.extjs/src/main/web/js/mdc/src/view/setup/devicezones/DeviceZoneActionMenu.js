/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicezones.DeviceZoneActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.device-of-zone-action-menu',
    itemId: 'device-of-zone-action-menu',
    initComponent: function () {
        this.items = [

            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                itemId: 'remove-device-zone',
                action: 'deleteDeviceZone',
                privileges: Cfg.privileges.Validation.adminZones,
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});
