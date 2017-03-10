/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.estimationrules.view.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.estimation-rules-action-menu',
    initComponent: function() {
        this.items = [
            {
                text: Uni.I18n.translate('general.activate', 'EST', 'Activate'),
                action: 'toggleActivation',
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.edit', 'EST', 'Edit'),
                action: 'edit',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.remove', 'EST', 'Remove'),
                action: 'remove',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    },

    listeners: {
        show: {
            fn: function (menu) {
                if (menu.record) {
                    menu.down('[action=toggleActivation]').setText(menu.record.get('active')
                        ? Uni.I18n.translate('general.deactivate', 'EST', 'Deactivate')
                        : Uni.I18n.translate('general.activate', 'EST', 'Activate'));
                }
            }
        }
    }
});