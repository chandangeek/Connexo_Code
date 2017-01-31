/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecyclestates.view.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.device-life-cycle-states-action-menu',
    initComponent: function() {
        this.items = [
            {
                text: Uni.I18n.translate('general.edit', 'DLC', 'Edit'),
                itemId: 'edit-action',
                action: 'edit',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('deviceLifeCycleStates.setAsInitial', 'DLC', 'Set as initial state'),
                itemId: 'initialAction',
                action: 'setAsInitial',
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.remove', 'DLC', 'Remove'),
                itemId: 'remove-state',
                action: 'removeState',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    },

    listeners: {
        beforeshow: function(menu) {
            var setAsInitialMenuItem = menu.down('#initialAction'),
                isInitial = menu.record.get('isInitial');

            isInitial ? setAsInitialMenuItem.hide() : setAsInitialMenuItem.show();
        }
    }
});