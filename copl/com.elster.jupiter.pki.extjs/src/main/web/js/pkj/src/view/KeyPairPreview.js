/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.KeyPairPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.key-pair-preview',

    initComponent: function () {
        var me = this;
        me.items = {
            xtype: 'form',
            items: [
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.alias', 'PKJ', 'Alias'),
                    name: 'alias'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.availableKeys', 'PKJ', 'Available keys'),
                    dataIndex: 'hasPublicKey',
                    renderer: function(){
                        var keypair = me.down('form').getRecord();
                        var result = '-';
                        if(keypair){
                            if(keypair.get('hasPublicKey') && keypair.get('hasPrivateKey')) {
                                result = Uni.I18n.translate('general.hasPrivatePublicKey', 'PKJ', 'Private key and public key')
                            } else {
                                if(keypair.get('hasPublicKey')){
                                    result = Uni.I18n.translate('general.hasPublicKey', 'PKJ', 'Public key')
                                }
                                if(keypair.get(hasPrivateKey)){
                                    result = Uni.I18n.translate('general.hasPrivateKey', 'PKJ', 'Private key')
                                }
                            }
                        }
                        return result;
                    }
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.storgaMethod', 'PKJ', 'Storage method'),
                    dataIndex: 'keyEncryptionMethod'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.keyType', 'PKJ', 'Key type'),
                    dataIndex: 'keyType',
                    renderer: function(data){
                        return data.name;
                    }
                }
            ]
        };
        me.callParent(arguments);
    },

    loadRecordInForm: function(keyPairRecord) {
         var me = this;
         me.down('form').loadRecord(keyPairRecord);
    }

});