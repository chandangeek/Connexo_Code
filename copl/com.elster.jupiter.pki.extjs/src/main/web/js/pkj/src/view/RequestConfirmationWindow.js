/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Pkj.view.RequestConfirmationWindow', {
    extend: 'Uni.view.window.Confirmation',

    alias: 'widget.certificate-request-window',
    modal: true,
    minWidth: 400,

    requires: [
        'Pkj.store.CertificateTimeouts'
    ],

    initComponent: function () {
        var me = this;

        me.callParent(arguments);
        me.insert(1, [{
            xtype: 'combobox',
            itemId: 'request-timeout-combobox',
            name: 'revocationTimeout',
            fieldLabel: Uni.I18n.translate('general.timeout', 'PKJ', 'Timeout'),
            store: Ext.getStore('Pkj.store.CertificateTimeouts') || Ext.create('Pkj.store.CertificateTimeouts'),
            queryMode: 'local',
            displayField: 'label',
            valueField: 'timeout',
            value: 30000,
            editable: false,
            margin: '0 15 15 15'
        },{
            xtype: 'panel',
            itemId: 'request-progress',
            layout: 'fit',
            padding: '0 25 15 60'
        }]);
    }
});