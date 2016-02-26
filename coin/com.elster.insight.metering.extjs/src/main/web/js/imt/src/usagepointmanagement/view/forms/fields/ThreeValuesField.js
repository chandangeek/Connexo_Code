Ext.define('Imt.usagepointmanagement.view.forms.fields.ThreeValuesField', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.techinfo-threevaluesfield',
    displayField: 'displayValue',
    valueField: 'value',
    queryMode: 'local',
    forceSelection: true,
    value: 'UNKNOWN',
    store: [
        ['YES', Uni.I18n.translate('general.yes', 'IMT', 'Yes')],
        ['NO', Uni.I18n.translate('general.no', 'IMT', 'No')],
        ['UNKNOWN', Uni.I18n.translate('general.unknown', 'IMT', 'Unknown')]
    ],

    setValue: function (value) {
        var me = this,
            valueToSet;

        switch (value) {
            case true:
                valueToSet = 'YES';
                break;
            case false:
                valueToSet = 'NO';
                break;
            case null:
                valueToSet = 'UNKNOWN';
                break;
            default:
                valueToSet = value;
        }

        me.callParent([valueToSet]);
    },

    getValue: function () {
        var me = this,
            value = me.callParent(arguments),
            returnedValue;

        switch (value) {
            case 'YES':
                returnedValue = true;
                break;
            case 'NO':
                returnedValue = false;
                break;
            default:
                returnedValue = null;
        }

        return returnedValue;
    }
});