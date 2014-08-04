Ext.define('Mdc.view.setup.deviceloadprofiles.SubMenuPanel', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Uni.view.navigation.SubMenu'
    ],
    alias: 'widget.deviceLoadProfilesSubMenuPanel',
    itemId: 'deviceLoadProfilesSubMenuPanel',
    ui: 'medium',
    title: Uni.I18n.translate('deviceloadprofiles.loadProfiles', 'MDC', 'Load profiles'),
    items: [
        {
            xtype: 'navigationSubMenu',
            itemId: 'deviceLoadProfilesSubMenu',
            ui: 'side-menu',
            items: [
                {
                    text: Uni.I18n.translate('general.overview', 'MDC', 'Overview'),
                    itemId: 'loadProfileOfDeviceOverviewLink',
                    href: '#/devices/{0}/loadprofiles/{1}/overview',
                    hrefTarget: '_self'
                },
                {
                    text: Uni.I18n.translate('deviceloadprofiles.channels', 'MDC', 'Channels'),
                    itemId: 'loadProfileOfDeviceChannelsLink',
                    href: '#/devices/{0}/loadprofiles/{1}/channels',
                    hrefTarget: '_self'
                },
                {
                    text: Uni.I18n.translate('deviceloadprofiles.loadProfileData', 'MDC', 'Load profile data'),
                    itemId: 'loadProfileOfDeviceDataLink',
                    href: '#/devices/{0}/loadprofiles/{1}/data',
                    hrefTarget: '_self'
                },
                {
                    text: Uni.I18n.translate('deviceloadprofiles.validation', 'MDC', 'Validation'),
                    itemId: 'loadProfileOfDeviceValidationLink',
                    href: '#/devices/{0}/loadprofiles/{1}/validation',
                    hrefTarget: '_self'
                }
            ]
        }
    ],

    setParams: function (mRID ,model) {
        var id = model.getId(),
            name = model.get('name'),
            currentHash = window.location.hash,
            menu = this.down('#deviceLoadProfilesSubMenu');

        menu.setTitle(name);

        Ext.Array.each(menu.query('menuitem'), function (item) {
            var href = item.href,
                formatHref = Ext.String.format(href, mRID, id);

            item.setHref(formatHref);
            (currentHash == formatHref) && item.addCls('current');
        });
    }
});