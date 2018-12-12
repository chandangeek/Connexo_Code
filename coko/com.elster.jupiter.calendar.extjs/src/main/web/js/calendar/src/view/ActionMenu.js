/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cal.view.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.tou-action-menu',
    initComponent: function() {
        this.items = [
            {
                itemId: 'activateDeactivate',
                text: Uni.I18n.translate('general.deActivate', 'CAL', 'Deactivate'),
                action: 'activateDeactivate',
                privileges: Cal.privileges.Calendar.admin,
                activateDeactivate: function(){
                    return this.record.get('status').id;
                },
                section: this.SECTION_ACTION
            },
            {
                itemId: 'view-preview-cal',
                text: Uni.I18n.translate('general.viewPreview', 'CAL', 'View preview'),
                action: 'viewpreview',
                section: this.SECTION_VIEW
            },
            {
                itemId: 'remove-preview-cal',
                text: Uni.I18n.translate('general.remove', 'CAL', 'Remove'),
                privileges: Cal.privileges.Calendar.admin,
                action: 'remove',
                visible: function () {
                    return !this.record.get('inUse');
                },
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    },

    listeners: {
        beforeshow: function () {
            var me = this;
            me.items.each(function (item) {
                if(item.activateDeactivate !== undefined){
                    item.setText(item.activateDeactivate.call(me)==='ACTIVE'?Uni.I18n.translate('general.deActivate', 'CAL', 'Deactivate'):Uni.I18n.translate('general.activate', 'CAL', 'Activate'));
                }
            })
        }
    }
});