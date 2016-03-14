Ext.define('Fwc.view.devicetype.SideMenu', {
    override: 'Mdc.view.setup.devicetype.SideMenu',

    initComponent: function () {
        var me = this;
        me.callParent(arguments);
        me.executeIfNoDataLoggerSlave(function() {
            me.addMenuItems([{
                title: Uni.I18n.translate('general.firmware', 'FWC', 'Firmware'),
                items: [
                    {
                        text: Uni.I18n.translate('general.firmwareManagementOptions', 'FWC', 'Firmware management options'),
                        itemId: 'firmwareoptionsLink',
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/firmware/options'
                    },
                    {
                        text: Uni.I18n.translate('general.firmwareVersions', 'FWC', 'Firmware versions'),
                        itemId: 'firmwareversionsLink',
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/firmware/versions'
                    }
                ]
            }]);
        });
        me.checkNavigation(Ext.util.History.getToken());
    }
});

