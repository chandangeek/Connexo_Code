/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.AddKeyPairForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.key-pair-add-form',

    requires: [
        'Pkj.view.KeyPairFileField',
        'Uni.util.FormErrorMessage',
        'Uni.util.FormInfoMessage'
    ],
    layout: 'anchor',
    margin: '15 0 0 0',

    cancelLink: undefined,
    certificateRecord: undefined,
    importMode: false,
    defaults: {
        labelWidth: 250
    },
    initComponent: function () {
        var me = this;

        me.items = [

            {
                xtype: 'uni-form-error-message',
                itemId: 'pkj-key-pair-add-form-errors',
                margin: '0 0 10 0',
                anchor: '40%',
                hidden: true
            },
            {
                xtype: 'textfield',
                fieldLabel: Uni.I18n.translate('general.alias', 'PKJ', 'Alias'),
                required: true,
                name: 'alias',
                itemId: 'pkj-key-pair-add-form-alias',
                allowBlank: !me.importMode,
                anchor: '40%',
                //hidden: me.importMode,
                vtype: 'checkForBlacklistCharacters'
            },
            {
                xtype: 'combobox',
                fieldLabel: Uni.I18n.translate('general.storageMethod', 'PKJ', 'Storage method'),
                emptyText: Uni.I18n.translate('general.selectKeyEncryptionMethod', 'PKJ', 'Select a key encryption method...'),
                store: 'Pkj.store.KeyEncryptionMethods',
                queryMode: 'local',
                displayField: 'name',
                valueField: 'id',
                editable: false,
                itemId: 'pkj-key-pair-add-form-key-encryption-method-combo',
                name: 'keyEncryptionMethod',
                required: true,
                allowBlank: false,
                forceSelection: true,
                anchor: '40%'
            },
            {
                xtype: 'combobox',
                fieldLabel: Uni.I18n.translate('general.keyType', 'PKJ', 'Key type'),
                store: 'Pkj.store.KeyPairTypes',
                required: true,
                queryMode: 'local',
                displayField: 'name',
                valueField: 'id',
                editable: false,
                allowBlank: false,
                forceSelection: true,
                name: 'keyType',
                hidden: true,
                itemId: 'pkj-key-pair-add-form-key-type-combo',
                anchor: '40%'
            },
            {
                xtype: 'textarea',
                itemId: 'pkj-key-pair-add-form-file',
                fieldLabel: Uni.I18n.translate('general.key', 'PKJ', 'Key'),
                name: 'key',
                required: true,
                anchor: '40%',
                height: 200,
                hidden: true
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: '&nbsp;',
                anchor: '40%',
                layout: {
                    type: 'hbox'
                },
                items: [
                    {
                        xtype: 'button',
                        text: me.importMode
                            ? Uni.I18n.translate('general.import', 'PKJ', 'Import')
                            : Uni.I18n.translate('general.add', 'PKJ', 'Add'),
                        ui: 'action',
                        action: me.importMode?'import':'add',
                        itemId: 'pkj-key-pair-add-form-add-btn'
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.cancel', 'PKJ', 'Cancel'),
                        ui: 'link',
                        itemId: 'pkj-key-pair-add-form-cancel-link',
                        href: me.cancelLink
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});