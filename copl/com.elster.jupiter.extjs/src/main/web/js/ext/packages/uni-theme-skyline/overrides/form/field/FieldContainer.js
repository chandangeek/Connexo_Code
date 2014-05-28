Ext.define('Skyline.form.field.FieldContainer', {
    override: "Ext.form.FieldContainer",
    getLabelCls: function () {
        var labelCls = this.labelCls;
        if (this.required) {
            labelCls += ' ' + 'uni-form-item-label-required';
        }
        return labelCls;
    },
    initComponent: function() {
        this.callParent(arguments);
    }
});
