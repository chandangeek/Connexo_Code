/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.property.CertificateSecurityAccessors', {
    extend: 'Uni.property.view.property.Base',

    initComponent: function() {
        var me = this,
            deviceNameToSet = me.parentForm.context.deviceName;

        me.securityAccessorsStore = Ext.create('Ext.data.Store', {
            model: 'Mdc.securityaccessors.model.DeviceSecurityKey',
            proxy: {
                type: 'rest',
                urlTpl: '/api/ddr/devices/{deviceNameToSet}/securityaccessors/keys',
                reader: {
                    type: 'json',
                    root: 'keys'
                },
                setUrl: function (deviceNameToSet) {
                    this.url = this.urlTpl.replace('{deviceNameToSet}', deviceNameToSet);
                }
            }
        });

        me.callParent();
        me.securityAccessorsStore.getProxy().setUrl(deviceNameToSet);
    },

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'combobox',
            itemId: me.key + 'combobox',
            name: this.getName(),
            store: me.securityAccessorsStore,
            minChars: 0,
            displayField: 'name',
            valueField: 'id',
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
