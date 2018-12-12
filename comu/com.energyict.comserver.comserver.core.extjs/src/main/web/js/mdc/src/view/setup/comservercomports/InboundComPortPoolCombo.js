/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comservercomports.InboundComPortPoolCombo', {
    alias: 'widget.inbound-com-port-pool-combo',
    extend: 'Ext.form.field.ComboBox',
    name: 'comPortPool_id',
    fieldLabel: Uni.I18n.translate('general.comPortPool','MDC','Communication port pool'),
    queryMode: 'local',
    displayField: 'name',
    valueField: 'id',

    setValue: function (value) {
        this.callParent([Ext.isObject(value) ? value.id : value]);
    },

    getValue: function () {
        var me = this,
            value = me.callParent(arguments),
            result = null,
            record;

        if (!Ext.isEmpty(value)) {
            record = me.findRecordByValue(value);
            if (record) {
                result = _.pick(me.findRecordByValue(value).getData(), 'id', 'version');
            }
        }
        return result;
    }
});