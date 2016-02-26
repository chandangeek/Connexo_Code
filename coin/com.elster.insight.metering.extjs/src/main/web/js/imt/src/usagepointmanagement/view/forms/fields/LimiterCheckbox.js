Ext.define('Imt.usagepointmanagement.view.forms.fields.LimiterCheckbox', {
    extend: 'Ext.form.field.Checkbox',
    alias: 'widget.techinfo-limitercheckbox',
    name: 'limiter',
    fieldLabel: Uni.I18n.translate('general.label.limiter', 'IMT', 'Limiter'),
    listeners: {
        change: {
            fn: function (field, newValue) {
                if (field.rendered) {
                    Ext.suspendLayouts();
                    field.nextSibling('techinfo-loadlimitertypefield').setVisible(newValue);
                    field.nextSibling('techinfo-loadlimitfield').setVisible(newValue);
                    Ext.resumeLayouts(true);
                }
            }
        }
    }
});