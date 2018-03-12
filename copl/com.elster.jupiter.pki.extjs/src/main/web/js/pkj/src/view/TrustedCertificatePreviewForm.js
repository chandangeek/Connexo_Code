/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.TrustedCertificatePreviewForm', {
    extend: 'Ext.form.Panel',
    frame: false,
    layout: 'fit',
    alias: 'widget.trusted-certificate-preview-form',

    requires: [
        'Uni.util.FormEmptyMessage'
    ],

    items: [
        {
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
                    fieldLabel: Uni.I18n.translate('general.certificateAttributes', 'PKJ', 'Certificate attributes'),
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
                            name: 'certificateVersion'
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
                        }
                    ]
                }
            ]
        }
    ]
});
