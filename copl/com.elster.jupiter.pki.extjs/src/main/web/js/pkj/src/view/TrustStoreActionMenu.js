/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.TrustStoreActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.truststore-action-menu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'pkj-edit-truststore-action',
                text: Uni.I18n.translate('general.edit', 'PKJ', 'Edit'),
                //privileges: Sct.privileges.ServiceCallType.admin,
                action: 'editTrustStore',
                section: this.SECTION_EDIT
            },
            {
                itemId: 'pkj-remove-truststore-action',
                text: Uni.I18n.translate('general.remove', 'PKJ', 'Remove'),
                //privileges: Sct.privileges.ServiceCallType.admin,
                action: 'removeTrustStore',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});