Ext.define('Skyline.form.Labelable', {
    override: 'Ext.form.Labelable',
    labelPad: 15,
    msgTarget: 'under',
    blankText: 'This is a required field',

    /**
     * Required property, set when a field is required.
     */
    required: false,

    /**
     * Changes the default value ':'.
     */
    labelSeparator: '',

    /**
     * Changes the default value 'left'.
     */
    labelAlign: 'right',

    /**
     * @inheritDoc Ext.form.Labelable#getLabelCls
     *
     * Adds an extra required class when needed
     *
     * @returns {string}
     */
    getLabelCls: function () {
        var labelCls = this.labelCls;

        if (this.required) {
            labelCls += ' ' + 'uni-form-item-label-required';
        }

        return labelCls;
    }
});

