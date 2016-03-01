Ext.define('Imt.usagepointmanagement.view.forms.fields.LimiterCheckbox', {
    extend: 'Ext.form.field.Checkbox',
    alias: 'widget.techinfo-limitercheckbox',
    name: 'limiter',
    fieldLabel: Uni.I18n.translate('general.label.limiter', 'IMT', 'Limiter'),
    listeners: {
        beforerender: {
            fn: function (field) {
                Ext.suspendLayouts();
                field.nextSibling('loadlimitertypefield').setVisible(field.value);
                field.nextSibling('loadlimitfield').setVisible(field.value);
                Ext.resumeLayouts(true);
            }
        },
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