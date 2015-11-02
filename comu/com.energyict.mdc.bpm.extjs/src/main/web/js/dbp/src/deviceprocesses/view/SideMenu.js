Ext.define('Dbp.deviceprocesses.view.SideMenu', {
    override: 'Mdc.view.setup.device.DeviceMenu',

    initComponent: function () {
        var me = this;

        me.callParent(arguments);
        mRID = me.device.get('mRID');
/*
        me.addMenuItems(
            {
                text: Uni.I18n.translate('devicemenu.processes', 'dbp', 'Processes'),
                privileges: Mdc.privileges.Device.viewOrAdministrateDeviceData,
                itemId: 'device-processes-link',
                href: '#/devices/' + encodeURIComponent(mRID) + '/processes'
            }, this.menuItems[0]
        );
        this.checkNavigation(Ext.util.History.getToken());*/
    }
});

