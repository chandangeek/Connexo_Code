/**
 * @class Uni.override.ApplicationOverride
 */
Ext.define('Uni.override.form.field.ComboBoxOverride', {
    override: 'Ext.form.field.ComboBox',

    anyMatch: true,

    listeners: {
        // force re-validate on combo change
        change: function (combo) {
            combo.validate();
        }
    },

    initComponent: function () {
        var me=this;
        me.listConfig = me.listConfig || {};
        Ext.apply(me.listConfig, {
            getInnerTpl: function (displayField) {
                return '{' + displayField  + ':htmlEncode}';
            }
        });
        me.getPicker() && me.getPicker().setOverflowXY('hidden', 'auto');
        this.callParent(arguments);
    }
});