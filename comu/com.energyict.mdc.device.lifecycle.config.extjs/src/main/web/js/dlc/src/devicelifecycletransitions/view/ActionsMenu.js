/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecycletransitions.view.ActionsMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.transitions-action-menu',
    initComponent: function() {
        this.items = [
            {
                itemId: 'edit-transition',
                text: Uni.I18n.translate('general.edit', 'DLC', 'Edit'),
                action: 'editTransition',
                section: this.SECTION_EDIT
            },
            {
                itemId: 'remove-transition',
                text: Uni.I18n.translate('general.remove', 'DLC', 'Remove'),
                action: 'removeTransition',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});


