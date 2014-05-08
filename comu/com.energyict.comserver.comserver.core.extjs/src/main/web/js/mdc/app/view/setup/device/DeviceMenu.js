Ext.define('Mdc.view.setup.device.DeviceMenu', {
    extend: 'Uni.view.navigation.SubMenu',
    alias: 'widget.deviceMenu',
    deviceId: null,
    toggle: null,
    initComponent: function () {
        this.callParent(this);
        this.add(
            {
                text: Uni.I18n.translate('devicemenu.overview', 'MDC', 'Overview'),
                pressed: false,
                itemId: 'deviceOverviewLink',
                href: '#setup/devices/' + this.deviceId,
                hrefTarget: '_self'
            },
            {
                text: Uni.I18n.translate('devicemenu.registerTypes', 'MDC', 'Registers'),
                pressed: false,
                itemId: 'registersLink',
                href: '#setup/devices/' + this.deviceId + '/registers',
                hrefTarget: '_self'
            }, {
                text: Uni.I18n.translate('devicemenu.loadProfiles', 'MDC', 'Load profiles'),
                pressed: false,
                itemId: 'loadProfilesLink',
                href: '#setup/devices/' + this.deviceId + '/loadprofiles',
                hrefTarget: '_self'
            }, {
                text: Uni.I18n.translate('devicemenu.logbooks', 'MDC', 'Logbooks'),
                pressed: false,
                itemId: 'logbooksLink',
                href: '#setup/devices/' + this.deviceId + '/logbooks',
                hrefTarget: '_self'
            },
            {
                text: Uni.I18n.translate('devicemenu.connectionMethods', 'MDC', 'Connection methods'),
                pressed: false,
                itemId: 'connectionMethodsLink',
                href: '#setup/devices/' + this.deviceId + '/connectionmethods',
                hrefTarget: '_self'
            },
            {
                text: Uni.I18n.translate('devicemenu.protocols', 'MDC', 'Protocols'),
                pressed: false,
                itemId: 'protocolLink',
                href: '#setup/devices/' + this.deviceId + '/protocols',
                hrefTarget: '_self'
            },
            {
                text: Uni.I18n.translate('devicemenu.communication', 'MDC', 'Communication'),
                pressed: false,
                itemId: 'communicationLink',
                href: '#setup/devices/' + this.deviceId + '/communication',
                hrefTarget: '_self'
            }
        );
        this.toggleMenuItem(this.toggle);
    }
});
