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