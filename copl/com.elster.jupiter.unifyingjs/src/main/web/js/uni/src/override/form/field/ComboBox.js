/**
 * @class Uni.override.ApplicationOverride
 */
Ext.define('Uni.override.form.field.ComboBox', {
    override: 'Ext.form.field.ComboBox',
    listeners: {
        // force re-validate on combo change
        change: function(combo) {
            combo.validate();
        }
    }
});