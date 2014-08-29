/**
 * @class Uni.form.field.IntervalFlagsDisplay
 */
Ext.define('Uni.form.field.IntervalFlagsDisplay', {
    extend: 'Ext.form.field.Display',
    xtype: 'interval-flags-displayfield',
    name: 'intervalFlags',
    fieldLabel: Uni.I18n.translate('intervalFlags.label', 'UNI', 'Interval flags'),
    emptyText: '',

    requires: [
        'Ext.button.Button'
    ],

    deferredRenderer: function (value, field, tooltip) {
        new Ext.button.Button({
            renderTo: field.getEl().down('.x-form-display-field'),
            tooltip: tooltip,
            iconCls: 'icon-info-small',
            cls: 'uni-btn-transparent',
            style: {
                display: 'inline-block',
                "text-decoration": 'none !important'
            }
        });

        field.updateLayout();
    },

    renderer: function (value, field) {
        var result,
            tooltip = '';
        if (!Ext.isArray(value) || !value.length) {
            return this.emptyText;
        }

        result = value.length;
        Ext.Array.each(value, function (value, index) {
            index++;
            tooltip += Uni.I18n.translate('intervalFlags.Flag', 'UNI', 'Flag') + ' ' + index + ': ' + value + '<br>';
        });

        Ext.defer(this.deferredRenderer, 1, this, [result, field, tooltip]);
        return '<span style="display: inline-block; width: 20px; float: left;">' + result + '</span>';
    }
});