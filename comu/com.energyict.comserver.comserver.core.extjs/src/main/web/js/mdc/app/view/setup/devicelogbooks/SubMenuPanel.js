Ext.define('Mdc.view.setup.devicelogbooks.SubMenuPanel', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Uni.view.navigation.SubMenu'
    ],
    alias: 'widget.deviceLogbookSubMenuPanel',
    itemId: 'deviceLogbookSubMenuPanel',
    ui: 'medium',
    title: Uni.I18n.translate('devicelogbooks.subMenuPanel.header', 'MDC', 'Logbooks'),
    router: null,
    commonRoute: 'devices/device/logbooks/logbook/',
    items: [
        {
            xtype: 'navigationSubMenu',
            itemId: 'deviceLogbookSubMenu',
            ui: 'side-menu',
            width: 310,
            items: [
                {
                    text: Uni.I18n.translate('general.overview', 'MDC', 'Overview'),
                    itemId: 'logbookOfDeviceOverviewLink',
                    href: 'overview',
                    hrefTarget: '_self'
                },
                {
                    text: Uni.I18n.translate('devicelogbooks.subMenuPanel.logbookData', 'MDC', 'Logbook data'),
                    itemId: 'logbookOfDeviceDataLink',
                    href: 'data',
                    hrefTarget: '_self'
                }
            ]
        }
    ],

    setParams: function (mRID ,model) {
        var me = this,
            menu = this.down('#deviceLogbookSubMenu'),
            formatHref;

        menu.setTitle(model.get('name'));

        Ext.Array.each(menu.query('menuitem'), function (item) {
            formatHref = me.router.getRoute(me.commonRoute + item.href).buildUrl({mRID: mRID, logbookId: model.getId()});

            item.setHref(formatHref);
            (window.location.hash == formatHref) && item.addCls('current');
        });
    }
});