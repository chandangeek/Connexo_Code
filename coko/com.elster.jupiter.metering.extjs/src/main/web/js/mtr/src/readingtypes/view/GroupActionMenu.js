/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mtr.readingtypes.view.GroupActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.readingTypesGroup-action-menu',
    initComponent: function() {
        this.items = [
            {
                itemId: 'mtr-readingTypesGroup-edit-action',
                text: Uni.I18n.translate('readingtypesmanagement.edit', 'MTR', 'Edit group'),
                action: 'edit',
                section: this.SECTION_EDIT
            },
            {
                itemId: 'mtr-readingTypesGroup-remove-action',
                text: Uni.I18n.translate('readingtypesmanagement.remove', 'MTR', 'Remove'),
                action: 'remove',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    },

    // listeners: {
    //     beforeshow: function(menu) {
    //         var activateMenuItem = menu.down('#reading-types-sorting-menu-activate'),
    //             deactivateMenuItem = menu.down('#reading-types-sorting-menu-deactivate'),
    //             active = menu.record.get('active');
    //
    //         if (active) {
    //             deactivateMenuItem.show();
    //             activateMenuItem.hide();
    //         } else {
    //             activateMenuItem.show();
    //             deactivateMenuItem.hide();
    //         }
    //     }
    // }
});
