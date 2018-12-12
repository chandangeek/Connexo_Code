/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.TrustedCertificateActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.trusted-certificate-action-menu',
    requires: [
        'Pkj.privileges.CertificateManagement'
    ],
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.download', 'PKJ', 'Download'),
                itemId: 'pkj-trusted-certificate-download-action',
                privileges: Pkj.privileges.CertificateManagement.adminTrustStores,
                action: 'downloadTrustedCertificate',
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.remove', 'PKJ', 'Remove'),
                itemId: 'pkj-trusted-certificate-remove-action',
                privileges: Pkj.privileges.CertificateManagement.adminTrustStores,
                action: 'removeTrustedCertificate',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});