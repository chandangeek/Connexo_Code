Ext.define('Uni.override.form.field.Text', {
    override: "Ext.form.field.Text",
    blankText: Uni.I18n.translate('fieldValidation.blankText', 'UNI', 'This field is required')
});