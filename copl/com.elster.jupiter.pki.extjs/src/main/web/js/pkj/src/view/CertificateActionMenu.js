/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.CertificateActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.certificate-action-menu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.downloadCSR', 'PKJ', 'Download CSR'),
                itemId: 'pkj-certificates-grid-download-csr',
                //privileges: Sct.privileges.ServiceCallType.admin,
                action: 'downloadCSR',
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.importCertificate', 'PKJ', 'Import certificate'),
                itemId: 'pkj-certificates-grid-import-certificate',
                //privileges: Sct.privileges.ServiceCallType.admin,
                action: 'importCertificate',
                section: this.SECTION_ACTION
            }
        ];
        this.callParent(arguments);
    }
});