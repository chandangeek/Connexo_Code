Ext.define('Skyline.form.field.Text', {
    override: 'Ext.form.field.Text',
    labelAlign: 'right',
    labelPad: 15,
    blankText: 'This is a required field',
    initComponent: function () {
        this.msgTarget = 'under';
        this.callParent(arguments)
    }
});