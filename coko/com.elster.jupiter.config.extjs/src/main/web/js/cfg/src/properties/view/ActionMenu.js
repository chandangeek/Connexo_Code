/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.properties.view.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.cfg-properties-action-menu',
    initComponent: function() {
        this.items = [
            {
                itemId: 'edit-config-properties',
                text: Uni.I18n.translate('general.edit', 'CFG', 'Edit'),
                action: 'editConfigProperties',
                privileges: this.editPrivileges,
                section: this.SECTION_EDIT
            }
        ];
        this.callParent(arguments);
    }
});

