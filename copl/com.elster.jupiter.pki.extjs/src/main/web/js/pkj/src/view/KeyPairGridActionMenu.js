Ext.define('Pkj.view.KeyPairGridActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.key-pair-grid-action-menu',
    requires: [
        'Pkj.privileges.CertificateManagement'
    ],
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.generateKeyPair', 'PKJ', 'Generate key pair'),
                itemId: 'pkj-key-pair-grid-generate',
                privileges: Pkj.privileges.CertificateManagement.adminCertificates,
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.importKeyPair', 'PKJ', 'Import key pair'),
                itemId: 'pkj-key-pair-grid-import',
                privileges: Pkj.privileges.CertificateManagement.adminCertificates,
                section: this.SECTION_ACTION
            }
        ];
        this.callParent(arguments);
    }
});