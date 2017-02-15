/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.search.field.internal.InputObis', {
    extend: 'Uni.view.search.field.internal.Input',
    xtype: 'uni-search-internal-input-obis',
    requires: [
        'Uni.form.field.Obis'
    ],

    init: function () {
        var me = this;

        Ext.apply(Ext.form.VTypes, {
            obisCode: function (val, field) {
                var obis = /^(0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5]|[x])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5]))$/;
                return obis.test(val);
            },
            obisCodeText: Uni.I18n.translate('obis.error', 'UNI', 'This field contains an invalid OBIS code.')
        });

        me.callParent(arguments);
        me.items = {
            itemId: 'filter-input',
            xtype: 'textfield',
            flex: 1,
            emptyText: Uni.I18n.translate('obis.mask', 'UNI', 'x.x.x.x.x.x'),
            fieldStyle: {
                border: 0,
                margin: 0
            },
            tooltip: Uni.I18n.translate('obis.info', 'UNI', 'Provide the values for the 6 attributes of the OBIS code, separated by a "."'),
            maskRe: /[\dx.]+/,
            vtype: 'obisCode',
            listeners: {
                blur: function (field, e, eOpts) {
                    var str = field.value;
                    var regex = /^(0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5]))$/;
                    return (regex.test(str)) ? (regex.test(str)) : field.reset();
                },
                change: {
                    fn: me.onChange,
                    scope: me
                }
            }
        };
    }
});