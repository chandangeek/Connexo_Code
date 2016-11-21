Ext.define('Sam.view.datapurge.HistoryActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.data-purge-history-action-menu',
    router: null,
    initComponent: function() {
        var me = this;
        me.items = [
            {
                text: Uni.I18n.translate('datapurge.history.viewlog', 'SAM', 'View log'),
                itemId: 'sam-purge-history-view-log',
                href: me.router.getRoute(me.router.currentRoute + '/log').buildUrl({historyId: me.record.getId()}),
                section: this.SECTION_VIEW
            }
        ];
        me.callParent(arguments);
    },

    listeners: {
        beforeshow: function(menu) {
            var viewLogMenuItem = menu.down('#sam-purge-history-view-log');
            if (menu.record && menu.router) {
                viewLogMenuItem.show();
            } else {
                viewLogMenuItem.hide();
            }
        }
    }
});