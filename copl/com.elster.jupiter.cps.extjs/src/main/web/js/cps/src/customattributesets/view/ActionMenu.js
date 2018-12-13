/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cps.customattributesets.view.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.custom-attribute-sets-action-menu',
    initComponent: function () {
        this.items =
            [
                {
                    text: Uni.I18n.translate('customattributesets.editlevels', 'CPS', 'Edit levels'),
                    itemId: 'custom-attribute-sets-edit-levels',
                    section: this.SECTION_EDIT
                }
            ];
        this.callParent(arguments);
    }
});