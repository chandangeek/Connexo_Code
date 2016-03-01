Ext.define('Imt.usagepointmanagement.view.forms.fields.BypassField', {
    extend: 'Imt.usagepointmanagement.view.forms.fields.ThreeValuesField',
    alias: 'widget.techinfo-bypassfield',
    name: 'bypass',
    fieldLabel: Uni.I18n.translate('general.label.bypass', 'IMT', 'Bypass'),
    listeners: {
        change: {
            fn: function (field, newValue) {
                if (field.rendered) {
                    field.nextSibling('techinfo-bypassstatuscombobox').setVisible(newValue === 'YES');
                }
            }
        }
    }
});