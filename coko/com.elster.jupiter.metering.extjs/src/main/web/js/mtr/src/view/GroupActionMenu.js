/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.view.GroupActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.readingTypesGroup-action-menu',

    initComponent: function () {
        this.items = [
            {
                itemId: 'mtr-readingTypesGroup-edit-action',
                text: Uni.I18n.translate('readingTypesManagement.edit', 'MTR', 'Edit group'),
                action: 'edit',
                section: this.SECTION_EDIT
            }
        ];
        this.callParent(arguments);
    }
});