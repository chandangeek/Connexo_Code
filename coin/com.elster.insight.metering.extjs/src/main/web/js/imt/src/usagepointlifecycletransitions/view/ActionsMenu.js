/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointlifecycletransitions.view.ActionsMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.transitions-action-menu',
    initComponent: function () {
        var me = this;
        me.items = [
            {
                itemId: 'edit-transition',
                text: Uni.I18n.translate('general.edit', 'IMT', 'Edit'),
                action: 'editTransition',
                section: me.SECTION_EDIT
            },
            {
                itemId: 'remove-transition',
                text: Uni.I18n.translate('general.menu.remove', 'IMT', 'Remove'),
                action: 'removeTransition',
                section: me.SECTION_REMOVE
            }
        ];
        me.callParent();
    }
});


