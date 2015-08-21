/**
 * @class Uni.form.field.Obis
 */
Ext.define('Uni.form.field.Obis', {
    extend: 'Ext.form.field.Text',
    requires: [
        'Ext.form.VTypes'
    ],
    xtype: 'obis-field',
    name: 'obisCode',
    cls: 'obisCode',
    msgTarget: 'under',
    fieldLabel: Uni.I18n.translate('obis.label', 'UNI', 'OBIS code'),
    emptyText: Uni.I18n.translate('obis.mask', 'UNI', 'x.x.x.x.x.x'),

    afterSubTpl:
        '<div class="x-form-display-field"><i>' +
        Uni.I18n.translate('obis.info', 'UNI', 'Provide the values for the 6 attributes of the Obis code, separated by a "."') +
        '</i></div>'
    ,
    maskRe: /[\d.]+/,
    vtype: 'obisCode',
    required: true,
    listeners: {
        blur: function( field, e, eOpts  ){
            var str=field.value;
            var regex = /^(0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5]))$/;
                return (regex.test(str)) ? (regex.test(str)) : field.reset();
            }
    }
    ,

    initComponent: function () {
        Ext.apply(Ext.form.VTypes, {
            obisCode: function (val, field) {
                var obis = /^(0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5]))$/;
                return obis.test(val);
            },
            obisCodeText: Uni.I18n.translate('obis.error', 'UNI', 'OBIS code is wrong')
        });
        this.callParent(this);
    }
});