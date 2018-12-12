/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.view.datapurge.HistoryActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.data-purge-history-action-menu',
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