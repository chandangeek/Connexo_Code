/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.view.tasks.HistoryGridActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.history-grid-action-menu',
    initComponent: function() {
        this.items = [
            {
                itemId: 'retry-history',
                text: Uni.I18n.translate('general.retry', 'DES', 'Retry'),
                action: 'retryHistory',
                section: this.SECTION_ACTION
            }
        ];
        this.callParent(arguments);
    }
});


