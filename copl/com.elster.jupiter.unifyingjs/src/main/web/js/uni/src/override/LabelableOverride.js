/**
 * @class Uni.override.LabelableOverride
 */
Ext.define('Uni.override.LabelableOverride', {
    override: 'Ext.form.Labelable',

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
    labelClsExtra: Uni.About.baseCssPrefix + 'form-item-bold'

});