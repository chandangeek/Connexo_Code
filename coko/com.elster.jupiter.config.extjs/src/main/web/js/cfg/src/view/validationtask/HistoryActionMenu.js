/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.validationtask.HistoryActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.cfg-tasks-history-action-menu',
    initComponent: function() {
        this.items = [
            {
                itemId: 'view-log',
                text: Uni.I18n.translate('validationTasks.general.viewLog', 'CFG', 'View log'),
                action: 'viewLog',
                section: this.SECTION_VIEW
            }
        ];
        this.callParent(arguments);
    }
});


