/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.view.SecurityAccessorsActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.security-accessors-action-menu',

    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.changePrivileges', 'MDC', 'Change privileges'),
                privileges: Mdc.privileges.DeviceType.canAdministrate(),
                visible: function () {
                    return !Ext.isEmpty(this.record) && this.record.get('isKey');
                },
                action: 'changePrivileges',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                privileges: Mdc.privileges.DeviceType.canAdministrate(),
                action: 'edit',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                privileges: Mdc.privileges.DeviceType.canAdministrate(),
                action: 'remove',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    },

    listeners: {
        beforeshow: function() {
            var me = this;
            me.items.each(function(item){
                if (item.visible === undefined) {
                    item.show();
                } else {
                    item.visible.call(me) && Mdc.privileges.DeviceType.canAdministrate() ?  item.show() : item.hide();
                }
            })
        }
    },

});