Ext.define('Pkj.view.AddKeyPair', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.key-pair-add',
    requires: [
       'Pkj.view.AddKeyPairForm'
    ],

    keyPairRecord: undefined,
    cancelLink: undefined,
    importMode: false,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: me.importMode
                    ? Uni.I18n.translate('general.importKeyPair', 'PKJ', 'Import key pair')
                    : Uni.I18n.translate('general.generateKayPair', 'PKJ', 'Generate key pair'),
                items: {
                    xtype: 'key-pair-add-form',
                    autoEl: {
                        tag: 'form',
                        enctype: 'multipart/form-data'
                    },
                    certificateRecord: this.certificateRecord,
                    cancelLink: this.cancelLink,
                    importMode: me.importMode
                }
            }
        ];
        me.callParent(arguments);
    }
});