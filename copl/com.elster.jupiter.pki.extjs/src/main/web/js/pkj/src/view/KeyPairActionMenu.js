/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.KeyPairActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.key-pair-action-menu',
    requires:[
        'Pkj.privileges.CertificateManagement'
    ],
    initComponent: function () {
        var me = this;

        me.items = [
            {
                text: Uni.I18n.translate('general.downloadPublicKey', 'PKJ', 'Download public key'),
                itemId: 'pkj-download-public-key-menu-item',
                privileges: Pkj.privileges.CertificateManagement.adminCertificates,
                action: 'downloadPublicKey',
                visible: function(record) {
                    return !Ext.isEmpty(record) && record.get('hasPublicKey');
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
            // {
            //     text: Uni.I18n.translate('general.importCertificate', 'PKJ', 'Import certificate'),
            //     itemId: 'pkj-import-certificate-menu-item',
            //     privileges: Pkj.privileges.CertificateManagement.adminCertificates,
            //     action: 'importCertificate',
            //     section: this.SECTION_ACTION
            // },
            {
                text: Uni.I18n.translate('general.remove', 'PKJ', 'Remove'),
                itemId: 'pkj-remove-key-pair-menu-item',
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