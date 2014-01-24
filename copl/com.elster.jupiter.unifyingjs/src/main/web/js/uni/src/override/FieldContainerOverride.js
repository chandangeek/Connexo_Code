/**
 * @class Uni.override.FieldContainerOverride
 */
Ext.define('Uni.override.FieldContainerOverride', {
    override: 'Ext.form.FieldContainer',

    /**
     * Changes the default value ':'.
     */
    labelSeparator: '',

    /**
     * Changes the default value 'qtip'.
     */
    msgTarget: 'side'

});