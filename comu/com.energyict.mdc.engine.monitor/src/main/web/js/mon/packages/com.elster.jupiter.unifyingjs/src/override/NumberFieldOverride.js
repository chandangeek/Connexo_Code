/**
 * @class Uni.override.NumberFieldOverride
 */
Ext.define('Uni.override.NumberFieldOverride', {
    override: 'Ext.form.field.Number',

    /**
     * Changes the default alignment of numberfields.
     */
    fieldStyle: 'text-align:right;',

    minText : "The minimum value is {0}",

    maxText : "The maximum value is {0}"



});