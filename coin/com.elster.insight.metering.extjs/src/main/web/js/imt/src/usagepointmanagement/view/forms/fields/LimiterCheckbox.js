Ext.define('Imt.usagepointmanagement.view.forms.fields.LimiterCheckbox', {
    extend: 'Ext.form.field.Checkbox',
    alias: 'widget.techinfo-limitercheckbox',
    name: 'limiter',
    fieldLabel: Uni.I18n.translate('general.label.limiter', 'IMT', 'Limiter'),
    listeners: {
        beforerender: {
            fn: function (field) {
                field.showChildField(field.value);
            }
        },
        change: {
            fn: function (field, newValue) {
                if (field.rendered) {
                    field.showChildField(newValue);
                }
            }
        }
    },
    showChildField: function (value) {
        Ext.suspendLayouts();
        this.nextSibling('techinfo-loadlimitertypefield').setVisible(value);
        this.nextSibling('techinfo-loadlimitfield').setVisible(value);
        Ext.resumeLayouts(true);
    }
});