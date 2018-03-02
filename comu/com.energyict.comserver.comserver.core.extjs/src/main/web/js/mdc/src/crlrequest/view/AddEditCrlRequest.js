/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.crlrequest.view.AddEditCrlRequest', {
    extend: 'Ext.form.Panel',
    alias: 'widget.crl-request-addedit-tgm',

    requires: [
        'Mdc.crlrequest.store.CrlDeviceGroups',
        'Mdc.crlrequest.store.CrlRequests'
    ],
    defaults: {
        labelWidth: 250,
        validateOnChange: false,
        validateOnBlur: false
    },

    initComponent: function () {
        var me = this,
            certAliasStore = Ext.create('Mdc.securityaccessors.store.CertificateAliases');

        certAliasStore.getProxy().setUrl('/api/dtc/securityaccessors');

        me.items = [
            {
                xtype: 'combobox',
                name: 'deviceGroup',
                emptyText: Uni.I18n.translate('general.selectADeviceGroup', 'MDC', 'Select a device group...'),
                itemId: 'cmb-device-group',
                fieldLabel: Uni.I18n.translate('general.deviceGroup', 'MDC', 'Device group'),
                store: 'Mdc.crlrequest.store.CrlDeviceGroups',
                queryMode: 'local',
                editable: false,
                displayField: 'name',
                valueField: 'id',
                allowBlank: false,
                required: true,
                width: 600,
                listeners: {
                    afterrender: function (field) {
                        field.focus(false, 200);
                    }
                }
            },
            {
                xtype: 'combobox',
                itemId: 'cbo-security-accessor',
                name: 'securityAccessor',
                width: 600,
                fieldLabel: Uni.I18n.translate('general.securityAccessor', 'MDC', 'Security accessor'),
                labelWidth: 250,
                store: 'Mdc.securityaccessors.store.SecurityAccessors',
                disabled: false,
                emptyText: Uni.I18n.translate('crlrequest.securityAccessorPrompt', 'MDC', 'Select security accessor...'),
                queryDelay: 500,
                queryCaching: false,
                minChars: 0,
                allowBlank: false,
                loadStore: false,
                forceSelection: false,
                displayField: 'name',
                queryMode: 'remote',
                queryParam: 'like',
                valueField: 'id',
                listeners: {
                    select: function (combo, records, eOpts) {
                        console.log(records);
                        console.log(records[0].get('keyType').isKey);
                        console.log(records[0].get('manageCentrally'));
                        var certificate = me.down('#cbo-certificate-alias');
                        certificate.setVisible(records[0].get('keyType').isKey === false);
                        certificate.setDisabled(records[0].get('keyType').isKey === true || records[0].get('manageCentrally') === false);

                    }
                }
            },
            {
                xtype: 'combobox',
                itemId: 'cbo-certificate-alias',
                name: 'certificateAlias',
                hidden: true,
                width: 600,
                fieldLabel: Uni.I18n.translate('crlrequest.certificateAlias', 'MDC', 'Certificate alias'),
                labelWidth: 250,
                store: certAliasStore,
                disabled: false,
                emptyText: Uni.I18n.translate('crlrequest.certificateAliasPrompt', 'MDC', 'Start typing to select...'),
                queryDelay: 500,
                queryCaching: false,
                minChars: 0,
                allowBlank: false,
                loadStore: false,
                forceSelection: false,
                displayField: 'alias',
                queryMode: 'remote',
                queryParam: 'like',
                valueField: 'id'
            },
            {
                xtype: 'textfield',
                name: 'caName',
                itemId: 'txt-caName',
                required: true,
                allowBlank: false,
                fieldLabel: Uni.I18n.translate('crlrequest.caName', 'MDC', 'CA name'),
            }
        ];

        me.callParent(arguments);
    },

});
