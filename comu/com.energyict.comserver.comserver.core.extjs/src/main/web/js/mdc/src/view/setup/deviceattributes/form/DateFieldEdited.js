Ext.define('Mdc.view.setup.deviceattributes.form.DateFieldEdited', {
    extend: 'Ext.form.field.Date',
    xtype: 'deviceDateFieldEdited',
    width: 400,
    editable: false,
    required: true,

    getTimeStampValue: function() {
        if (this.getValue()) {
            return this.getValue().getTime();
        } else {
            return null;
        }
    }
});



