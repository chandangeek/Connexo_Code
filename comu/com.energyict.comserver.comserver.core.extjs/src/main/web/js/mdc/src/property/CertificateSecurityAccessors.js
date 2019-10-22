/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.property.CertificateSecurityAccessors', {
    extend: 'Uni.property.view.property.Base',

    require: [
        'Mdc.securityaccessors.store.DeviceSecurityCertificates'
    ],

    store: null,

    initComponent: function() {
        var me = this,
            deviceName = me.parentForm.context.deviceName;

        me.store = Ext.getStore('Mdc.securityaccessors.store.DeviceSecurityCertificates');

        me.callParent();
        me.store.getProxy().setExtraParam('deviceId', deviceName);
    },

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'combobox',
            itemId: me.key + 'combobox',
            name: this.getName(),
            store: me.store,
            minChars: 0,
            displayField: 'name',
            valueField: 'name',
            width: me.width,
            readOnly: me.isReadOnly,
            blankText: me.blankText,
            emptyText: Uni.I18n.translate('securityacessrors.certSecuritySets', 'MDC', 'Select security accessor type...'),
        }
    },

    getField: function () {
        return this.down('combobox');
    },

    markInvalid: function (error) {
        this.down('combobox').markInvalid(error);
    },

    clearInvalid: function (error) {
        this.down('combobox') && this.down('combobox').clearInvalid();
    }
});
