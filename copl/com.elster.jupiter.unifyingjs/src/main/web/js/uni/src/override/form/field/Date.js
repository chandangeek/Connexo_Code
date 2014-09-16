Ext.define('Uni.override.form.field.Date', {
    override: 'Ext.form.field.Date',

    format: 'd/m/Y',

    initComponent: function () {
        this.callParent(arguments);
    }
});


