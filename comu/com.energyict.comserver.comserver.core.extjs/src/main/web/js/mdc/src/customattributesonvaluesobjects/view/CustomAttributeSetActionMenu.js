/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.customattributesonvaluesobjects.view.CustomAttributeSetActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.time-sliced-custom-attribute-set-action-menu',
    itemId: 'time-sliced-custom-attribute-set-action-menu-id',
    type: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                itemId: 'time-sliced-custom-attribute-set-action-menu-edit-btn-id',
                handler: function() {
                    me.fireEvent('moveToEditPage', me.type, me.record.get('versionId'));
                },
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.clone', 'MDC', 'Clone'),
                itemId: 'time-sliced-custom-attribute-set-action-menu-clone-btn-id',
                handler: function() {
                    me.fireEvent('moveToClonePage', me.type, me.record.get('versionId'));
                },
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                itemId: 'time-sliced-custom-attribute-set-action-menu-remove-btn-id',
                handler: function() {
                    me.fireEvent('removeVersion', me.type, me.record.get('versionId'), me.record.get('period'));
                },
                section: this.SECTION_REMOVE
            }
        ];

        me.callParent(arguments);
    },
    onBeforeShow: function(){
        var me = this,
            record = me.record,
            removeMenuItem = me.down('#time-sliced-custom-attribute-set-action-menu-remove-btn-id');

        record.get('removable') ? removeMenuItem.show() : removeMenuItem.hide();
        me.callParent(arguments);


    }
});