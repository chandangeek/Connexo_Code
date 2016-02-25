Ext.define('Imt.usagepointmanagement.view.forms.fields.LimiterCheckbox', {
    extend: 'Ext.form.field.Checkbox',
    alias: 'widget.limitercheckbox',
    name: 'limiter',
    fieldLabel: Uni.I18n.translate('general.label.limiter', 'IMT', 'Limiter'),
    listeners: {
        change: {
            fn: function (field, newValue) {
                if (field.rendered) {
                    Ext.suspendLayouts();
                    field.nextSibling('loadlimitertypefield').setVisible(newValue);
                    field.nextSibling('loadlimitfield').setVisible(newValue);
                    Ext.resumeLayouts(true);
                }
            }
        }
    }
});