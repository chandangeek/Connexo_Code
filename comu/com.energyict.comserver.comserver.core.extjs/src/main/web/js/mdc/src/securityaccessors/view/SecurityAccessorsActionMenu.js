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
                privileges: Mdc.privileges.SecurityAccessor.canAdmin(),
                hidden: this.deviceTypeId && !(!Ext.isEmpty(this.record) && this.record.get('isKey')),
                action: 'changePrivileges',
                itemId: 'menu-sa-change-privileges',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                privileges: Mdc.privileges.SecurityAccessor.canAdmin(),
                action: 'edit',
                itemId: 'menu-sa-edit',
                hidden: !!this.deviceTypeId,
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                privileges: Mdc.privileges.SecurityAccessor.canAdmin(),
                action: 'remove',
                itemId: 'menu-sa-remove',
                section: this.SECTION_REMOVE
            },
            {
                text: Uni.I18n.translate('general.clearPassiveCertificate', 'MDC', 'Clear passive certificate'),
                privileges: Mdc.privileges.SecurityAccessor.canAdmin(),
                checkPassive: true,
                hidden: !Ext.isEmpty(this.record) && !this.record.get('passiveCertificate'),
                action: 'clearPassiveCertificate',
                itemId: 'menu-sa-clear-passive-certificate',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.activatePassiveCertificate', 'MDC', 'Activate passive certificate'),
                privileges: Mdc.privileges.SecurityAccessor.canAdmin(),
                checkPassive: true,
                hidden: !Ext.isEmpty(this.record) && !this.record.get('passiveCertificate'),
                action: 'activatePassiveCertificate',
                itemId: 'menu-sa-activate-passive-certificate',
                section: this.SECTION_EDIT
            }
        ];
        this.callParent(arguments);
    },
    updateMenuItems: function (record) {
        this.down('#menu-sa-clear-passive-certificate')
        && this.down('#menu-sa-clear-passive-certificate')
                .setVisible(!this.deviceTypeId && record.get('defaultValue'));
        this.down('#menu-sa-activate-passive-certificate')
        &&  this.down('#menu-sa-activate-passive-certificate')
                .setVisible(!this.deviceTypeId && record.get('defaultValue'));
    }
});