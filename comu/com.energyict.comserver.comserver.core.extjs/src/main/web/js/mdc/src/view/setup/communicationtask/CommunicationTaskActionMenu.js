/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.communicationtask.CommunicationTaskActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.communication-task-action-menu',
    itemId: 'communication-task-action-menu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.activate', 'MDC', 'Activate'),
                action: 'activatecommunicationtask',
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                action: 'editcommunicationtask',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                action: 'removecommunicationtask',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});
