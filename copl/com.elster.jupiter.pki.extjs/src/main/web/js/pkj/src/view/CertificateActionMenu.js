/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.CertificateActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.certificate-action-menu',
    initComponent: function () {
        var me = this;

        me.items = [
            {
                text: Uni.I18n.translate('general.downloadCertificate', 'PKJ', 'Download certificate'),
                itemId: 'pkj-download-certificate-menu-item',
                privileges: Pkj.privileges.CertificateManagement.adminCertificates,
                action: 'downloadCertificate',
                hidden: Ext.isEmpty(me.record) || !me.record.get('hasCertificate'),
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.downloadCSR', 'PKJ', 'Download CSR'),
                itemId: 'pkj-download-csr-menu-item',
                privileges: Pkj.privileges.CertificateManagement.adminCertificates,
                action: 'downloadCSR',
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.importCertificate', 'PKJ', 'Import certificate'),
                itemId: 'pkj-import-certificate-menu-item',
                privileges: Pkj.privileges.CertificateManagement.adminCertificates,
                action: 'importCertificate',
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.remove', 'PKJ', 'Remove'),
                itemId: 'pkj-remove-certificate-menu-item',
                privileges: Pkj.privileges.CertificateManagement.adminCertificates,
                action: 'removeCertificate',
                section: this.SECTION_REMOVE
            }
        ];
        me.callParent(arguments);
    },

    listeners: {
        show: {
            fn: function (menu) {
                menu.down('[action=downloadCertificate]').setVisible( !Ext.isEmpty(menu.record) && menu.record.get('hasCertificate') )
            }
        }
    }

});