/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.CertificatePreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.certificate-preview',
    requires: [
    ],

    initComponent: function () {
        var me = this;
        me.items = {
            xtype: 'form',
            items: [
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: '',
                    labelAlign: 'top',
                    layout: 'vbox',
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 250
                    },
                    items: [
                        {
                            fieldLabel: Uni.I18n.translate('general.alias', 'PKJ', 'Alias'),
                            name: 'alias'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('general.status', 'PKJ', 'Status'),
                            name: 'status'
                        }
                    ]
                }
                //,
                //{
                //    xtype: 'fieldcontainer',
                //    fieldLabel: Uni.I18n.translate('general.keyAttributes', 'PKJ', 'Key attributes'),
                //    itemId: 'pkj-certificate-preview-key-attributes-container',
                //    labelAlign: 'top',
                //    layout: 'vbox',
                //    defaults: {
                //        xtype: 'displayfield',
                //        labelWidth: 250
                //    },
                //    items: [
                //        {
                //            fieldLabel: Uni.I18n.translate('general.keyEncryptionMethod', 'PKJ', 'Key encryption method'),
                //            name: 'keyEncryptionMethod'
                //        }
                //    ]
                //},
                //{
                //    xtype: 'fieldcontainer',
                //    fieldLabel: Uni.I18n.translate('general.csrAttributes', 'PKJ', 'CSR attributes'),
                //    itemId: 'pkj-certificate-preview-csr-attributes-container',
                //    labelAlign: 'top',
                //    layout: 'vbox',
                //    defaults: {
                //        xtype: 'displayfield',
                //        labelWidth: 250
                //    },
                //    items: [
                //        {
                //            fieldLabel: Uni.I18n.translate('general.keyEncryptionMethod', 'PKJ', 'Key encryption method'),
                //            name: 'keyEncryptionMethod'
                //        }
                //    ]
                //},
                //{
                //    xtype: 'fieldcontainer',
                //    fieldLabel: Uni.I18n.translate('general.certificateAttributes', 'PKJ', 'Certificate attributes'),
                //    labelAlign: 'top',
                //    layout: 'vbox',
                //    defaults: {
                //        xtype: 'displayfield',
                //        labelWidth: 250
                //    },
                //    items: [
                //        {
                //            fieldLabel: Uni.I18n.translate('general.type', 'PKJ', 'Type'),
                //            name: 'type'
                //        },
                //        {
                //            fieldLabel: Uni.I18n.translate('general.issuer', 'PKJ', 'Issuer'),
                //            name: 'issuer'
                //        },
                //        {
                //            fieldLabel: Uni.I18n.translate('general.subject', 'PKJ', 'Subject'),
                //            name: 'subject'
                //        },
                //        {
                //            fieldLabel: Uni.I18n.translate('general.version', 'PKJ', 'Version'),
                //            name: 'version'
                //        },
                //        {
                //            fieldLabel: Uni.I18n.translate('general.serialNumber', 'PKJ', 'Serial number'),
                //            name: 'serialNumber'
                //        },
                //        {
                //            fieldLabel: Uni.I18n.translate('general.validFrom', 'PKJ', 'Valid from'),
                //            name: 'notBefore',
                //            renderer: function(value) {
                //                if (Ext.isEmpty(value)) {
                //                    return '-';
                //                }
                //                return Uni.DateTime.formatDateShort(new Date(value));
                //            }
                //        },
                //        {
                //            fieldLabel: Uni.I18n.translate('general.validTo', 'PKJ', 'Valid to'),
                //            name: 'notAfter',
                //            renderer: function(value) {
                //                if (Ext.isEmpty(value)) {
                //                    return '-';
                //                }
                //                return Uni.DateTime.formatDateShort(new Date(value));
                //            }
                //        },
                //        {
                //            fieldLabel: Uni.I18n.translate('general.signatureAlgorithm', 'PKJ', 'Signature algorithm'),
                //            name: 'signatureAlgorithm'
                //        }
                //    ]
                //}
            ]
        };
        me.callParent(arguments);
    },

    loadRecordInForm: function(certificateRecord) {
        var me = this;

        //me.down('#pkj-certificate-preview-key-attributes-container').setVisible(certificateRecord.get('hasPrivateKey'));
        me.down('form').loadRecord(certificateRecord);
    }

});