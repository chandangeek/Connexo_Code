Ext.define('Imt.usagepointmanagement.view.forms.fields.BypassField', {
    extend: 'Ext.form.field.Checkbox',
    alias: 'widget.techinfo-bypassfield',
    name: 'bypass',
    fieldLabel: Uni.I18n.translate('general.label.bypass', 'IMT', 'Bypass'),
    listeners: {
        change: {
            fn: function (field, newValue) {
                if (field.rendered) {
                    field.nextSibling('techinfo-bypassstatuscombobox').setVisible(newValue);
                }
            }
        }
    }
});