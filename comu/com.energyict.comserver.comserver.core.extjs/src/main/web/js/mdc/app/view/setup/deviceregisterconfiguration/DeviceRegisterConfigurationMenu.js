Ext.define('Mdc.view.setup.deviceregisterconfiguration.DeviceRegisterConfigurationMenu', {
    extend: 'Uni.view.navigation.SubMenu',
    alias: 'widget.deviceRegisterConfigurationMenu',
    itemId: 'deviceRegisterConfigurationMenu',
    mRID: null,
    registerId: null,
    toggle: null,

    initComponent: function () {
        var me = this;
        me.callParent(arguments);

        me.add(
            {
                text: Uni.I18n.translate('deviceregisterconfiguration.overview', 'MDC', 'Overview'),
                pressed: false,
                itemId: 'deviceRegisterConfigurationOverviewLink',
                href: '#/devices/' + me.mRID + '/registers/' + me.registerId,
                hrefTarget: '_self'
            },
            {
                text: Uni.I18n.translate('deviceregisterconfiguration.registerData', 'MDC', 'Register data'),
                pressed: false,
                itemId: 'registerDataLink',
                href: '#/devices/' + me.mRID + '/registers/' + me.registerId + '/data',
                hrefTarget: '_self'
            }
        );

        me.toggleMenuItem(me.toggle);
    }
});
