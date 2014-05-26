Ext.define('Isu.view.administration.communicationtasks.parameters.TimeCombo', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.communication-tasks-parameters-timecombo',
    store: 'Isu.store.TimeTypes',
    queryMode: 'local',
    displayField: 'displayValue',
    valueField: 'name',
    editable: false,
    validateOnBlur: false,
    validateOnChange: false
});