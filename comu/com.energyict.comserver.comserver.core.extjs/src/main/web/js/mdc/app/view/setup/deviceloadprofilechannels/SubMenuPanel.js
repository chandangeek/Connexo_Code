Ext.define('Mdc.view.setup.deviceloadprofilechannels.SubMenuPanel', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Uni.view.navigation.SubMenu'
    ],
    alias: 'widget.deviceLoadProfileChannelSubMenuPanel',
    itemId: 'deviceLoadProfileChannelSubMenuPanel',
    ui: 'medium',
    title: Uni.I18n.translate('deviceloadprofiles.channels', 'MDC', 'Channels'),
    router: null,
    commonRoute: 'devices/device/loadprofiles/loadprofile/channels/channel/',
    items: [
        {
            xtype: 'navigationSubMenu',
            itemId: 'deviceLoadProfileChannelSubMenu',
            ui: 'side-menu',
            items: [
                {
                    text: Uni.I18n.translate('general.overview', 'MDC', 'Overview'),
                    itemId: 'channelOfLoadProfileOfDeviceOverviewLink',
                    href: 'overview',
                    hrefTarget: '_self'
                },
                {
                    text: Uni.I18n.translate('deviceloadprofiles.channels.channelData', 'MDC', 'Channel data'),
                    itemId: 'channelOfLoadProfileOfDeviceDataLink',
                    href: 'data',
                    hrefTarget: '_self'
                },
                {
                    text: Uni.I18n.translate('deviceloadprofiles.validation', 'MDC', 'Validation'),
                    itemId: 'channelOfLoadProfileOfDeviceValidationLink',
                    href: 'validation',
                    hrefTarget: '_self'
                }
            ]
        }
    ],

    setParams: function (mRID, loadProfileId, model) {
        var me = this,
            menu = this.down('#deviceLoadProfileChannelSubMenu'),
            formatHref;

        menu.setTitle(model.get('name'));

        Ext.Array.each(menu.query('menuitem'), function (item) {
            formatHref = me.router.getRoute(me.commonRoute + item.href).buildUrl({mRID: mRID, loadProfileId: loadProfileId, channelId: model.getId()});

            item.setHref(formatHref);
            (window.location.hash == formatHref) && item.addCls('current');
        });
    }
});