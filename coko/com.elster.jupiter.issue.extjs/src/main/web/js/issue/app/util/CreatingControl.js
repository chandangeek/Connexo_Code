Ext.define('Isu.util.CreatingControl', {
    requires: [
        'Isu.view.workspace.issues.component.UserCombo'
    ],
    createControl: function (obj) {
        var control = false;

        switch (obj.control.xtype.toLowerCase()) {
            case 'textfield':
                control = this.createTextField(obj);
                break;
            case 'numberfield':
                control = this.createNumberField(obj);
                break;
            case 'combobox':
                control = this.createCombobox(obj);
                break;
            case 'textarea':
                control = this.createTextArea(obj);
                break;
            case 'emaillist':
                control = this.createEmailList(obj);
                break;
            case 'usercombobox':
                control = this.createUserCombobox(obj);
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
            required: obj.constraint.required,
            formBind: false
        };

        obj.constraint.max && (textField.maxLength = obj.constraint.max);
        obj.constraint.min && (textField.minLength = obj.constraint.min);
        obj.constraint.regexp && (textField.regex = new RegExp(obj.constraint.regexp));
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
            required: obj.constraint.required,
            formBind: false
        };

        obj.constraint.max && (numberField.maxValue = obj.constraint.max);
        obj.constraint.min && (numberField.minValue = obj.constraint.min);
        obj.defaultValue && (numberField.value = obj.defaultValue);

        return numberField;
    },

    createCombobox: function (obj) {
        var comboboxStore = Ext.create('Ext.data.Store', {
                fields: [{name: 'id', type: 'string'},{name: 'title', type: 'auto'}],
                data: obj.values
            }),
            combobox = {
                xtype: 'combobox',
                name: obj.key,
                fieldLabel: obj.label,
                allowBlank: !obj.constraint.required,
                required: obj.constraint.required,
                store: comboboxStore,
                queryMode: 'local',
                displayField: 'title',
                valueField: 'id',
                editable: false,
                formBind: false
            };

        obj.defaultValue && (combobox.value = obj.defaultValue.id);

        return combobox;
    },

    createTextArea: function (obj) {
        var textareafield = {
            xtype: 'textareafield',
            itemId: 'emailBody',
            name: obj.key,
            fieldLabel: obj.label,
            allowBlank: !obj.constraint.required,
            required: obj.constraint.required,
            width: 500,
            height: 150,
            formBind: false
        };

        obj.constraint.max && (textareafield.maxLength = obj.constraint.max);
        obj.constraint.min && (textareafield.minLength = obj.constraint.min);
        obj.constraint.regexp && (textareafield.regex = new RegExp(obj.constraint.regexp));
        obj.defaultValue && (textareafield.value = obj.defaultValue);
        obj.help && (textareafield.afterSubTpl = obj.help);

        return textareafield;
    },

    createEmailList: function (obj) {
        var emailList = {
            xtype: 'textarea',
            itemId: 'emailList',
            name: obj.key,
            width: 500,
            height: 150,
            allowBlank: !obj.constraint.required,
            required: obj.constraint.required,
            fieldLabel: obj.label,
            emptyText: 'user@example.com',
            regex: /^((([a-zA-Z0-9_\-\.]+)@([a-zA-Z0-9_\-\.]+)\.([a-zA-Z?]{2,5}){1,25})*(\n?)*)*$/,
            regexText: 'This field should contains one e-mail address per line',
            formBind: false
        };

        obj.constraint.max && (emailList.maxLength = obj.constraint.max);
        obj.constraint.min && (emailList.minLength = obj.constraint.min);

        return emailList;
    },

    createUserCombobox: function (obj) {
        var userCombobox = {
            xtype: 'issues-user-combo',
            itemId: 'userCombo',
            name: obj.key,
            fieldLabel: obj.label,
            allowBlank: !obj.constraint.required,
            required: obj.constraint.required
        };
        return userCombobox;
    }
});