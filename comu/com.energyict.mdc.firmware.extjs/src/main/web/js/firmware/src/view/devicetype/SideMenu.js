Ext.define('Fwc.view.devicetype.SideMenu', {
    override: 'Mdc.view.setup.devicetype.SideMenu',

    initComponent: function () {
        this.callParent(arguments);
        this.addMenuItems([{
            title: Uni.I18n.translate('general.firmware', 'FWC', 'Firmware'),
            items: [
                {
                    text: Uni.I18n.translate('general.firmwareManagementOptions', 'FWC', 'Firmware management options'),
                    itemId: 'firmwareoptionsLink',
                    href: '#/administration/devicetypes/' + this.deviceTypeId + '/firmware/options'
                },
                {
                    text: Uni.I18n.translate('general.firmwareVersions', 'FWC', 'Firmware versions'),
                    itemId: 'firmwareversionsLink',
                    href: '#/administration/devicetypes/' + this.deviceTypeId + '/firmware/versions'
                }
            ]
        }]);
        this.checkNavigation(Ext.util.History.getToken());
    }
});

