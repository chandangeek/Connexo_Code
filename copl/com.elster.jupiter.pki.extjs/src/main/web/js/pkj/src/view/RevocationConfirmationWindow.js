/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.RevocationConfirmationWindow', {
    extend: 'Uni.view.window.Confirmation',
    xtype: 'confirmation-window',
    requires: [
        'Pkj.store.RevocationTimeouts'
    ],

    itemId: 'revocation-confirmation-window',
    bindRecordId: undefined,
    certificatesView: undefined,
    //default
    timeout: 30000,
    confirmText: Uni.I18n.translate('general.revoke', 'PKJ', 'Revoke'),

    initComponent: function () {
        var me = this;
        me.callParent(arguments);
        me.insert(1, {
            xtype: 'combobox',
            itemId: 'revocation-timeout-combobox',
            name: 'revocationTimeout',
            fieldLabel: Uni.I18n.translate('general.timeout', 'PKJ', 'Timeout'),
            store: Ext.getStore('Pkj.store.RevocationTimeouts') || Ext.create('Pkj.store.RevocationTimeouts'),
            queryMode: 'local',
            displayField: 'label',
            valueField: 'timeout',
            value: me.timeout,
            editable: false,
            margin: '0 15 15 15',
            listeners: {
                change: {
                    fn: function (field, newValue) {
                        if (!Ext.isEmpty(newValue)) {
                            me.timeout = newValue;
                        }
                    }
                }
            }
        });
        me.insert(2, {
            xtype: 'panel',
            itemId: 'pnl-revocation-progress',
            layout: 'fit',
            padding: '0 25 15 60'
        });
    },

    //override
    confirmation: function () {
        this.confirm();
    },

    show: function (config) {
        if (!Ext.isDefined(config.title)) {
            Ext.apply(config, {
                title: config.caOnline ?
                    Uni.I18n.translate('certificate.revoke.confirm.title.caOnline', 'PKJ', 'Revoke certificate?') :
                    Uni.I18n.translate('certificate.revoke.confirm.title.caOffline', 'PKJ', 'Mark certificate as revoked?')
            });
        }
        if (!Ext.isDefined(config.msg)) {
            Ext.apply(config, {
                msg: config.caOnline ?
                    Uni.I18n.translate('certificate.revoke.confirm.body.caOnline', 'PKJ',
                        'A request for revoking the certificate will be sent to the Certification Authority') :
                    Uni.I18n.translate('certificate.revoke.confirm.body.caOffline', 'PKJ',
                        'The certificate will be marked as revoked in the system but you need to manually send it to the Certificate Authority that will change it revocation status')
            });
        }

        this.callParent(arguments);
    },

    confirm: function () {
        var me = this;
        var url = '/api/pir/certificates/' + me.bindRecordId + '/revoke?timeout=' + me.timeout;

       var pb = me.down('#pnl-revocation-progress').add(Ext.create('Ext.ProgressBar', {
            xtype: 'progressbar',
            itemId: 'revocation-progressbar',
        }));

        pb.wait({
            duration: me.timeout,
            interval: 100,
            increment: me.timeout / 100,
            text: Uni.I18n.translate('certificate.revoke.progress.test', 'PKJ', 'Request to CA is in progress. Please wait...')
        });

        me.disableClickables();

        Ext.Ajax.request({
            url: url,
            method: 'POST',
            //ajax request timeout increased to specified one by user plus 5 sec extra
            timeout: me.timeout + 5000,
            callback: function (config, success, response) {
                pb.destroy();
                me.hide();
                if (success) {
                    me.certificatesView.getApplication().fireEvent('acknowledge', Uni.I18n.translate('certificate.revoked', 'PKJ', "Certificate revoked"));
                    me.certificatesView.navigateToCertificatesOverview();
                }
            }
        });

    },

    disableClickables: function () {
        var me = this;
        me.down('#confirm-button').disable();
        me.down('#cancel-button').disable();
        me.down('#revocation-timeout-combobox').disable();
    }
});