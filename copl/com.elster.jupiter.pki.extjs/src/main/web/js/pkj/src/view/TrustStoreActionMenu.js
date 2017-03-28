/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.TrustStoreActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.truststore-action-menu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.edit', 'PKJ', 'Edit'),
                itemId: 'pkj-edit-truststore-action',
                //privileges: Sct.privileges.ServiceCallType.admin,
                action: 'editTrustStore',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.importTrustedCertificates', 'PKJ', 'Import trusted certificates'),
                itemId: 'pkj-import-trusted-certificates-action',
                //privileges: Sct.privileges.ServiceCallType.admin,
                action: 'importTrustedCertificates',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.remove', 'PKJ', 'Remove'),
                itemId: 'pkj-remove-truststore-action',
                //privileges: Sct.privileges.ServiceCallType.admin,
                action: 'removeTrustStore',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});