Ext.define('Isu.util.CreatingControl', {
    createControl: function (obj) {
        var control = false;

        switch (obj.control.xtype) {
            case 'textfield':
                control = this.createTextField(obj);
                break;
            case 'numberfield':
                control = this.createNumberField(obj);
                break;
            case 'combobox':
                control = this.createCombobox(obj);
                break;
        }

        return control;
    },

    createTextField: function (obj) {
        var textField = {
            xtype: 'textfield',
            name: obj.key,
            fieldLabel: obj.label,
            allowBlank: !obj.constraint.required,
            labelSeparator: (obj.constraint.required ? ' *' : ''),
            formBind: false
        };

        obj.constraint.max && (textField.maxLength = obj.constraint.max);
        obj.constraint.min && (textField.minLength = obj.constraint.min);
        obj.constraint.regexp && (textField.regex = new RegExp(obj.constraint.regexp, 'g'));
        obj.defaultValue && (textField.value = obj.defaultValue);
        obj.help && (textField.afterSubTpl = obj.help);

        return textField;
    },

    createNumberField: function (obj) {
        var numberField = {
            xtype: 'numberfield',
            name: obj.key,
            fieldLabel: obj.label,
            allowBlank: !obj.constraint.required,
            labelSeparator: (obj.constraint.required ? ' *' : ''),
            formBind: false
        };

        obj.constraint.max && (numberField.maxValue = obj.constraint.max);
        obj.constraint.min && (numberField.minValue = obj.constraint.min);
        obj.defaultValue && (numberField.value = obj.defaultValue);

        return numberField;
    },

    createCombobox: function (obj) {
        var comboboxStore = Ext.create('Ext.data.Store', {
                fields: [{name: 'title', type: 'auto'}],
                data: obj.values
            }),
            combobox = {
                xtype: 'combobox',
                name: obj.key,
                fieldLabel: obj.label,
                allowBlank: !obj.constraint.required,
                labelSeparator: (obj.constraint.required ? ' *' : ''),
                maxLength: (obj.constraint.max ? ' *' : ''),
                store: comboboxStore,
                queryMode: 'local',
                displayField: 'title',
                valueField: 'id',
                editable: false,
                formBind: false
            };

        obj.defaultValue && (combobox.value = obj.defaultValue.id);

        return combobox;
    }
});