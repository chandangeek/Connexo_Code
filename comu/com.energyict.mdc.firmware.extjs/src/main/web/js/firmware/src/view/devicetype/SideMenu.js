Ext.define('Fwc.view.devicetype.SideMenu', {
    override: 'Mdc.view.setup.devicetype.SideMenu',

    initComponent: function () {
        this.callParent(arguments);
        this.addMenuItems([{
            title: Uni.I18n.translate('firmware.navigation.title', 'FWC', 'Firmware'),
            items: [
                {
                    text: Uni.I18n.translate('firmware.management.options', 'FWC', 'Firmware management options'),
                    itemId: 'firmwareoptionsLink',
                    href: '#/administration/devicetypes/' + this.deviceTypeId + '/firmware/options'
                },
                {
                    text: Uni.I18n.translate('firmware.versions', 'FWC', 'Firmware versions'),
                    itemId: 'firmwareversionsLink',
                    href: '#/administration/devicetypes/' + this.deviceTypeId + '/firmware/versions'
                }
            ]
        }]);
        this.checkNavigation(Ext.util.History.getToken());
    }
});

