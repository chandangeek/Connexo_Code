/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.CertificatesGridActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.certificates-grid-action-menu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.addCertificate', 'PKJ', 'Add certificate'),
                itemId: 'pkj-certificates-grid-add-certificate',
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.addCSR', 'PKJ', 'Add CSR'),
                itemId: 'pkj-certificates-grid-add-csr',
                section: this.SECTION_ACTION
            }
        ];
        this.callParent(arguments);
    }
});