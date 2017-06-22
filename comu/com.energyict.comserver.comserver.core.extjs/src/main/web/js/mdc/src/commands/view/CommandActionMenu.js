/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.commands.view.CommandActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.command-action-menu',

    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.triggerNow', 'MDC', 'Trigger now'),
                itemId: 'mdc-command-action-triggerNow',
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.changeReleaseDate', 'MDC', 'Change release date'),
                itemId: 'mdc-command-action-changeReleaseDate',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.revoke', 'MDC', 'Revoke'),
                itemId: 'mdc-command-action-revoke',
                section: this.SECTION_ACTION
            }
        ];
        this.callParent(arguments);
    }
});