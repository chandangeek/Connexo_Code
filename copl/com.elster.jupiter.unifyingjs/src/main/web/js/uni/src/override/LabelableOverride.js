/**
 * @class Uni.override.LabelableOverride
 */
Ext.define('Uni.override.LabelableOverride', {
    override: 'Ext.form.Labelable',

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
     * Adds extra styling to emphasize the label.
     */
    labelClsExtra: Uni.About.baseCssPrefix + 'form-item-bold',

    /**
     * @inheritDoc Ext.form.Labelable#getLabelCls
     *
     * Adds an extra required class when needed
     *
     * @returns {string}
     */
    getLabelCls: function() {
        var labelCls = this.labelCls + ' ' + Ext.dom.Element.unselectableCls,
            labelClsExtra = this.labelClsExtra;

        if(this.required){
            labelCls += ' ' + Uni.About.baseCssPrefix + 'form-item-label-required';
        }

        return labelClsExtra ? labelCls + ' ' + labelClsExtra : labelCls;
    }
});