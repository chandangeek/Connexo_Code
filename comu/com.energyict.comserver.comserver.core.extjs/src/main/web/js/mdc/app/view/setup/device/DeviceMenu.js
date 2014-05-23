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
                itemId: 'deviceOverviewLink',
                href: '#/administration/devices/' + this.deviceId,
                hrefTarget: '_self'
            },
            {
                text: Uni.I18n.translate('devicemenu.registerTypes', 'MDC', 'Registers'),
                itemId: 'registersLink',
                href: '#/administration/devices/' + this.deviceId + '/registers',
                hrefTarget: '_self'
            }, {
                text: Uni.I18n.translate('devicemenu.loadProfiles', 'MDC', 'Load profiles'),
                itemId: 'loadProfilesLink',
                href: '#/administration/devices/' + this.deviceId + '/loadprofiles',
                hrefTarget: '_self'
            }, {
                text: Uni.I18n.translate('devicemenu.logbooks', 'MDC', 'Logbooks'),
                itemId: 'logbooksLink',
                href: '#/administration/devices/' + this.deviceId + '/logbooks',
                hrefTarget: '_self'
            },
            {
                text: Uni.I18n.translate('devicemenu.connectionMethods', 'MDC', 'Connection methods'),
                itemId: 'connectionMethodsLink',
                href: '#/administration/devices/' + this.deviceId + '/connectionmethods',
                hrefTarget: '_self'
            },
            {
                text: Uni.I18n.translate('devicemenu.protocols', 'MDC', 'Protocols'),
                itemId: 'protocolLink',
                href: '#/administration/devices/' + this.deviceId + '/protocols',
                hrefTarget: '_self'
            },
            {
                text: Uni.I18n.translate('devicemenu.communication', 'MDC', 'Communication'),
                itemId: 'communicationLink',
                href: '#/administration/devices/' + this.deviceId + '/communication',
                hrefTarget: '_self'
            }
        );
        this.toggleMenuItem(this.toggle);
    }
});
