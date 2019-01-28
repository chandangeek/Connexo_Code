/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.view.DeviceActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.tou-campaigns-device-action-menu',
    requires:[
        'Tou.privileges.TouCampaign'
    ],

    initComponent: function () {
        this.items = [
            {
                text:  Uni.I18n.translate('general.cancel', 'TOU', 'Cancel'),
                action: 'cancelDevice',
                itemId: 'tou-device-action-cancel',
                privileges: Tou.privileges.TouCampaign.administrate,
                section: this.SECTION_ACTION
            },
            {
                text:  Uni.I18n.translate('general.retry', 'TOU', 'Retry'),
                action: 'retryDevice',
                itemId: 'tou-device-action-retry',
                privileges: Tou.privileges.TouCampaign.administrate,
                section: this.SECTION_ACTION
            }
        ];
        this.callParent(arguments);
    },

    listeners: {
        beforeshow: function(menu) {
            var currentDeviceStatus = menu.record.get('status'),
                cancelAllowed = currentDeviceStatus === 'Pending' || currentDeviceStatus === 'Ongoing',
                retryAllowed = currentDeviceStatus === 'Canceled' || currentDeviceStatus === 'Failed' || currentDeviceStatus === 'Configuration Error',
                cancelMenuItem = menu.down('#tou-device-action-cancel'),
                retryMenuItem = menu.down('#tou-device-action-retry');

            cancelMenuItem.setVisible(cancelAllowed);
            retryMenuItem.setVisible(retryAllowed);
        }
    }
});