/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.view.DeviceActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.tou-campaigns-device-action-menu',
    requires:[
        'Fwc.privileges.FirmwareCampaign'
    ],

    initComponent: function () {
        this.items = [
            /*{
                text: 'Cancel',
                action: 'cancelDevice',
                itemId: 'tou-device-action-cancel',
                privileges: Fwc.privileges.FirmwareCampaign.administrate,
                section: this.SECTION_ACTION
            },*/
            {
                text: 'Retry',
                action: 'retryDevice',
                itemId: 'tou-device-action-retry',
                privileges: Fwc.privileges.FirmwareCampaign.administrate,
                section: this.SECTION_ACTION
            }
        ];
        this.callParent(arguments);
    },

    listeners: {
        beforeshow: function(menu) {
            var currentDeviceStatus = menu.record.get('status'),
                //cancelAllowed = currentDeviceStatus === 'pending' || currentDeviceStatus === 'ongoing',
                retryAllowed = currentDeviceStatus === 'cancelled' || currentDeviceStatus === 'failed' || currentDeviceStatus === 'configurationError',
                //cancelMenuItem = menu.down('#tou-device-action-cancel'),
                retryMenuItem = menu.down('#tou-device-action-retry');

            //cancelMenuItem.setVisible(cancelAllowed);
            retryMenuItem.setVisible(retryAllowed);
        }
    }
});