/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.controller.KeyPairs', {
    extend: 'Ext.app.Controller',

    views: [
        'Pkj.view.KeyPairOverview',
        'Pkj.view.AddKeyPair'
    ],
    stores: [
         'Pkj.store.KeyPairs',
         'Pkj.store.KeyEncryptionMethods',
        'Pkj.store.KeyPairTypes'
    ],
    models: [
         'Pkj.model.Certificate',
         'Pkj.model.KeyEncryptionMethod'
    ],

    refs: [
        {
            ref: 'keyPairPreview',
            selector: 'key-pair-overview key-pair-preview'
        },
        {
            ref: 'keyPairAddForm',
            selector: 'key-pair-add key-pair-add-form'
        },

    ],

    init: function() {
         this.control({
            '#pkj-key-pair-grid': {
                select: this.onKeyPairSelected
            },
            '#pkj-key-pair-grid-generate': {
                click: this.navigateToGenerateKeyPairPage
            },
             '#pkj-key-pair-grid-import': {
                 click: this.navigateToImportKeyPairPage
             },
            '#pkj-key-pair-add-form-key-encryption-method-combo': {
                select: this.storageSelected
            },
            '#pkj-key-pair-add-form-add-btn[action=add]': {
                click: this.addKeyPair
            },
             '#pkj-key-pair-add-form-add-btn[action=import]': {
                 click: this.importKeyPair
             },
            '#pkj-remove-key-pair-menu-item': {
                click: this.removeKeyPair
            },
             '#pkj-download-public-key-menu-item':{
                click: this.downloadPublicKey
             },
             '#pkj-no-key-pair-generate-key-pair-btn':{
                 click: this.navigateToGenerateKeyPairPage
             },
             '#pkj-no-key-pair-import-key-pair-btn':{
                 click: this.navigateToImportKeyPairPage
             }
         });
    },

    showKeyPairs: function() {
        var store = Ext.getStore('Pkj.store.KeyPairs');
        this.getApplication().fireEvent('changecontentevent',
            Ext.widget('key-pair-overview',
                {
                    store: store,
                    router:this.getController('Uni.controller.history.Router')
                }
            )
        );
    },

    onKeyPairSelected: function(grid, record) {
        var me = this,
            menu = me.getKeyPairPreview().down('key-pair-action-menu');

        me.getKeyPairPreview().loadRecordInForm(record);
        me.getKeyPairPreview().setTitle(Ext.htmlEncode(record.get('alias')));
        if (menu) {
            menu.record = record;
        }
    },

    navigateToKeyPairOverview: function() {
        this.getController('Uni.controller.history.Router').getRoute('administration/keypairs').forward();
    },

    navigateToGenerateKeyPairPage: function() {
        this.getController('Uni.controller.history.Router').getRoute('administration/keypairs/generate').forward();
    },

    navigateToImportKeyPairPage: function() {
        this.getController('Uni.controller.history.Router').getRoute('administration/keypairs/import').forward();
    },


    storageSelected: function(combo){
        var keyTypestore = Ext.getStore('Pkj.store.KeyPairTypes');
        keyTypestore.load();
        combo.up('form').down('#pkj-key-pair-add-form-key-type-combo').setVisible(true);
        if(combo.up('form').importMode){
            combo.up('form').down('#pkj-key-pair-add-form-file').setVisible(true);
        }
    },

    showGenerateKeyPairPage: function() {
        var keyEncryptionMethodsStore = Ext.getStore('Pkj.store.KeyEncryptionMethods');
        keyEncryptionMethodsStore.load();
        this.getApplication().fireEvent('changecontentevent',
            Ext.widget('key-pair-add',
                {
                    importMode: false,
                    cancelLink: this.getController('Uni.controller.history.Router').getRoute('administration/keypairs').buildUrl()
                }
            )
        );
    },

    showImportKeyPairPage: function() {
        var keyEncryptionMethodsStore = Ext.getStore('Pkj.store.KeyEncryptionMethods');
        keyEncryptionMethodsStore.load();
        this.getApplication().fireEvent('changecontentevent',
            Ext.widget('key-pair-add',
                {
                    importMode: true,
                    cancelLink: this.getController('Uni.controller.history.Router').getRoute('administration/keypairs').buildUrl()
                }
            )
        );
    },

    addKeyPair: function() {
        var me = this,
            form = me.getKeyPairAddForm(),
             errorMsgPanel = form.down('uni-form-error-message');

        errorMsgPanel.hide();
        form.getForm().clearInvalid();

        form.setLoading();
        var values = form.getValues();
        values.keyType={id: values.keyType};
        Ext.Ajax.request({
            url: '/api/pir/keypairs',
            method: 'POST',
            jsonData: values,
            callback: function (config, success, response) {
                if (!Ext.isEmpty(response.responseText)) {
                    var responseObject = JSON.parse(response.responseText);
                    if (!success) {
                        if(responseObject.errors.length !== 0){
                            form.getForm().markInvalid(responseObject.errors);
                        }
                    } else {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.generateKeyPair.success', 'PKJ', 'Key pair generated'));
                        me.getController('Uni.controller.history.Router').getRoute('administration/keypairs').forward();
                    }
                }
                form.setLoading(false);
            }
        });
    },

    importKeyPair: function(){
        var me = this,
            form = me.getKeyPairAddForm(),
            errorMsgPanel = form.down('uni-form-error-message');

        errorMsgPanel.hide();
        form.getForm().clearInvalid();

        form.setLoading();
        var values = form.getValues();
        values.keyType={id: values.keyType};
        Ext.Ajax.request({
            url: '/api/pir/keypairs/import',
            method: 'POST',
            jsonData: values,
            callback: function (config, success, response) {
                if (!Ext.isEmpty(response.responseText)) {
                    var responseObject = JSON.parse(response.responseText);
                    if (!success) {
                        if(responseObject.errors.length !== 0){
                            form.getForm().markInvalid(responseObject.errors);
                        }
                    } else {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.generateKeyPair.success', 'PKJ', 'Key pair generated'));
                        me.getController('Uni.controller.history.Router').getRoute('administration/keypairs').forward();
                    }
                }
                form.setLoading(false);
            }
        });
    },

    removeKeyPair: function(menuItem){
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation'),
            keyPairRecord = menuItem.up('key-pair-action-menu').record;

        confirmationWindow.show({
            title: Uni.I18n.translate('general.removeX', 'PKJ', "Remove '{0}'?", keyPairRecord.get('alias')),
            msg: Uni.I18n.translate('keyPair.remove.msg', 'PKJ', 'The key pair will no longer be available.'),
            fn: function (state) {
                if (state === 'confirm') {
                    keyPairRecord.destroy({
                        success: function () {
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.keyPairRemoved', 'PKJ', 'Key pair removed'));
                            me.navigateToKeyPairOverview();
                        }
                    });
                }
            }
        });
    },

    downloadPublicKey: function(menuItem){
        var keyPairRecord = menuItem.up('key-pair-action-menu').record,
            url = '/api/pir/keypairs/' + keyPairRecord.get('id') + '/download/publickey';
        window.open(url, '_blank');
    }

});