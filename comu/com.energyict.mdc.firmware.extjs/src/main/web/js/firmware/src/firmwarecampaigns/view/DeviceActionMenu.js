Ext.define('Fwc.firmwarecampaigns.view.DeviceActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.firmware-campaigns-device-action-menu',
    requires:[
        'Fwc.privileges.FirmwareCampaign'
    ],
    plain: true,
    border: false,
    shadow: false,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                text: Uni.I18n.translate('firmware.campaigns.skip', 'FWC', 'Skip'),
                action: 'skipDevice',
                itemId: 'fwc-device-action-skip',
                privileges: Fwc.privileges.FirmwareCampaign.administrate
            },
            {
                text: Uni.I18n.translate('device.firmware.failed.retry', 'FWC', 'Retry'),
                action: 'retryDevice',
                itemId: 'fwc-device-action-retry',
                privileges: Fwc.privileges.FirmwareCampaign.administrate
            }
        ];
        me.callParent(arguments);

        me.mon(me, 'beforeshow', me.onBeforeShow, me);
    },

    onBeforeShow: function() {
        var me = this,
            currentDeviceStatus = me.record.get('status').id,
            skipAllowed = currentDeviceStatus === 'pending' || currentDeviceStatus === 'ongoing',
            retryAllowed = currentDeviceStatus === 'skipped' || currentDeviceStatus === 'failed' || currentDeviceStatus === 'configurationError',
            skipMenuItem = me.down('#fwc-device-action-skip'),
            retryMenuItem = me.down('#fwc-device-action-retry');

        if (skipAllowed) {
            if (!skipMenuItem) {
                me.insert(0, {
                    text: Uni.I18n.translate('firmware.campaigns.skip', 'FWC', 'Skip'),
                    action: 'skipDevice',
                    itemId: 'fwc-device-action-skip',
                    privileges: Fwc.privileges.FirmwareCampaign.administrate
                });
            }
        } else if (skipMenuItem) {
            me.remove(skipMenuItem);
        }

        if (retryAllowed) {
            if (!retryMenuItem) {
                me.add({
                    text: Uni.I18n.translate('device.firmware.failed.retry', 'FWC', 'Retry'),
                    action: 'retryDevice',
                    itemId: 'fwc-device-action-retry',
                    privileges: Fwc.privileges.FirmwareCampaign.administrate
                });
            }
        } else if (retryMenuItem) {
            me.remove(retryMenuItem);
        }
    }
});