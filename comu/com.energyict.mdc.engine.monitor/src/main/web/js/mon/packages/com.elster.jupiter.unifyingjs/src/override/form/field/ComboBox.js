/**
 * @class Uni.override.ApplicationOverride
 */
Ext.define('Uni.override.form.field.ComboBox', {
    override: 'Ext.form.field.ComboBox',

    anyMatch: true,

    listeners: {
        // force re-validate on combo change
        change: function (combo) {
            combo.validate();
        }
    },

    initComponent: function () {
        this.callParent(arguments);
    }
});