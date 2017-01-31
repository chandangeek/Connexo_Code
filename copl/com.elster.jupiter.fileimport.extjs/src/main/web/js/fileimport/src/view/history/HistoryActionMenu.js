/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fim.view.history.HistoryActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.fim-history-action-menu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'menu-view-log',
                text: Uni.I18n.translate('importService.history.viewLog', 'FIM', 'View log'),
                action: 'viewLog',
                section: this.SECTION_VIEW
            }
        ];
        this.callParent(arguments);
    }
});


