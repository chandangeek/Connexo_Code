/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.validation.VersionActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.version-action-menu',
    itemId: 'version-action-menu',
    initComponent: function() {
        this.items = [
            {
                itemId: 'editVersion',
                text: Uni.I18n.translate('general.edit', 'CFG', 'Edit'),
                privileges: Cfg.privileges.Validation.admin,
                action: 'editVersion',
                section: this.SECTION_EDIT
            },
            {
                itemId: 'cloneVersion',
                text: Uni.I18n.translate('validation.clone', 'CFG', 'Clone'),
                privileges: Cfg.privileges.Validation.admin,
                action: 'cloneVersion',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'deleteVersion',
                text: Uni.I18n.translate('general.remove', 'CFG', 'Remove'),
                privileges: Cfg.privileges.Validation.admin,
                action: 'deleteVersion',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});

