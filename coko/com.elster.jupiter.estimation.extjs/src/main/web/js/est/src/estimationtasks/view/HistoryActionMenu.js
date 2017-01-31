/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.estimationtasks.view.HistoryActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.estimationtasks-history-action-menu',
    initComponent: function() {
        this.items = [
            {
                itemId: 'estimationtasks-view-log',
                text: Uni.I18n.translate('estimationtasks.general.viewLog', 'EST', 'View log'),
                action: 'viewLog',
                section: this.SECTION_VIEW
            }
        ];
        this.callParent(arguments);
    }
});


