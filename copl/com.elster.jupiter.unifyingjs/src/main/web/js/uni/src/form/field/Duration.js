/**
 * @class Uni.form.field.Duration
 */
Ext.define('Uni.form.field.Duration', {
    extend: 'Ext.form.field.Display',
    xtype: 'uni-form-field-duration',

    requires: [
        'Uni.util.String'
    ],

    fieldLabel: Uni.I18n.translate('general.duration', 'UNI', 'Duration'),

    /**
     * @cfg usesSeconds
     *
     * If the duration field gets its value in seconds or not.
     * By default it assumes a millisecond value.
     */
    usesSeconds: false,

    initComponent: function () {
        this.callParent(arguments);
    },

    setRawValue: function (value) {
        var me = this;

        if (!isNaN(value)) {
            value = me.usesSeconds ? value * 1000 : value;
            arguments[0] = Uni.util.String.formatDuration(parseInt(value, 10));
        }

        me.callParent(arguments);
    }
});