Ext.define('Uni.override.form.field.Date', {
    override: 'Ext.form.field.Date',

    format: 'd/m/Y',
    editable: false,

    getValue: function () {
        return this.value;
    }
});