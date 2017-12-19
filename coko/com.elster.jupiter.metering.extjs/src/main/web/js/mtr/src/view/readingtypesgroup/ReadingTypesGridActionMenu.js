/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.view.readingtypesgroup.ReadingTypesGridActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.reading-types-grid-action-menu',
    initComponent: function () {
        this.items = [
            // {
            //     itemId: 'activate-reading-type',
            //     text: Uni.I18n.translate('general.activate', 'MTR', 'Activate'),
            //     //action: 'retryHistory',
            //     action: 'activateReadingType',
            //     section: this.SECTION_ACTION
            // },
            {
                itemId: 'edit-reading-type',
                text: Uni.I18n.translate('general.edit', 'MTR', 'Edit'),
                //action: 'retryHistory',
                action: 'editReadingType',
                section: this.SECTION_ACTION
            }
        ];
        this.callParent(arguments);
    }
});
