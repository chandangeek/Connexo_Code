/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.customtask.HistoryActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.ctk-tasks-history-action-menu',
    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'view-log',
                text: Uni.I18n.translate('customTask.general.viewLog', 'APR', 'View log'),
                action: 'viewLog',
                section: this.SECTION_VIEW
            }
        ];
        me.callParent(arguments);
    }
});


