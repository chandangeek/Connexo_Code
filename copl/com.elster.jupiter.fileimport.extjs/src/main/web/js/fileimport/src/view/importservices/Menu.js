Ext.define('Fim.view.importservices.Menu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.fim-import-service-menu',

    router: null,
    importServiceId: null,
    title: Uni.I18n.translate('general.importServices', 'FIM', 'Import services'),

    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: Uni.I18n.translate('general.overview', 'FIM', 'Overview'),
                itemId: 'import-services-view-link',
                href: '#/administration/importservices/' + this.importServiceId
            },
			{
                text: Uni.I18n.translate('general.history', 'FIM', 'History'),
                itemId: 'import-services-view-history-link',
                href: '#/administration/importservices/' + this.importServiceId + '/history'
            }
        ];


        me.callParent(arguments);
    }
});

