Ext.define('Mdc.view.setup.comtasks.parameters.TimeCombo', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.communication-tasks-parameters-timecombo',
    store: 'Mdc.store.TimeUnits',
    queryMode: 'local',
    width: 143,
    displayField: 'localizedValue',
    valueField: 'timeUnit',
    editable: false,
    validateOnBlur: false,
    validateOnChange: false
});