/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.TrustedCertificateActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.trusted-certificate-action-menu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'pkj-trusted-certificate-download-action',
                text: Uni.I18n.translate('general.download', 'PKJ', 'Download'),
                //privileges: Sct.privileges.ServiceCallType.admin,
                action: 'downloadTrustedCertificate',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'pkj-trusted-certificate-remove-action',
                text: Uni.I18n.translate('general.remove', 'PKJ', 'Remove'),
                //privileges: Sct.privileges.ServiceCallType.admin,
                action: 'removeTrustedCertificate',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});