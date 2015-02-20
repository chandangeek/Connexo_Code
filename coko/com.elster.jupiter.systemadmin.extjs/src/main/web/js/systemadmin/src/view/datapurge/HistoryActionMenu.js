Ext.define('Sam.view.datapurge.HistoryActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.data-purge-history-action-menu',
    plain: true,
    border: false,
    shadow: false,
    defaultAlign: 'tr-br?',
    router: null,

    listeners: {
        show: {
            fn: function () {
                var me = this;
                Ext.suspendLayouts();
                me.removeAll();
                if (me.record && me.router) {
                    me.add(
                        {
                            text: Uni.I18n.translate('datapurge.history.viewlog', 'SAM', 'View log'),
                            href: me.router.getRoute(me.router.currentRoute + '/log').buildUrl({historyId: me.record.getId()})
                        }
                    );
                }
                Ext.resumeLayouts(true);
            }
        }
    }
});