Ext.define('Mdc.view.setup.deviceloadprofiles.SubMenuPanel', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Uni.view.navigation.SubMenu'
    ],
    alias: 'widget.deviceLoadProfilesSubMenuPanel',
    itemId: 'deviceLoadProfilesSubMenuPanel',
    ui: 'medium',
    title: Uni.I18n.translate('deviceloadprofiles.loadProfiles', 'MDC', 'Load profiles'),
    router: null,
    commonRoute: 'devices/device/loadprofiles/loadprofile/',
    items: [
        {
            xtype: 'navigationSubMenu',
            itemId: 'deviceLoadProfilesSubMenu',
            ui: 'side-menu',
            items: [
                {
                    text: Uni.I18n.translate('general.overview', 'MDC', 'Overview'),
                    itemId: 'loadProfileOfDeviceOverviewLink',
                    href: 'overview',
                    hrefTarget: '_self'
                },
                {
                    text: Uni.I18n.translate('deviceloadprofiles.channels', 'MDC', 'Channels'),
                    itemId: 'loadProfileOfDeviceChannelsLink',
                    href: 'channels',
                    hrefTarget: '_self'
                },
                {
                    text: Uni.I18n.translate('deviceloadprofiles.loadProfileData', 'MDC', 'Load profile data'),
                    itemId: 'loadProfileOfDeviceDataLink',
                    href: 'data',
                    hrefTarget: '_self'
                },
                {
                    text: Uni.I18n.translate('deviceloadprofiles.validation', 'MDC', 'Validation'),
                    itemId: 'loadProfileOfDeviceValidationLink',
                    href: 'validation',
                    hrefTarget: '_self'
                }
            ]
        }
    ],

    setParams: function (mRID ,model) {
        var me = this,
            menu = this.down('#deviceLoadProfilesSubMenu'),
            formatHref;

        menu.setTitle(model.get('name'));

        Ext.Array.each(menu.query('menuitem'), function (item) {
            formatHref = me.router.getRoute(me.commonRoute + item.href).buildUrl({mRID: mRID, loadProfileId: model.getId()});

            item.setHref(formatHref);
            (window.location.hash == formatHref) && item.addCls('current');
        });
    }
});