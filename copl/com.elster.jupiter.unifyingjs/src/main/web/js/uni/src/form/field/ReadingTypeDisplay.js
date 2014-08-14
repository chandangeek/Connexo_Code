/**
 * @class Uni.form.field.ObisDisplay
 */
Ext.define('Uni.form.field.ReadingTypeDisplay', {
    extend: 'Ext.form.field.Display',
    xtype: 'reading-type-displayfield',
    name: 'readingType',
    fieldLabel: Uni.I18n.translate('readingType.label', 'UNI', 'Reading type'),
    emptyText: '-',

    requires: [
        'Ext.button.Button'
    ],

    deferredRenderer: function (value, field) {
        var me = this;

        new Ext.button.Button({
            renderTo: field.getEl().down('.x-form-display-field'),
            tooltip: Uni.I18n.translate('readingType.tooltip', 'UNI', 'Reading type info'),
            iconCls: 'icon-info-small',
            cls: 'uni-btn-transparent',
            style: {
                display: 'inline-block',
                "text-decoration": 'none !important'
            },
            handler: function() {
                me.handler(value);
            }
        });

        field.updateLayout();
    },

    handler: function (value) {
        var widget = Ext.widget('readingTypeDetails');

        widget.down('form').getForm().setValues(value);
        widget.show();
    },

    renderer: function (value, field) {
        if (!value) {
            return this.emptyText;
        }

        Ext.defer(this.deferredRenderer, 1, this, [value, field]);
        return '<span style="display: inline-block; width: 230px; float: left;">' + (value.mrid || value) + '</span>';
    }
});