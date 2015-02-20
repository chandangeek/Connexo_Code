Ext.define('Isu.util.CreatingControl', {
    requires: [
        'Isu.view.component.UserCombo',
        'Isu.view.component.AssigneeControl'
    ],
    createControl: function (obj) {
        var control = false;

        switch (obj.control.xtype.toLowerCase()) {
            case 'textfield':
                control = Ext.isEmpty(obj.suffix) ? this.createTextField(obj) : this.suffixAppender(this.createTextField, obj.suffix);
                break;
            case 'numberfield':
                control = Ext.isEmpty(obj.suffix) ? this.createNumberField(obj) : this.suffixAppender(this.createNumberField(obj), obj.suffix);
                break;
            case 'combobox':
                control = Ext.isEmpty(obj.suffix) ? this.createCombobox(obj) : this.suffixAppender(this.createCombobox(obj), obj.suffix);
                break;
            case 'textarea':
                control = Ext.isEmpty(obj.suffix) ? this.createTextArea(obj) : this.suffixAppender(this.createTextArea(obj), obj.suffix);
                break;
            case 'emaillist':
                control = Ext.isEmpty(obj.suffix) ? this.createEmailList(obj) : this.suffixAppender(this.createEmailList(obj), obj.suffix);
                break;
            case 'usercombobox':
                control = Ext.isEmpty(obj.suffix) ? this.createUserCombobox(obj) : this.suffixAppender(this.createUserCombobox(obj), obj.suffix);
                break;
            case 'trendperiodcontrol':
                control = this.createTrendPeriodControl(obj);
                break;
            case 'checkbox':
                control = Ext.isEmpty(obj.suffix) ? this.createCheckBox(obj) : this.suffixAppender(this.createCheckBox(obj), obj.suffix);
                break;
            case 'issueassignee':
                control = this.createIssueAssignee(obj);
                break;
        }

        return control;
    },

    createTextField: function (obj) {
        var textField = {
            xtype: 'textfield',
            itemId: 'issues-creation-rules-template-textfield',
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
        obj.help && (textField.afterSubTpl = '<span style="color: #686868; font-style: italic">' + obj.help + '</span>');
        obj.dependOn && (textField.dependOn = obj.dependOn);

        return textField;
    },

    createNumberField: function (obj) {
        var numberField = {
            xtype: 'numberfield',
            itemId: 'issues-creation-rules-template-numberfield',
            name: obj.key,
            fieldLabel: obj.label,
            allowBlank: !obj.constraint.required,
            required: obj.constraint.required,
            formBind: false
        };

        obj.constraint.max && (numberField.maxValue = obj.constraint.max);
        (obj.constraint.min !== undefined) && (numberField.minValue = obj.constraint.min);
        obj.defaultValue && (numberField.value = obj.defaultValue);
        obj.help && (numberField.afterSubTpl = '<span style="color: #686868; font-style: italic">' + obj.help + '</span>');
        obj.dependOn && (numberField.dependOn = obj.dependOn);

        return numberField;
    },

    createCombobox: function (obj) {
        var comboboxStore = Ext.create('Ext.data.Store', {
                fields: ['id', 'title'],
                data: obj.defaultValues
            }),
            combobox = {
                xtype: 'combobox',
                itemId: 'issues-creation-rules-template-combobox',
                name: obj.key,
                fieldLabel: obj.label,
                allowBlank: !obj.constraint.required,
                required: obj.constraint.required,
                store: comboboxStore,
                queryMode: 'local',
                displayField: 'title',
                valueField: 'id',
                editable: true,
                forceSelection: true,
                formBind: false
            };

        obj.defaultValue && (combobox.value = obj.defaultValue.id);
        obj.help && (combobox.afterSubTpl = '<span style="color: #686868; font-style: italic">' + obj.help + '</span>');
        obj.dependOn && (combobox.dependOn = obj.dependOn);

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
            height: 150,
            formBind: false
        };

        obj.constraint.max && (textareafield.maxLength = obj.constraint.max);
        obj.constraint.min && (textareafield.minLength = obj.constraint.min);
        obj.constraint.regexp && (textareafield.regex = new RegExp(obj.constraint.regexp));
        obj.defaultValue && (textareafield.value = obj.defaultValue);
        obj.help && (textareafield.afterSubTpl = '<span style="color: #686868; font-style: italic">' + obj.help + '</span>');
        obj.dependOn && (textareafield.dependOn = obj.dependOn);

        return textareafield;
    },

    createEmailList: function (obj) {
        var emailList = {
            xtype: 'textarea',
            itemId: 'emailList',
            name: obj.key,
            height: 150,
            allowBlank: !obj.constraint.required,
            required: obj.constraint.required,
            fieldLabel: obj.label,
            emptyText: 'user@example.com',
            formBind: false
        };

        obj.constraint.max && (emailList.maxLength = obj.constraint.max);
        obj.constraint.min && (emailList.minLength = obj.constraint.min);
        obj.help && (emailList.afterSubTpl = '<span style="color: #686868; font-style: italic">' + obj.help + '</span>');
        obj.dependOn && (emailList.dependOn = obj.dependOn);

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

        obj.help && (userCombobox.afterSubTpl = '<span style="color: #686868; font-style: italic">' + obj.help + '</span>');
        obj.dependOn && (userCombobox.dependOn = obj.dependOn);

        return userCombobox;
    },

    createTrendPeriodControl: function (obj) {
        var trendPeriodControl = {
                xtype: 'fieldcontainer',
                itemId: 'issues-creation-rules-template-trendPeriod',
                layout: 'hbox',
                fieldLabel: obj.label,
                name: obj.key,
                required: obj.constraint.required,
                items: []
            },
            trendPeriod = this.createNumberField(obj),
            trendPeriodUnit = this.createCombobox(obj.control.unitParameter);

        delete trendPeriod.fieldLabel;
        delete trendPeriod.required;
        trendPeriod.width = 150;
        trendPeriod.margin = '0 10 0 0';
        trendPeriodUnit.flex = 1;
        trendPeriodControl.items.push(trendPeriod, trendPeriodUnit);

        return trendPeriodControl;
    },

    createIssueAssignee: function (obj) {
        var me = this,
            issueAssigneeControl = {
                xtype: 'issues-assignee-control',
                fieldLabel: obj.label,
                name: obj.key,
                required: obj.constraint.required,
                items: []
            },
           //this was done in scope of JP-8241. We will have only users assignee for now
            controls = [
                obj.control.userControl
            ];

        Ext.Array.each(controls, function (item, index) {
            var control = me.createControl(item);

            control.fieldLabel = '';
            control.allowBlank = false;
            control.flex = 1;
            issueAssigneeControl.items.push({
                items: [
                    control
                ]
            });
        });

        return issueAssigneeControl;
    },

    createCheckBox: function (obj) {
        var checkBox = {
            xtype: 'checkboxfield',
            itemId: 'issues-creation-rules-template-checkboxfield',
            name: obj.key,
            fieldLabel: obj.label,
            formBind: false
        };

        obj.defaultValue && (checkBox.value = obj.defaultValue);
        obj.help && (checkBox.afterSubTpl = '<span style="color: #686868; font-style: italic">' + obj.help + '</span>');
        obj.dependOn && (checkBox.dependOn = obj.dependOn);

        return checkBox;
    },

    suffixAppender: function (field, suffix) {
        field.columnWidth = 1;
        return {
            xtype: 'fieldcontainer',
            layout: 'column',
            name: field.name,
            defaults: {
                labelWidth: 150,
                anchor: '100%',
                validateOnChange: false,
                validateOnBlur: false
            },
            items: [ field, { xtype: 'displayfield', margin: '0 0 0 5', submitValue: false, value: suffix } ]
        };
    }
});