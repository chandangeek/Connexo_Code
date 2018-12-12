/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecycles.view.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.device-life-cycles-action-menu',
    initComponent: function() {
        this.items = [
            {
                text: Uni.I18n.translate('general.clone', 'DLC', 'Clone'),
                action: 'clone',
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.edit', 'DLC', 'Edit'),
                action: 'edit',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.remove', 'DLC', 'Remove'),
                action: 'remove',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});