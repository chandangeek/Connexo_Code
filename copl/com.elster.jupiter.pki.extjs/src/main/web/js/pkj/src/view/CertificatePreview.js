/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.CertificatePreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.certificate-preview',

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
                            name: 'status',
                            renderer: function(value) {
                                return value ? value.name : value;
                            }
                        }
                    ]
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('general.keyAttributes', 'PKJ', 'Key attributes'),
                    itemId: 'pkj-certificate-preview-key-attributes-container',
                    labelAlign: 'top',
                    layout: 'vbox',
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 250
                    },
                    items: [
                        {
                            fieldLabel: Uni.I18n.translate('general.keyEncryptionMethod', 'PKJ', 'Key encryption method'),
                            name: 'keyEncryptionMethod'
                        }
                    ]
                },
                {
                    xtype: 'fieldcontainer',
                    ui: 'medium',
                    fieldLabel: Uni.I18n.translate('general.certificateAttributes', 'PKJ', 'Certificate attributes'),
                    itemId: 'pkj-certificate-preview-certificate-attributes-container',
                    labelAlign: 'top',
                    layout: 'vbox',
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 250
                    },
                    items: [
                        {
                            fieldLabel: Uni.I18n.translate('general.keyUsage', 'PKJ', 'Key usage'),
                            name: 'type'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('general.issuer', 'PKJ', 'Issuer'),
                            name: 'issuer'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('general.subject', 'PKJ', 'Subject'),
                            name: 'subject'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('general.version', 'PKJ', 'Version'),
                            name: 'version'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('general.serialNumber', 'PKJ', 'Serial number'),
                            name: 'serialNumber'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('general.validFrom', 'PKJ', 'Valid from'),
                            name: 'notBefore',
                            renderer: function(value) {
                                if (Ext.isEmpty(value)) {
                                    return '-';
                                }
                                return Uni.DateTime.formatDateShort(new Date(value));
                            }
                        },
                        {
                            fieldLabel: Uni.I18n.translate('general.validTo', 'PKJ', 'Valid to'),
                            name: 'notAfter',
                            renderer: function(value) {
                                if (Ext.isEmpty(value)) {
                                    return '-';
                                }
                                return Uni.DateTime.formatDateShort(new Date(value));
                            }
                        },
                        {
                            fieldLabel: Uni.I18n.translate('general.signatureAlgorithm', 'PKJ', 'Signature algorithm'),
                            name: 'signatureAlgorithm'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('general.ejbca.endentity', 'PKJ', 'End Entity'),
                            name: 'endEntityName'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('general.ejbca.caname', 'PKJ', 'CA Name'),
                            name: 'caName'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('general.ejbca.certprofile', 'PKJ', 'Certificate profile'),
                            name: 'certProfileName'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('general.ejbca.subjectdnfields', 'PKJ', 'Subject DN fields'),
                            name: 'subjectDnFields'
                        }
                    ]
                }
            ]
        };
        me.callParent(arguments);
    },

    loadRecordInForm: function(certificateRecord) {
        var me = this,
            hasKey = certificateRecord.get('hasPrivateKey'),
            hasCSR = certificateRecord.get('hasCSR'),
            hasCertificate = certificateRecord.get('hasCertificate'),
            keyEncryptionMethod = certificateRecord.get('keyEncryptionMethod'),
            keyContainer = me.down('#pkj-certificate-preview-key-attributes-container'),
            certificateContainer = me.down('#pkj-certificate-preview-certificate-attributes-container');

        keyContainer.setVisible(hasKey || !Ext.isEmpty(keyEncryptionMethod));
        certificateContainer.setVisible(hasCertificate || hasCSR);
        if (hasCSR) {
            certificateContainer.setFieldLabel(Uni.I18n.translate('general.csrAttributes', 'PKJ', 'CSR attributes'));
        }
        if (hasCertificate) {
            certificateContainer.setFieldLabel(Uni.I18n.translate('general.certificateAttributes', 'PKJ', 'Certificate attributes'));
        }
        me.down('form').loadRecord(certificateRecord);
    }

});
