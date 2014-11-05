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
    commonRoute: 'devices/device/loadprofiles/loadprofile/channels/channel',

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'navigationSubMenu',
                itemId: 'deviceLoadProfileChannelSubMenu',
                ui: 'side-menu',
                width: 250,
                items: [
                    {
                        text: Uni.I18n.translate('general.overview', 'MDC', 'Overview'),
                        itemId: 'channelOfLoadProfileOfDeviceOverviewLink',
                        href: me.router.getRoute(me.commonRoute).buildUrl(),
                        hrefTarget: '_self'
                    },
                    {
                        text: Uni.I18n.translate('deviceloadprofiles.channels.channelData', 'MDC', 'Channel data'),
                        itemId: 'channelOfLoadProfileOfDeviceDataLink',
                        href: me.router.getRoute(me.commonRoute + '/data').buildUrl(me.router.arguments, me.router.queryParams),
                        hrefTarget: '_self'
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    setParams: function (model) {
        var me = this,
            menu = this.down('#deviceLoadProfileChannelSubMenu');

        menu.setTitle(model.get('name'));
        menu.down('menuitem[href=' + window.location.hash + ']').addCls('current');
    }
});