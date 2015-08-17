Ext.define('Fim.view.history.Menu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.fim-history-menu',

    router: null,
    title: '',
    showImportService: false,

    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: Uni.I18n.translate('general.overview', 'FIM', 'Overview'),
                hidden: me.showImportService,
                itemId: 'import-service-view-link',
                href: '#/administration/importservices/' + this.importServiceId
            }
        ];

        if (!me.showImportService) {
            me.menuItems.push(
                {
                    text: Uni.I18n.translate('general.history', 'FIM', 'History'),
                    itemId: 'import-service-history-link',
                    href: me.router.getRoute('administration/importservices/importservice/history').buildUrl()
                }
            );
        }


        me.callParent(arguments);
    }
});

