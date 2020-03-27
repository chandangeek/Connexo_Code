/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.customattributesonvaluesobjects.view.CustomAttributeSetActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.time-sliced-custom-attribute-set-action-menu',
    type: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                text: Uni.I18n.translate('general.edit', 'IMT', 'Edit'),
                itemId: 'time-sliced-custom-attribute-set-action-menu-edit-btn-id',
                privileges: Imt.privileges.UsagePoint.admin,
                handler: function() {
                    me.fireEvent('moveToEditPage', me.type, me.record.get('versionId'));
                },
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.clone', 'IMT', 'Clone'),
                itemId: 'time-sliced-custom-attribute-set-action-menu-clone-btn-id',
                privileges: Imt.privileges.UsagePoint.adminTimeSlicedCps,
                handler: function() {
                    me.fireEvent('moveToClonePage', me.type, me.record.get('versionId'));
                },
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.remove', 'IMT', 'Remove'),
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