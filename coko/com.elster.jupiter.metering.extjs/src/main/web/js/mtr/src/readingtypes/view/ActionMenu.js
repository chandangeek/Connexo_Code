/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.readingtypes.view.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.reading-types-action-menu',
    initComponent: function() {
        this.items = [
            {
                itemId: 'reading-types-sorting-menu-activate',
                text: Uni.I18n.translate('readingtypesmanagment.activate', 'MTR', 'Activate'),
                action: 'activate',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'reading-types-sorting-menu-deactivate',
                text: Uni.I18n.translate('readingtypesmanagment.deactivate', 'MTR', 'Deactivate'),
                action: 'deactivate',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'reading-types-action-menu-edit',
                text: Uni.I18n.translate('readingtypesmanagment.edit', 'MTR', 'Edit'),
                action: 'edit',
                section: this.SECTION_EDIT
            }
        ];
        this.callParent(arguments);
    },

    listeners: {
        beforeshow: function(menu) {
            var activateMenuItem = menu.down('#reading-types-sorting-menu-activate'),
                deactivateMenuItem = menu.down('#reading-types-sorting-menu-deactivate'),
                active = menu.record.get('active');

            if (active) {
                deactivateMenuItem.show();
                activateMenuItem.hide();
            } else {
                activateMenuItem.show();
                deactivateMenuItem.hide();
            }
        }
    }
});