/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointlifecyclestates.view.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.usagepoint-life-cycle-states-action-menu',
    initComponent: function () {
        var me = this;
        me.items = [
            {
                text: Uni.I18n.translate('general.edit', 'IMT', 'Edit'),
                itemId: 'edit-action',
                action: 'edit',
                section: me.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('usagePointLifeCycleStates.setAsInitial', 'IMT', 'Set as initial state'),
                itemId: 'initialAction',
                action: 'setAsInitial',
                section: me.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.menu.remove', 'IMT', 'Remove'),
                itemId: 'remove-state',
                action: 'removeState',
                section: me.SECTION_REMOVE
            }
        ];
        me.callParent();
    },

    listeners: {
        beforeshow: function () {
            this.down('#initialAction').setVisible(!this.record.get('isInitial'));
        }
    }
});