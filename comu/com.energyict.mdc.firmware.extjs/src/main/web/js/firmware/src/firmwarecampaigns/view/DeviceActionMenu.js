Ext.define('Fwc.firmwarecampaigns.view.DeviceActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.firmware-campaigns-device-action-menu',
    requires:[
        'Fwc.privileges.FirmwareCampaign'
    ],
    plain: true,
    border: false,
    shadow: false,
    cancelMenuItem: {
        text: Uni.I18n.translate('general.cancel', 'FWC', 'Cancel'),
        action: 'cancelDevice',
        itemId: 'fwc-device-action-cancel',
        privileges: Fwc.privileges.FirmwareCampaign.administrate
    },
    retryMenuItem: {
        text: Uni.I18n.translate('device.firmware.failed.retry', 'FWC', 'Retry'),
        action: 'retryDevice',
        itemId: 'fwc-device-action-retry',
        privileges: Fwc.privileges.FirmwareCampaign.administrate
    },

    initComponent: function () {
        var me = this;

        me.items = [ me.cancelMenuItem, me.retryMenuItem ];
        me.callParent(arguments);
        me.mon(me, 'beforeshow', me.onBeforeShow, me);
    },

    onBeforeShow: function() {
        var me = this,
            currentDeviceStatus = me.record.get('status').id,
            cancelAllowed = currentDeviceStatus === 'pending' || currentDeviceStatus === 'ongoing',
            retryAllowed = currentDeviceStatus === 'cancelled' || currentDeviceStatus === 'failed' || currentDeviceStatus === 'configurationError',
            currentCancelMenuItem = me.down('#fwc-device-action-cancel'),
            currentRetryMenuItem = me.down('#fwc-device-action-retry');

        if (cancelAllowed) {
            if (!currentCancelMenuItem) {
                me.insert(0, me.cancelMenuItem);
            }
        } else if (currentCancelMenuItem) {
            me.remove(currentCancelMenuItem);
        }

        if (retryAllowed) {
            if (!currentRetryMenuItem) {
                me.add(me.retryMenuItem);
            }
        } else if (currentRetryMenuItem) {
            me.remove(currentRetryMenuItem);
        }
    }
});