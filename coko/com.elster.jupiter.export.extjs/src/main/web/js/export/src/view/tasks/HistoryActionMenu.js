/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.view.tasks.HistoryActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.tasks-history-action-menu',
    initComponent: function() {
        this.items = [
            {
                itemId: 'view-log',
                text: Uni.I18n.translate('general.viewLog', 'DES', 'View log'),
                action: 'viewLog',
                section: this.SECTION_VIEW
            }
        ];
        this.callParent(arguments);
    }
});


