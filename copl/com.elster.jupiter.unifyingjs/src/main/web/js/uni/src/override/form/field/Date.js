Ext.define('Uni.override.form.field.Date', {
    override: 'Ext.form.field.Date',

    format: 'd/m/Y',
    editable: false,

    initComponent: function () {
        this.callParent(arguments);
    },

    getValue: function () {
        return this.value;
    }
});