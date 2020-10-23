/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.AddCSRForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.csr-add-form',

    requires: [
        'Uni.util.FormErrorMessage'
    ],
    margin: '15 0 0 0',

    cancelLink: undefined,

    defaults: {
        labelWidth: 260,
        width: 600,
        msgTarget: 'under'
    },

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'uni-form-error-message',
                itemId: 'pkj-csr-add-form-errors',
                margin: '0 0 10 0',
                hidden: true
            },
            {
                xtype: 'textfield',
                fieldLabel: Uni.I18n.translate('general.alias', 'PKJ', 'Alias'),
                required: true,
                name: 'alias',
                itemId: 'pkj-csr-add-form-alias',
                allowBlank: false
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('general.keyAttribute', 'PKJ', 'Key attribute'),
                labelAlign: 'left'
            },
            {
                xtype: 'combo',
                fieldLabel: Uni.I18n.translate('general.keyEncryptionMethod', 'PKJ', 'Key encryption method'),
                emptyText: Uni.I18n.translate('general.selectKeyEncryptionMethod', 'PKJ', 'Select a key encryption method...'),
                store: 'Pkj.store.KeyEncryptionMethods',
                queryMode: 'local',
                displayField: 'name',
                valueField: 'id',
                editable: false,
                itemId: 'pkj-csr-add-form-key-encryption-method-combo',
                name: 'keyEncryptionMethod',
                required: true,
                allowBlank: false,
                forceSelection: true,
                listeners: {
                    select: function (combo, records) {
                        var endEntityCombo = me.down('#pkj-csr-add-form-end-entity');
                        var caCombo = me.down('#pkj-csr-add-form-ca-name');
                        var cpCombo = me.down('#pkj-csr-add-form-cert-profile');
                        var dnFields = me.down('#pkj-csr-add-form-subject-dn-fields');

                        endEntityCombo.getStore().load();
                        endEntityCombo.setDisabled(false);
                        endEntityCombo.setVisible(true);

                        caCombo.setDisabled(false);
                        caCombo.setVisible(true);

                        cpCombo.setDisabled(false);
                        cpCombo.setVisible(true);

                        dnFields.setDisabled(false);
                        dnFields.setVisible(true);

                    }
                }
            },
            {
                xtype: 'combo',
                fieldLabel: Uni.I18n.translate('general.ejbca.endentity', 'PKJ', 'End Entity'),
                emptyText: Uni.I18n.translate('general.select.ejbca.endentity', 'PKJ', 'Select an available end entity...'),
                store: 'Pkj.store.EJBCAEndEntities',
                queryMode: 'local',
                displayField: 'name',
                valueField: 'name',
                editable: false,
                disabled: true,
                hidden: true,
                itemId: 'pkj-csr-add-form-end-entity',
                name: 'endEntityName',
                allowBlank: false,
                required: true,
                forceSelection: true,
                listeners: {
                    select: function (combo, records) {
                        var eId = records[0].get('id');
                        var caCombo = me.down('#pkj-csr-add-form-ca-name');
                        var caStore = caCombo.getStore();
                        caCombo.setDisabled(false);
                        caStore.getProxy().setUrl(eId);
                        caStore.load();

                        var cpCombo = me.down('#pkj-csr-add-form-cert-profile');
                        var certStore = cpCombo.getStore();
                        certStore.getProxy().setUrl(eId);
                        cpCombo.setDisabled(false);
                        certStore.load();
                    }
                }
            },
            {
                xtype: 'combo',
                fieldLabel: Uni.I18n.translate('general.ejbca.caname', 'PKJ', 'CA Name'),
                emptyText: Uni.I18n.translate('general.select.ejbca.caname', 'PKJ', 'Select an available CA name...'),
                store: 'Pkj.store.EJBCACaNames',
                queryMode: 'local',
                displayField: 'name',
                valueField: 'name',
                editable: false,
                disabled: true,
                hidden: true,
                itemId: 'pkj-csr-add-form-ca-name',
                name: 'caName',
                allowBlank: false,
                required: true,
                forceSelection: true
            },
            {
                xtype: 'combo',
                fieldLabel: Uni.I18n.translate('general.ejbca.certprofile', 'PKJ', 'Certificate profile'),
                emptyText: Uni.I18n.translate('general.select.ejbca.certprofile', 'PKJ', 'Select an available certificate profile...'),
                store: 'Pkj.store.EJBCACertProfiles',
                queryMode: 'local',
                displayField: 'name',
                valueField: 'name',
                editable: false,
                disabled: true,
                hidden: true,
                itemId: 'pkj-csr-add-form-cert-profile',
                name: 'certificateProfileName',
                allowBlank: false,
                required: true,
                forceSelection: true
            },
            {
                xtype: 'textfield',
                fieldLabel: Uni.I18n.translate('general.ejbca.subjectdnfields', 'PKJ', 'Subject DN fields'),
                required: false,
                name: 'subjectDnFields',
                itemId: 'pkj-csr-add-form-subject-dn-fields',
                allowBlank: true,
                disabled: true,
                hidden: true
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('general.csrAttribute', 'PKJ', 'CSR attributes'),
                labelAlign: 'left'
            },
            {
                xtype: 'combo',
                fieldLabel: Uni.I18n.translate('general.certificateType', 'PKJ', 'Certificate type'),
                emptyText: Uni.I18n.translate('general.selectCertificateType', 'PKJ', 'Select a certificate type...'),
                store: 'Pkj.store.CertificateTypes',
                displayField: 'name',
                valueField: 'id',
                editable: false,
                itemId: 'pkj-csr-add-form-certificate-type-combo',
                name: 'keyTypeId',
                required: true,
                allowBlank: false,
                forceSelection: true
            },
            {
                xtype: 'textfield',
                fieldLabel: Uni.I18n.translate('general.csrAttribute.commonName', 'PKJ', 'Common name (CN)'),
                required: true,
                name: 'CN',
                itemId: 'pkj-csr-add-form-common-name',
                allowBlank: false
            },
            {
                xtype: 'textfield',
                fieldLabel: Uni.I18n.translate('general.csrAttribute.organisationalUnit', 'PKJ', 'Organisational unit (OU)'),
                name: 'OU',
                itemId: 'pkj-csr-add-form-organisational-unit'
            },
            {
                xtype: 'textfield',
                fieldLabel: Uni.I18n.translate('general.csrAttribute.organisation', 'PKJ', 'Organisation (O)'),
                name: 'O',
                itemId: 'pkj-csr-add-form-organisation'
            },
            {
                xtype: 'textfield',
                fieldLabel: Uni.I18n.translate('general.csrAttribute.locality', 'PKJ', 'Locality (L)'),
                name: 'L',
                itemId: 'pkj-csr-add-form-locality'
            },
            {
                xtype: 'textfield',
                fieldLabel: Uni.I18n.translate('general.csrAttribute.stateOrProvince', 'PKJ', 'State or province (ST)'),
                name: 'ST',
                itemId: 'pkj-csr-add-form-state'
            },
            {
                xtype: 'textfield',
                fieldLabel: Uni.I18n.translate('general.csrAttribute.country', 'PKJ', 'Country (C)'),
                name: 'C',
                itemId: 'pkj-csr-add-form-country'
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: '&nbsp;'
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: '&nbsp;',
                layout: {
                    type: 'hbox'
                },
                items: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.add', 'PKJ', 'Add'),
                        ui: 'action',
                        itemId: 'pkj-csr-add-form-add-btn'
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.cancel', 'PKJ', 'Cancel'),
                        ui: 'link',
                        itemId: 'pkj-csr-add-form-cancel-link',
                        href: me.cancelLink
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});