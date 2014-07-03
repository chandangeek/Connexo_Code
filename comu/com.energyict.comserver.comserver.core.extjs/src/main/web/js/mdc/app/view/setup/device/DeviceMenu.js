Ext.define('Mdc.view.setup.device.DeviceMenu', {
    extend: 'Uni.view.navigation.SubMenu',
    alias: 'widget.deviceMenu',
    mRID: null,
    toggle: null,
    initComponent: function () {
        this.callParent(this);
        this.add(
            {
                text: Uni.I18n.translate('devicemenu.overview', 'MDC', 'Overview'),
                itemId: 'deviceOverviewLink',
                href: '#/devices/' + this.mRID,
                hrefTarget: '_self'
            },
            {
                text: Uni.I18n.translate('devicemenu.registerTypes', 'MDC', 'Registers'),
                itemId: 'registersLink',
                href: '#/devices/' + this.mRID + '/registers',
                hrefTarget: '_self'
            }, {
                text: Uni.I18n.translate('devicemenu.loadProfiles', 'MDC', 'Load profiles'),
                itemId: 'loadProfilesLink',
                href: '#/devices/' + this.mRID + '/loadprofiles',
                hrefTarget: '_self'
            }, {
                text: Uni.I18n.translate('devicemenu.logbooks', 'MDC', 'Logbooks'),
                itemId: 'logbooksLink',
                href: '#/devices/' + this.mRID + '/logbooks',
                hrefTarget: '_self'
            },
            {
                text: Uni.I18n.translate('devicemenu.connectionMethods', 'MDC', 'Connection methods'),
                itemId: 'connectionMethodsLink',
                href: '#/devices/' + this.mRID + '/connectionmethods',
                hrefTarget: '_self'
            },
            {
                text: Uni.I18n.translate('devicemenu.protocols', 'MDC', 'Protocol dialects'),
                itemId: 'protocolLink',
                href: '#/devices/' + this.mRID + '/protocols',
                hrefTarget: '_self'
            },
            {
                text: Uni.I18n.translate('devicemenu.communication', 'MDC', 'Communication'),
                itemId: 'communicationLink',
                href: '#/devices/' + this.mRID + '/communication',
                hrefTarget: '_self'
            }
        );
        this.toggleMenuItem(this.toggle);
    }
});
