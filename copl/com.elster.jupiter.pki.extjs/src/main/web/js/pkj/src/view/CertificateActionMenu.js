/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.CertificateActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.certificate-action-menu',
    requires:[
        'Pkj.privileges.CertificateManagement'
    ],
    initComponent: function () {
        var me = this;

        me.items = [
            {
                text: Uni.I18n.translate('general.downloadCertificate', 'PKJ', 'Download certificate'),
                itemId: 'pkj-download-certificate-menu-item',
                privileges: Pkj.privileges.CertificateManagement.adminCertificates,
                action: 'downloadCertificate',
                visible: function(record) {
                    return !Ext.isEmpty(record) && record.get('hasCertificate');
                },
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.downloadCSR', 'PKJ', 'Download CSR'),
                itemId: 'pkj-download-csr-menu-item',
                privileges: Pkj.privileges.CertificateManagement.adminCertificates,
                action: 'downloadCSR',
                visible: function(record) {
                    return !Ext.isEmpty(record) && record.get('hasCSR');
                },
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
            },
            {
                text: Uni.I18n.translate('general.obsolete', 'PKJ', 'Mark as obsolete'),
                itemId: 'pkj-obsolete-certificate-menu-item',
                privileges: Pkj.privileges.CertificateManagement.adminCertificates,
                action: 'obsoleteCertificate',
                visible: function(record) {
                    return !Ext.isEmpty(record) && record.get('status') === 'Available';
                },
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.unmarkObsolete', 'PKJ', 'Unmark obsolete'),
                itemId: 'pkj-cancel-obsolete-certificate-menu-item',
                privileges: Pkj.privileges.CertificateManagement.adminCertificates,
                action: 'cancelObsoleteCertificate',
                visible: function(record) {
                    return !Ext.isEmpty(record) && record.get('status') === 'Obsolete';
                },
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.revoke', 'PKJ', 'Revoke'),
                itemId: 'pkj-revoke-certificate-menu-item',
                privileges: Pkj.privileges.CertificateManagement.adminCertificates,
                action: 'revokeCertificate',
                visible: function(record) {
                    return !Ext.isEmpty(record) && (record.get('status') !== 'Requested' && record.get('status') !== 'Revoked');
                },
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.requestCertificate', 'PKJ', 'Request certificate'),
                itemId: 'pkj-request-certificate-menu-item',
                privileges: Pkj.privileges.CertificateManagement.adminCertificates,
                action: 'requestCertificate',
                visible: function(record) {
                    return !Ext.isEmpty(record) && (record.get('status') !== 'Requested');
                },
                section: this.SECTION_ACTION
            }

        ];
        me.callParent(arguments);
    },

    listeners: {
        beforeshow: {
            fn: function (menu) {
                var me = this,
                    visible = true;

                me.items.each(function(item) {
                    visible = true;
                    if (Ext.isDefined(item.visible)) {
                        visible = visible && item.visible.call(me, menu.record);
                    }
                    item.setVisible(visible);
                });
            }
        }
    }

});