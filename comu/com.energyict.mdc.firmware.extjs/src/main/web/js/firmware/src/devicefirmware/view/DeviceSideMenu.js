Ext.define('Fwc.devicefirmware.view.DeviceSideMenu', {
    override: 'Mdc.view.setup.device.DeviceMenu',

    initComponent: function () {
        var me = this;

        this.callParent(arguments);
        this.addMenuItems([{
            title: Uni.I18n.translate('general.firmware', 'FWC', 'Firmware'),
            items: [
                {
                    text: Uni.I18n.translate('general.firmware', 'FWC', 'Firmware'),
                    itemId: 'device-firmware-link-menu',
                    href: '#/devices/' + encodeURIComponent(me.device.get('mRID')) + '/firmware'
                }
            ]
        }]);
    }
});
