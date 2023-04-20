/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.view.DeviceActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.firmware-campaigns-device-action-menu',
    requires: [
        'Fwc.privileges.FirmwareCampaign'
    ],
    manuallyCancelled: null,

    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.cancel', 'FWC', 'Cancel'),
                action: 'cancelDevice',
                itemId: 'fwc-device-action-cancel',
                privileges: Fwc.privileges.FirmwareCampaign.administrate,
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('device.firmware.failed.retry', 'FWC', 'Retry'),
                action: 'retryDevice',
                itemId: 'fwc-device-action-retry',
                privileges: Fwc.privileges.FirmwareCampaign.administrate,
                section: this.SECTION_ACTION
            }
        ];
        this.callParent(arguments);
    },

    listeners: {
        beforeshow: function (menu) {
            var me = this,
                currentDeviceStatus = menu.record.get('status').id,
                cancelAllowed = currentDeviceStatus === 'PENDING' || currentDeviceStatus === 'ONGOING',
                retryAllowed = currentDeviceStatus === 'CANCELLED' || currentDeviceStatus === 'FAILED' || currentDeviceStatus === 'REJECTED' || me.manuallyCancelled,
                cancelMenuItem = menu.down('#fwc-device-action-cancel'),
                retryMenuItem = menu.down('#fwc-device-action-retry');

            cancelMenuItem.setVisible(cancelAllowed);
            retryMenuItem.setVisible(retryAllowed);
        }
    }
});
