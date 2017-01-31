/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicelogbooks.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.deviceLogbooksActionMenu',
    itemId: 'deviceLogbooksActionMenu',
    record: null,
    initComponent: function () {
        this.items = [
            {
                itemId: 'editLogbook',
                text: Uni.I18n.translate('general.changeNextReadingBlockStart', 'MDC', 'Change next reading block start'),
                action: 'editLogbook',
                section: this.SECTION_EDIT
            }
        ];
        this.callParent(arguments);
    }
});
