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
        Ext.applyIf(me.listConfig, {
            getInnerTpl: function (displayField) {
                return '{' + displayField  + ':htmlEncode}';
            }
        });
        this.callParent(arguments);
    }
});