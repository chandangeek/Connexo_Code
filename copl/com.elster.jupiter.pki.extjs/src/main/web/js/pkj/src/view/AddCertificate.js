/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.AddCertificate', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.certificate-add',
    requires: [
        'Pkj.view.AddCertificateForm'
    ],

    record: undefined,
    cancelLink: undefined,
    importMode: false,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: me.importMode
                    ? Uni.I18n.translate('general.importCertificate', 'PKJ', 'Import certificate')
                    : Uni.I18n.translate('general.addCertificate', 'PKJ', 'Add certificate'),
                items: {
                    xtype: 'certificate-add-form',
                    autoEl: {
                        tag: 'form',
                        enctype: 'multipart/form-data'
                    },
                    record: this.record,
                    cancelLink: this.cancelLink,
                    importMode: me.importMode
                }
            }
        ];
        me.callParent(arguments);
    }
});