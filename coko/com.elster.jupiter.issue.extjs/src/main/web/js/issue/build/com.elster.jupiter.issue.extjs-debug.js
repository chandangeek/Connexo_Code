Ext.define('Isu.view.component.UserCombo', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.issues-user-combo',
    store: 'Isu.store.Users',
    displayField: 'name',
    valueField: 'id',
    triggerAction: 'query',
    queryMode: 'remote',
    queryParam: 'like',
    allQuery: '',
    lastQuery: '',
    queryDelay: 100,
    minChars: 0,
    disableKeyFilter: true,
    queryCaching: false,
    typeAhead: true,
    forceSelection: true,
    formBind: false,
    emptyText: 'select user',
    listConfig: {
        getInnerTpl: function(displayField) {
            return '<img src="../../apps/isu/resources/images/icons/USER.png"/> {' + displayField + '}';
        }
    }
});

Ext.define('Isu.view.component.AssigneeControl', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.issues-assignee-control',
    mixins: {
        field: 'Ext.form.field.Field'
    },
    defaults: {
        xtype: 'container',
        layout: {
            type: 'hbox'
        },
        margin: '0 0 8 0'
    },

    getValue: function () {
        return this.down('radiofield[checked=true]').inputValue;
    },

    markInvalid: function (errors) {
        this.down('radiofield[checked=true]').nextSibling().markInvalid(errors);
    }
});

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
            name: obj.key,
            fieldLabel: obj.label,
            allowBlank: !obj.constraint.required,
            required: obj.constraint.required,
            formBind: false
        };

        obj.constraint.max && (numberField.maxValue = obj.constraint.max);
        obj.constraint.min && (numberField.minValue = obj.constraint.min);
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
            regex: /^((([a-zA-Z0-9_\-\.]+)@([a-zA-Z0-9_\-\.]+)\.([a-zA-Z?]{2,5}){1,25})*(\n?)*)*$/,
            regexText: 'This field should contains one e-mail address per line',
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
            controls = [
                obj.control.userControl,
                obj.control.groupControl,
                obj.control.roleControl
            ];

        Ext.Array.each(controls, function (item, index) {
            var control = me.createControl(item),
                radio = {
                    xtype: 'radiofield',
                    name: obj.key,
                    boxLabel: item.label,
                    inputValue: item.key,
                    checked: index === 0 ? true : false,
                    width: 100,
                    listeners: {
                        focus: {
                            fn: function () {
                                var combo = this.nextSibling();
                                Ext.Array.each(this.up('issues-assignee-control').query('radiofield'), function (radiofield) {
                                    radiofield.nextSibling().allowBlank = true;
                                });
                                combo.allowBlank = false;
                                combo.focus();
                            }
                        }
                    }
                };
            control.fieldLabel = '';
            control.allowBlank = index !== 0 ? true : false;
            control.listeners = {
                focus: {
                    fn: function () {
                        this.previousSibling().setValue(true);
                    }
                }
            };
            control.flex = 1;
            issueAssigneeControl.items.push({
                items: [
                    radio,
                    control
                ]
            });
        });

        return issueAssigneeControl;
    },

    createCheckBox: function (obj) {
        var checkBox = {
            xtype: 'checkboxfield',
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

Ext.define('Isu.view.issues.ActionView', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.util.FormErrorMessage'
    ],
    alias: 'widget.issue-action-view',
    router: null,
    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'form',
                itemId: 'issue-action-view-form',
                title: '&nbsp;',
                ui: 'large',
                defaults: {
                    labelWidth: 150,
                    width: 700
                },
                items: [
                    {
                        itemId: 'issue-action-view-form-errors',
                        xtype: 'uni-form-error-message',
                        hidden: true
                    }
                ],
                buttons: [
                    {
                        itemId: 'issue-action-apply',
                        ui: 'action',
                        text: Uni.I18n.translate('general.apply', 'ISU', 'Apply'),
                        action: 'applyAction'
                    },
                    {
                        itemId: 'issue-action-cancel',
                        text: Uni.I18n.translate('general.cancel', 'ISU', 'Cancel'),
                        ui: 'link',
                        action: 'cancelAction',
                        href: me.router.getRoute(me.router.currentRoute.replace('/action', '')).buildUrl()
                    }
                ],
                updateRecord: function (record) {
                    var basic = this.getForm();

                    record = record || basic._record;
                    if (!record) {
                        Ext.Error.raise("A record is required.");
                        return basic;
                    }

                    record.set('parameters', this.getValues());

                    return basic;
                }
            }
        ];

        me.callParent(arguments);
    }
});

Ext.define('Isu.controller.ApplyIssueAction', {
    extend: 'Ext.app.Controller',

    views: [
        'Isu.view.issues.ActionView'
    ],

    mixins: [
        'Isu.util.CreatingControl'
    ],

    showOverview: function (issueModelClass, issueId, actionId, widgetItemId) {
        var me = this,
            widget = Ext.widget('issue-action-view', {
                router: me.getController('Uni.controller.history.Router'),
                itemId: widgetItemId
            }),
            form = widget.down('#issue-action-view-form'),
            issueModel = me.getModel(issueModelClass),
            actionModel = Ext.create(issueModelClass).actions().model;

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);

        issueModel.load(issueId, {
            success: function (record) {
                me.getApplication().fireEvent('issueLoad', record);
            }
        });

        actionModel.getProxy().url = issueModel.getProxy().url + '/' + issueId + '/actions';
        actionModel.load(actionId, {
            success: function (record) {
                me.getApplication().fireEvent('issueActionLoad', record);
                form.setTitle(record.get('name'));
                Ext.Object.each(record.get('parameters'), function(key, value) {
                    var formItem = me.createControl(value);
                    formItem && form.add(formItem);
                });
                form.loadRecord(record);
                widget.setLoading(false);
            }
        });
    },

    applyAction: function () {
        var me = this,
            page = me.getPage(),
            form = me.getForm(),
            basicForm = form.getForm(),
            errorPanel = form.down('#issue-action-view-form-errors'),
            router = me.getController('Uni.controller.history.Router');

        errorPanel.hide();
        basicForm.clearInvalid();
        form.updateRecord();
        page.setLoading(true);
        form.getRecord().save({
            callback: function (model, operation, success) {
                var responseText = Ext.decode(operation.response.responseText, true);

                page.setLoading(false);
                if (responseText) {
                    if (success) {
                        if (responseText.data.actions[0].success) {
                            me.getApplication().fireEvent('acknowledge', responseText.data.actions[0].message);
                            router.getRoute(router.currentRoute.replace('/action', '')).forward();
                        } else {
                            me.getApplication().getController('Uni.controller.Error').showError(form.getRecord().get('name'), responseText.data.actions[0].message);
                        }
                    } else if (operation.response.status === 400) {
                        if (responseText.errors) {
                            errorPanel.show();
                            basicForm.markInvalid(responseText.errors);
                        }
                    }
                }
            }
        });
    }
});

Ext.define('Isu.view.component.AssigneeColumn', {
    extend: 'Ext.grid.column.Column',
    xtype: 'isu-assignee-column',
    header: '',
    emptyText: '',

    renderer: function (value, metaData, record, rowIndex, colIndex) {
        var result;

        if (!Ext.isEmpty(value)) {
            result = '';
            if (value.type) {
                result += '<span class="isu-icon-' + value.type + ' isu-assignee-type-icon" data-qtip="' + Uni.I18n.translate('assignee.tooltip.' + value.type, 'ISU', value.type) + '"></span> ';
            }
            if (value.name) {
                result += value.name;
            }
        }

        return result || this.columns[colIndex].emptyText;
    }
});

Ext.define('Isu.view.assignmentrules.List', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.layout.container.Column',
        'Ext.grid.column.Template',
        'Isu.view.component.AssigneeColumn'
    ],
    alias: 'widget.issues-assignment-rules-list',
    store: 'Isu.store.AssignmentRules',
    height: 285,

    columns: [
        {
            header: 'Description',
            dataIndex: 'description',
            flex: 1
        },
        {
            header: 'Assign to',
            xtype: 'isu-assignee-column',
            dataIndex: 'assignee',
            flex: 1
        }
    ],

    initComponent: function () {
        var me = this;

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                isFullTotalCount: true,
                displayMsg: Uni.I18n.translate('assignmentrules.list.pagingtoolbartop.displayMsg', 'ISU', '{2} rules'),
                emptyMsg: Uni.I18n.translate('assignmentrules.list.pagingtoolbartop.emptyMsg', 'ISU', 'There are no rules to display')
            }
        ];

        me.callParent(arguments);
    }
});


Ext.define('Isu.view.assignmentrules.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Isu.view.assignmentrules.List',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
    alias: 'widget.issue-assignment-rules-overview',

    content: {
        itemId: 'title',
        xtype: 'panel',
        ui: 'large',
        title: Uni.I18n.translate('issue.administration.assignment', 'ISU', 'Issue assignment rules'),
        items: {
            itemId: 'issues-rules-list',
            xtype: 'preview-container',
            grid: {
                xtype: 'issues-assignment-rules-list'
            },
            emptyComponent: {
                xtype: 'no-items-found-panel',
                title: Uni.I18n.translate('issueAssignment.empty.title', 'ISU', 'No issue assignment rules found'),
                reasons: [
                    Uni.I18n.translate('issueAssignment.empty.list.item', 'ISU', 'No issue assignment rules have been defined yet.')
                ]
            },
            previewComponent: null
        }
    }
});

Ext.define('Isu.model.AssignmentRule', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'description',
            type: 'string'
        },
        {
            name: 'assignee',
            type: 'auto'
        },
        {
            name: 'version',
            type: 'int'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/rules/assign',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

Ext.define('Isu.store.AssignmentRules', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    model: 'Isu.model.AssignmentRule',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/isu/rules/assign',
        reader: {
            type: 'json',
            root: 'data'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});

Ext.define('Isu.controller.AssignmentRules', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    stores: [
        'Isu.store.AssignmentRules'
    ],
    views: [
        'Isu.view.assignmentrules.Overview'
    ],

    showOverview: function () {
        var widget = Ext.widget('issue-assignment-rules-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
        this.getStore('Isu.store.AssignmentRules').load();
    }
});

Ext.define('Isu.view.creationrules.EditAction', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.issues-creation-rules-edit-action',
    content: [
        {
            name: 'pageTitle',
            ui: 'large',
            items: [
                {
                    xtype: 'form',
                    width: '75%',
                    defaults: {
                        labelWidth: 150,
                        validateOnChange: false,
                        validateOnBlur: false,
                        width: 700
                    },
                    items: [
                        {
                            itemId: 'form-errors',
                            xtype: 'uni-form-error-message',
                            name: 'form-errors',
                            hidden: true
                        },
                        {
                            xtype: 'radiogroup',
                            itemId: 'phasesRadioGroup',
                            name: 'phasesRadioGroup',
                            fieldLabel: 'When to perform',
                            required: true,
                            columns: 1,
                            vertical: true
                        },
                        {
                            itemId: 'actionType',
                            xtype: 'combobox',
                            name: 'actionType',
                            fieldLabel: 'Action',
                            required: true,
                            store: 'Isu.store.CreationRuleActions',
                            queryMode: 'local',
                            displayField: 'name',
                            valueField: 'id',
                            allowBlank: false,
                            editable: false
                        },
                        {
                            itemId: 'actionTypeDetails',
                            xtype: 'container',
                            name: 'actionTypeDetails',
                            defaults: {
                                labelWidth: 150,
                                margin: '0 0 10 0',
                                validateOnChange: false,
                                validateOnBlur: false,
                                width: 700
                            }
                        }
                    ],
                    buttons: [
                        {
                            itemId: 'actionOperation',
                            name: 'actionOperation',
                            ui: 'action',
                            formBind: false,
                            action: 'actionOperation'
                        },
                        {
                            itemId: 'cancel',
                            text: 'Cancel',
                            action: 'cancel',
                            ui: 'link',
                            name: 'cancel'
                        }
                    ]
                }
            ]
        }
    ]
});

Ext.define('Isu.model.CreationRuleAction', {
    extend: 'Ext.data.Model',
    idProperty: 'id',
    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'type',
            type: 'auto'
        },
        {
            name: 'phase',
            type: 'auto'
        },
        {
            name: 'parameters',
            type: 'auto'
        }
    ]
});

Ext.define('Isu.model.Action', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'name',
            type: 'text'
        },
        {
            name: 'issueType',
            type: 'text'
        },
        {
            name: 'parameters',
            type: 'auto'
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/idc/actions',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

Ext.define('Isu.store.CreationRuleActions', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    model: 'Isu.model.Action',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/isu/actions',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

Ext.define('Isu.model.CreationRuleActionPhase', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'uuid',
            type: 'string'
        },
        {
            name: 'title',
            type: 'string'
        },
        {
            name: 'description',
            type: 'string'
        }
    ],

    idProperty: 'uuid'
});

Ext.define('Isu.store.CreationRuleActionPhases', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.CreationRuleActionPhase',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/isu/actions/phases',
        reader: {
            type: 'json',
            root: 'data'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});

Ext.define('Isu.store.Clipboard', {
    extend: 'Ext.data.Store',
    fields: [
        {
            name: 'id',
            type: 'string'
        },
        {
            name: 'value',
            type: 'auto'
        }
    ],

    set: function (name, obj) {
        var model = this.getById(name);

        if (model) {
            model.set('value', obj)
        } else {
            this.add({
                id: name,
                value: obj
            });
        }
    },

    get: function (name) {
        var model = this.getById(name);

        if (model) {
            return model.get('value');
        } else {
            return model;
        }
    },

    clear: function (name) {
        var model = this.getById(name);

        this.remove(model);
    }
});

Ext.define('Isu.model.User', {
    extend: 'Ext.data.Model',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    fields: [
        {
            name: 'id',
            displayValue: 'ID',
            type: 'int'
        },
        {
            name: 'type',
            displayValue: 'Type',
            type: 'auto'
        },
        {
            name: 'name',
            displayValue: 'Name',
            type: 'auto'
        }
    ],

    // GET ?like="operator"
    proxy: {
        type: 'rest',
        url: '/api/isu/assignees/users',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

Ext.define('Isu.store.Users', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.User',
    autoLoad: false
});

Ext.define('Isu.controller.CreationRuleActionEdit', {
    extend: 'Ext.app.Controller',

    stores: [
        'Isu.store.CreationRuleActions',
        'Isu.store.CreationRuleActionPhases',
        'Isu.store.Clipboard',
        'Isu.store.Users'
    ],

    views: [
        'Isu.view.creationrules.EditAction'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'issues-creation-rules-edit-action'
        },
        {
            ref: 'pageTitle',
            selector: 'issues-creation-rules-edit-action [name=pageTitle]'
        },
        {
            ref: 'actionOperationBtn',
            selector: 'issues-creation-rules-edit-action button[name=actionOperation]'
        },
        {
            ref: 'actionForm',
            selector: 'issues-creation-rules-edit-action form'
        },
        {
            ref: 'phasesRadioGroup',
            selector: 'issues-creation-rules-edit-action form [name=phasesRadioGroup]'
        },
        {
            ref: 'actionTypeDetails',
            selector: 'issues-creation-rules-edit-action form [name=actionTypeDetails]'
        }
    ],

    models: [
        'Isu.model.CreationRuleAction'
    ],

    mixins: [
        'Isu.util.CreatingControl'
    ],

    init: function () {
        this.control({
            'issues-creation-rules-edit-action form [name=actionType]': {
                change: this.setActionTypeDetails
            },
            'issues-creation-rules-edit-action button[action=cancel]': {
                click: this.finishEdit
            },
            'issues-creation-rules-edit-action button[action=actionOperation]': {
                click: this.saveAction
            }
        });
    },

    showCreate: function (id) {
        var widget;

        if (!this.getStore('Isu.store.Clipboard').get('issuesCreationRuleState')) {
            this.getController('Uni.controller.history.Router').getRoute('administration/creationrules/add').forward();
            return
        }

        widget = Ext.widget('issues-creation-rules-edit-action');

        Ext.util.History.on('change', this.checkRoute, this);

        this.setPage(id, 'create');
        this.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
    },

    checkRoute: function (token) {
        var clipboard = this.getStore('Isu.store.Clipboard'),
            createRegexp = /administration\/creationrules\/add/,
            editRegexp = /administration\/creationrules\/\d+\/edit/;

        Ext.util.History.un('change', this.checkRoute, this);

        if (token.search(createRegexp) == -1 && token.search(editRegexp) == -1) {
            clipboard.clear('issuesCreationRuleState');
        }
    },

    setPage: function (id, action) {
        var me = this,
            actionTypesStore = me.getStore('Isu.store.CreationRuleActions'),
            actionTypesPhases = me.getStore('Isu.store.CreationRuleActionPhases'),
            loadedStoresCount = 0,
            prefix,
            btnTxt;

        var checkLoadedStores = function () {
            loadedStoresCount++;

            if (loadedStoresCount == 2) {
                switch (action) {
                    case 'create':
                        prefix = btnTxt = 'Add ';
                        me.actionModel = Ext.create('Isu.model.CreationRuleAction');
                        break;
                }

                me.getPageTitle().setTitle(prefix + 'action');
                me.getActionOperationBtn().setText(btnTxt);
                me.getPage().setLoading(false);
            }
        };

        actionTypesStore.getProxy().setExtraParam('issueType', me.getStore('Isu.store.Clipboard').get('issuesCreationRuleState').get('issueType').uid);
        actionTypesStore.load(checkLoadedStores);
        actionTypesPhases.load(function (records) {
            var phasesRadioGroup = me.getPhasesRadioGroup();

            Ext.Array.each(records, function (record, index) {
                phasesRadioGroup.add({
                    boxLabel: record.get('title'),
                    name: 'phase',
                    inputValue: record.get('uuid'),
                    afterSubTpl: '<span style="color: #686868; font-style: italic">' + record.get('description') + '</span>',
                    checked: !index
                });
            });
            checkLoadedStores();
        });
    },

    formToModel: function (model) {
        var form = this.getActionForm(),
            phaseField = form.down('[name=phasesRadioGroup]'),
            actionStore = this.getStore('Isu.store.CreationRuleActions'),
            actionField = form.down('[name=actionType]'),
            action = actionStore.getById(actionField.getValue()),
            parameters = {};

        model.set('type', action.getData());
        delete model.get('type').parameters;
        model.set('phase', {
            uuid: phaseField.getValue().phase
        });
        Ext.Array.each(form.down('[name=actionTypeDetails]').query(), function (formItem) {
            if (formItem.isFormField) {
                parameters[formItem.name] = formItem.getValue();
            }
        });
        model.set('parameters', parameters);

        return model;
    },

    setActionTypeDetails: function (combo, newValue) {
        var me = this,
            actionTypesStore = me.getStore('Isu.store.CreationRuleActions'),
            parameters = actionTypesStore.getById(newValue).get('parameters'),
            actionTypeDetails = me.getActionTypeDetails();

        actionTypeDetails.removeAll();

        Ext.Object.each(parameters, function(key, value) {
            var formItem = me.createControl(value);

            formItem && actionTypeDetails.add(formItem);
        });
    },

    saveAction: function () {
        var rule = this.getStore('Isu.store.Clipboard').get('issuesCreationRuleState'),
            form = this.getActionForm().getForm(),
            formErrorsPanel = this.getActionForm().down('[name=form-errors]'),
            newAction,
            actions;

        if (rule) {
            if (form.isValid()) {
                newAction = this.formToModel(this.actionModel);
                actions = rule.actions();
                formErrorsPanel.hide();
                actions.add(newAction);
                this.finishEdit();
            } else {
                formErrorsPanel.show();
            }
        } else {
            this.finishEdit();
        }

    },

    finishEdit: function () {
        var router = this.getController('Uni.controller.history.Router'),
            rule = this.getStore('Isu.store.Clipboard').get('issuesCreationRuleState');

        if (rule) {
            if (rule.getId()) {
                router.getRoute('administration/creationrules/edit').forward({id: rule.getId()});
            } else {
                router.getRoute('administration/creationrules/add').forward();
            }
        } else {
            router.getRoute('administration/creationrules').forward();
        }
    }
});

Ext.define('Isu.view.creationrules.ActionsList', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Isu.model.CreationRuleAction'
    ],
    alias: 'widget.issues-creation-rules-actions-list',
    store: Ext.create('Ext.data.Store', {
        model: 'Isu.model.CreationRuleAction'
    }),
    enableColumnHide: false,
    columns: {
        items: [
            {
                itemId: 'description',
                header: 'Description',
                xtype: 'templatecolumn',
                tpl: '{type.name}',
                flex: 1
            },
            {
                itemId: 'phase',
                header: 'When to perform',
                xtype: 'templatecolumn',
                tpl: new Ext.XTemplate('{[this.getWhenToPerform(values.phase.uuid)]}', {
                    getWhenToPerform: function (uuid) {
                        var phasesStore = Ext.getStore('Isu.store.CreationRuleActionPhases'),
                            whenToPerform = phasesStore.getById(uuid).get('title');

                        return (whenToPerform || '');
                    }
                }),
                flex: 1
            },
            {
                itemId: 'action',
                xtype: 'uni-actioncolumn',
                items: [
                    {
                        text: Uni.I18n.translate('general.remove', 'ISU', 'Remove'),
                        action: 'delete'
                    }
                ]
            }
        ]
    }
});

Ext.define('Isu.view.creationrules.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Isu.view.creationrules.ActionsList',
        'Uni.util.FormErrorMessage'
    ],
    alias: 'widget.issues-creation-rules-edit',
    content: [
        {
            name: 'pageTitle',
            ui: 'large',
            items: [
                {
                    xtype: 'form',
                    defaults: {
                        labelWidth: 150,
                        validateOnChange: false,
                        validateOnBlur: false,
                        width: 700
                    },
                    items: [
                        {
                            itemId: 'form-errors',
                            xtype: 'uni-form-error-message',
                            name: 'form-errors',
                            hidden: true
                        },
                        {
                            itemId: 'name',
                            xtype: 'textfield',
                            name: 'name',
                            fieldLabel: 'Name',
                            required: true,
                            allowBlank: false,
                            maxLength: 80
                        },
                        {
                            itemId: 'issueType',
                            xtype: 'combobox',
                            name: 'issueType',
                            fieldLabel: 'Issue type',
                            required: true,
                            store: 'Isu.store.IssueTypes',
                            queryMode: 'local',
                            displayField: 'name',
                            valueField: 'uid',
                            allowBlank: false,
                            editable: false
                        },
                        {
                            itemId: 'ruleTemplate',
                            xtype: 'combobox',
                            name: 'template',
                            fieldLabel: 'Rule template',
                            required: true,
                            store: 'Isu.store.CreationRuleTemplates',
                            queryMode: 'local',
                            displayField: 'name',
                            valueField: 'uid',
                            allowBlank: false,
                            editable: false
                        },
                        {
                            itemId: 'templateDetails',
                            xtype: 'container',
                            name: 'templateDetails',
                            defaults: {
                                labelWidth: 150,
                                margin: '0 0 10 0',
                                validateOnChange: false,
                                validateOnBlur: false,
                                width: 700
                            }
                        },
                        {
                            itemId: 'issueReason',
                            xtype: 'combobox',
                            name: 'reason',
                            fieldLabel: 'Issue reason',
                            required: true,
                            store: 'Isu.store.IssueReasons',
                            queryMode: 'local',
                            displayField: 'name',
                            valueField: 'id',
                            allowBlank: false,
                            editable: false
                        },
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: 'Due date',
                            layout: 'hbox',
                            items: [
                                {
                                    itemId: 'dueDateTrigger',
                                    xtype: 'radiogroup',
                                    name: 'dueDateTrigger',
                                    formBind: false,
                                    columns: 1,
                                    vertical: true,
                                    width: 100,
                                    defaults: {
                                        name: 'dueDate',
                                        formBind: false,
                                        submitValue: false
                                    },
                                    items: [
                                        {
                                            itemId: 'noDueDate',
                                            boxLabel: 'No due date',
                                            inputValue: false
                                        },
                                        {
                                            itemId: 'dueIn',
                                            boxLabel: 'Due in',
                                            inputValue: true
                                        }
                                    ],
                                    listeners: {
                                        change: {
                                            fn: function (radioGroup, newValue, oldValue) {
                                                this.up('issues-creation-rules-edit').dueDateTrigger(radioGroup, newValue, oldValue);
                                            }
                                        }
                                    }
                                },
                                {
                                    itemId: 'dueDateValues',
                                    xtype: 'container',
                                    name: 'dueDateValues',
                                    margin: '30 0 10 0',
                                    layout: {
                                        type: 'hbox'
                                    },
                                    items: [
                                        {
                                            itemId: 'dueIn.number',
                                            xtype: 'numberfield',
                                            name: 'dueIn.number',
                                            minValue: 1,
                                            width: 60,
                                            margin: '0 10 0 0',
                                            listeners: {
                                                focus: {
                                                    fn: function () {
                                                        var radioButton = Ext.ComponentQuery.query('issues-creation-rules-edit [boxLabel=Due in]')[0];
                                                        radioButton.setValue(true);
                                                    }
                                                }
                                            }
                                        },
                                        {
                                            itemId: 'dueIn.type',
                                            xtype: 'combobox',
                                            name: 'dueIn.type',
                                            store: 'Isu.store.DueinTypes',
                                            queryMode: 'local',
                                            displayField: 'displayValue',
                                            valueField: 'name',
                                            editable: false,
                                            width: 100,
                                            listeners: {
                                                focus: {
                                                    fn: function () {
                                                        var radioButton = Ext.ComponentQuery.query('issues-creation-rules-edit [boxLabel=Due in]')[0];
                                                        radioButton.setValue(true);
                                                    }
                                                }
                                            }
                                        }
                                    ]
                                }
                            ]
                        },
                        {
                            itemId: 'comment',
                            xtype: 'textareafield',
                            name: 'comment',
                            fieldLabel: 'Comment',
                            emptyText: 'Provide a comment (optionally)',
                            height: 100
                        },
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: 'Actions',
                            width: 900,
                            items: [
                                {
                                    xtype: 'button',
                                    itemId: 'addAction',
                                    text: 'Add action',
                                    action: 'addAction',
                                    margin: '0 0 10 0'
                                },
                                {
                                    xtype: 'issues-creation-rules-actions-list',
                                    hidden: true
                                },
                                {
                                    name: 'noactions',
                                    html: 'There are no actions added yet to this rule',
                                    hidden: true
                                }
                            ]
                        },
                        {
                            xtype: 'fieldcontainer',
                            ui: 'actions',
                            fieldLabel: '&nbsp',
                            defaultType: 'button',
                            items: [
                                {
                                    itemId: 'ruleAction',
                                    name: 'ruleAction',
                                    ui: 'action',
                                    action: 'save'
                                },
                                {
                                    itemId: 'cancel',
                                    text: 'Cancel',
                                    ui: 'link',
                                    name: 'cancel',
                                    href: '#/administration/creationrules'
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ],

    dueDateTrigger: function (radioGroup, newValue) {
        var dueDateValues = this.down('form [name=dueDateValues]'),
            dueInNumberField = this.down('form [name=dueIn.number]'),
            dueInTypeField = this.down('form [name=dueIn.type]');

        if (!newValue.dueDate) {
            dueInNumberField.reset();
            dueInTypeField.setValue(dueInTypeField.getStore().getAt(0).get('name'));
        }
    }
});

Ext.define('Isu.model.CreationRule', {
    extend: 'Ext.data.Model',
    requires: [
        'Isu.model.CreationRuleAction'
    ],
    idProperty: 'id',
    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'parameters',
            type: 'auto'
        },
        {
            name: 'comment',
            type: 'string'
        },
        {
            name: 'creationDate',
            dateFormat: 'time',
            type: 'date'
        },
        {
            name: 'modificationDate',
            dateFormat: 'time',
            type: 'date'
        },
        {
            name: 'version',
            type: 'int'
        },
        {
            name: 'template',
            type: 'auto'
        },
        {
            name: 'issueType',
            type: 'auto'
        },
        {
            name: 'reason',
            type: 'auto'
        },
        {
            name: 'dueIn',
            type: 'auto'
        },
        {
            name: 'title',
            mapping: 'name'
        },
        {
            name: 'issueType_name',
            mapping: 'issueType.name'
        },
        {
            name: 'reason_name',
            mapping: 'reason.name'
        },
        {
            name: 'template_name',
            mapping: 'template.name'
        },
        {
            name: 'due_in',
            mapping: function (data) {
                var dueIn = '';

                if (data.dueIn && data.dueIn.number) {
                    dueIn =  data.dueIn.number + ' ' + data.dueIn.type;
                }

                return dueIn;
            }
        }
    ],

    associations: [
        {
            name: 'actions',
            type: 'hasMany',
            model: 'Isu.model.CreationRuleAction',
            associationKey: 'actions'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/creationrules',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

Ext.define('Isu.store.CreationRules', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.CreationRule',
    pageSize: 10,
    autoLoad: false
});

Ext.define('Isu.model.IssueType', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'uid',
            type: 'text'
        },
        {
            name: 'name',
            type: 'text'
        }
    ],

    idProperty: 'uid',

    proxy: {
        type: 'rest',
        url: '/api/isu/issuetypes',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

Ext.define('Isu.store.IssueTypes', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.IssueType',
    pageSize: 10,
    autoLoad: false
});

Ext.define('Isu.model.CreationRuleTemplate', {
    extend: 'Ext.data.Model',
    requires: [
        'Isu.model.CreationRule'
    ],
    belongsTo: 'Isu.model.CreationRule',
    fields: [
        {
            name: 'uid',
            type: 'string'
        },
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'description',
            type: 'string'
        },
        {
            name: 'parameters',
            type: 'auto'
        }
    ],

    idProperty: 'uid',

    proxy: {
        type: 'rest',
        url: '/api/isu/rules/templates',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

Ext.define('Isu.store.CreationRuleTemplates', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.CreationRuleTemplate',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/isu/rules/templates',
        reader: {
            type: 'json',
            root: 'data'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});

Ext.define('Isu.model.DueinType', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'displayValue',
            type: 'string'
        }
    ]
});

Ext.define('Isu.store.DueinTypes', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.DueinType',

    data: [
        {name: 'days', displayValue: 'day(s)'},
        {name: 'weeks', displayValue: 'week(s)'},
        {name: 'months', displayValue: 'month(s)'}
    ]
});

Ext.define('Isu.model.IssueReason', {
    extend: 'Ext.data.Model',
    requires: [
        'Ext.data.proxy.Rest'
    ],

    fields: [
        {
            name: 'id',
            type: 'auto'
        },
        {
            name: 'name',
            type: 'string'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/reasons',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

Ext.define('Isu.store.IssueReasons', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.IssueReason',
    autoLoad: false
});

Ext.define('Isu.controller.CreationRuleEdit', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    stores: [
        'Isu.store.CreationRules',
        'Isu.store.IssueTypes',
        'Isu.store.CreationRuleTemplates',
        'Isu.store.DueinTypes',
        'Isu.store.Clipboard',
        'Isu.store.CreationRuleActionPhases',
        'Isu.store.IssueReasons'
    ],
    views: [
        'Isu.view.creationrules.Edit'
    ],

    models: [
        'Isu.model.CreationRuleAction'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'issues-creation-rules-edit'
        },
        {
            ref: 'pageTitle',
            selector: 'issues-creation-rules-edit [name=pageTitle]'
        },
        {
            ref: 'ruleForm',
            selector: 'issues-creation-rules-edit form'
        },
        {
            ref: 'ruleActionBtn',
            selector: 'issues-creation-rules-edit button[name=ruleAction]'
        },
        {
            ref: 'templateDetails',
            selector: 'issues-creation-rules-edit form [name=templateDetails]'
        },
        {
            ref: 'actionsGrid',
            selector: 'issues-creation-rules-edit issues-creation-rules-actions-list'
        }
    ],

    mixins: [
        'Isu.util.CreatingControl'
    ],

    init: function () {
        this.control({
            'issues-creation-rules-edit form [name=issueType]': {
                change: this.setRuleTemplateCombobox
            },
            'issues-creation-rules-edit form [name=template]': {
                change: this.setRuleTemplate,
                resize: this.comboTemplateResize
            },
            'issues-creation-rules-edit': {
                beforedestroy: this.removeTemplateDescription
            },
            'issues-creation-rules-edit button[action=save]': {
                click: this.ruleSave
            },
            'issues-creation-rules-edit button[action=addAction]': {
                click: this.addAction
            },
            'issues-creation-rules-edit issues-creation-rules-actions-list uni-actioncolumn': {
                menuclick: this.chooseActionOperation
            }
        });

        this.on('templateloaded', this.checkDependencies, this);
    },

    showCreate: function (id) {
        var widget = Ext.widget('issues-creation-rules-edit');

        this.setPage(id, 'create', widget);
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    showEdit: function (id) {
        var widget = Ext.widget('issues-creation-rules-edit');

        this.setPage(id, 'edit', widget);
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    clearActionsStore: function (widget) {
        var actionsGrid = widget ? widget.down('issues-creation-rules-actions-list') : this.getActionsGrid(),
            actionsStore = actionsGrid.getStore();

        actionsStore.removeAll();
    },

    setPage: function (id, action, widget) {
        var me = this,
            ruleActionBtn = me.getRuleActionBtn(),
            clipboard = this.getStore('Isu.store.Clipboard'),
            savedData = clipboard.get('issuesCreationRuleState'),
            title,
            btnTxt;

        this.clearActionsStore(widget);

        switch (action) {
            case 'edit':
                title = Uni.I18n.translate('administration.issueCreationRules.title.editIssueCreationRule', 'ISU', 'Edit');
                btnTxt = Uni.I18n.translate('general.save', 'ISU', 'Save');
                if (savedData) {
                    me.ruleModel = savedData;
                    clipboard.clear('issuesCreationRuleState');
                    widget.on('afterrender', function () {
                        me.modelToForm(me.ruleModel);
                    }, me, {single: true});
                } else {
                    me.getModel('Isu.model.CreationRule').load(id, {
                        success: function (record) {
                            me.ruleModel = record;
                            delete me.ruleModel.data.creationDate;
                            delete me.ruleModel.data.modificationDate;
                            if (widget.isVisible()) {
                                me.modelToForm(record);
                            } else {
                                widget.on('afterrender', function () {
                                    me.modelToForm(record);
                                }, me, {single: true});
                            }
                        }
                    });
                }
                break;
            case 'create':
                title = Uni.I18n.translate('administration.issueCreationRules.title.addIssueCreationRule', 'ISU', 'Add issue creation rule');
                btnTxt = Uni.I18n.translate('general.add', 'ISU', 'Add');
                if (savedData) {
                    me.ruleModel = savedData;
                    clipboard.clear('issuesCreationRuleState');
                } else {
                    me.ruleModel = Isu.model.CreationRule.create();
                    delete me.ruleModel.data.id;
                    me.ruleModel.data.actions = [];
                }
                widget.on('afterrender', function () {
                    me.modelToForm(me.ruleModel);
                }, me, {single: true});
                break;
        }

        me.getPageTitle().title = title;
        ruleActionBtn.setText(btnTxt);
    },

    setDataToModel: function (data, model) {
        for (var field in data) {
            model.set(field, data[field]);
        }
    },

    modelToForm: function (record) {
        var me = this,
            form = me.getRuleForm(),
            data = record.getData(),
            nameField = form.down('[name=name]'),
            issueTypeField = form.down('[name=issueType]'),
            templateField = form.down('[name=template]'),
            templateDetails = this.getTemplateDetails(),
            reasonField = form.down('[name=reason]'),
            dueDateTrigger = form.down('[name=dueDateTrigger]'),
            dueInNumberField = form.down('[name=dueIn.number]'),
            dueInTypeField = form.down('[name=dueIn.type]'),
            commentField = form.down('[name=comment]'),
            page = me.getPage();

        if (record.get('template') && record.get('template').uid) {
            page.setLoading(true);
            me.on('templateloaded', function () {
                var formField,
                    name,
                    value;

                if (data.parameters) {
                    for (name in data.parameters) {
                        formField = templateDetails.down('[name=' + name + '][isFormField=true]');
                        value = data.parameters[name];

                        formField && formField.setValue(value);
                    }
                }
            }, me, {single: true});
        }

        nameField.setValue(data.name);
        issueTypeField.getStore().load(function () {
            issueTypeField.setValue(data.issueType.uid || issueTypeField.getStore().getAt(0).get('uid'));
            reasonField.getStore().getProxy().setExtraParam('issueType', issueTypeField.getValue());
            page.setLoading(true);
            reasonField.getStore().load(function () {
                page.setLoading(false);
                if (!reasonField.isDestroyed) {
                    reasonField.setValue(data.reason.id);
                }
            });
            templateField.getStore().on('load', function () {
                templateField.setValue(data.template.uid);
            }, me, {single: true});
        });
        if (data.dueIn.number) {
            dueDateTrigger.setValue({dueDate: true});
            dueInNumberField.setValue(data.dueIn.number);
            dueInTypeField.setValue(data.dueIn.type || dueInTypeField.getStore().getAt(0).get('name'));
        } else {
            dueDateTrigger.setValue({dueDate: false});
        }
        commentField.setValue(data.comment);

        me.loadActionsToForm(record.actions().getRange());
    },

    formToModel: function (model) {
        var form = this.getRuleForm(),
            ruleModel = model || Isu.model.CreationRule.create(),
            nameField = form.down('[name=name]'),
            issueTypeField = form.down('[name=issueType]'),
            templateField = form.down('[name=template]'),
            reasonField = form.down('[name=reason]'),
            dueInNumberField = form.down('[name=dueIn.number]'),
            dueInTypeField = form.down('[name=dueIn.type]'),
            commentField = form.down('[name=comment]'),
            templateDetails = this.getTemplateDetails(),
            parameters = {};

        ruleModel.set('name', nameField.getValue());
        ruleModel.set('issueType', {
            uid: issueTypeField.getValue()
        });
        ruleModel.set('template', {
            uid: templateField.getValue()
        });
        ruleModel.set('reason', {
            id: reasonField.getValue()
        });
        ruleModel.set('dueIn', {
            number: dueInNumberField.getValue(),
            type: dueInTypeField.getValue()
        });
        ruleModel.set('comment', commentField.getValue());

        Ext.Array.each(templateDetails.query(), function (formItem) {
            if (formItem.isFormField && formItem.submitValue) {
                parameters[formItem.name] = formItem.getValue();
            }
        });

        ruleModel.set('parameters', parameters);
        this.loadActionsToModel(ruleModel);

        return ruleModel;
    },

    setRuleTemplateCombobox: function (combo, newValue) {
        var form = this.getRuleForm(),
            templateField = form.down('[name=template]'),
            templateStore = templateField.getStore(),
            templateStoreProxy = templateStore.getProxy(),
            reasonField = form.down('[name=reason]'),
            reasonStore = reasonField.getStore(),
            reasonStoreProxy = reasonStore.getProxy();

        templateStoreProxy.setExtraParam('issueType', newValue);
        templateField.reset();
        templateStore.load();
        reasonStoreProxy.setExtraParam('issueType', newValue);
        reasonField.reset();
        reasonStore.load();
    },

    setRuleTemplate: function (combo, newValue) {
        var me = this,
            templateDetails = me.getTemplateDetails(),
            templateModel = combo.getStore().model,
            formItem;

        templateDetails.removeAll();

        if (newValue) {
            templateModel.load(newValue, {
                success: function (template) {
                    var description = template.get('description'),
                        parameters = template.get('parameters');

                    me.addTemplateDescription(combo, description);

                    Ext.Array.each(parameters, function (obj) {
                        formItem = me.createControl(obj);
                        formItem && templateDetails.add(formItem);
                    });
                    me.fireEvent('templateloaded', template);
                }
            });
        }
    },

    addTemplateDescription: function (combo, descriptionText) {
        var form = this.getRuleForm();

        this.removeTemplateDescription();

        if (descriptionText) {
            combo.templateDescriptionIcon = Ext.widget('button', {
                tooltip: Uni.I18n.translate('administration.issueCreationRules.templateInfo', 'ISU', 'Template info'),
                iconCls: 'icon-info-small',
                ui: 'blank',
                itemId: 'creationRuleTplHelp',
                floating: true,
                renderTo: form.getEl(),
                shadow: false,
                width: 16,
                handler: function () {
                    combo.templateDescriptionWindow = Ext.Msg.show({
                        title: Uni.I18n.translate('administration.issueCreationRules.templateDescription', 'ISU', 'Template description'),
                        msg: descriptionText,
                        buttons: Ext.MessageBox.CANCEL,
                        buttonText: {cancel: Uni.I18n.translate('general.close', 'ISU', 'Close')},
                        modal: true,
                        animateTarget: combo.templateDescriptionIcon
                    })
                }
            });
            this.comboTemplateResize(combo);
        }
    },

    comboTemplateResize: function (combo) {
        var comboEl = combo.getEl(),
            icon = combo.templateDescriptionIcon;

        icon && icon.setXY([
                comboEl.getX() + comboEl.getWidth(false) + 5,
                comboEl.getY() + (comboEl.getHeight(false) - icon.getEl().getHeight(false)) / 2
        ]);
    },

    removeTemplateDescription: function () {
        var combo = Ext.ComponentQuery.query('issues-creation-rules-edit form [name=template]')[0];

        if (combo && combo.templateDescriptionIcon) {
            combo.templateDescriptionIcon.clearListeners();
            combo.templateDescriptionIcon.destroy();
            delete combo.templateDescriptionIcon;
        }
    },

    ruleSave: function (button) {
        var me = this,
            form = me.getRuleForm().getForm(),
            rule = me.formToModel(me.ruleModel),
            formErrorsPanel = me.getRuleForm().down('[name=form-errors]'),
            store = me.getStore('Isu.store.CreationRules'),
            templateCombo = me.getRuleForm().down('combobox[name=template]'),
            router = this.getController('Uni.controller.history.Router'),
            page = me.getPage();

        if (form.isValid()) {
            page.setLoading('Saving...');
            button.setDisabled(true);
            formErrorsPanel.hide();
            rule.save({
                callback: function (model, operation, success) {
                    var messageText,
                        json;

                    page.setLoading(false);
                    button.setDisabled(false);

                    if (success) {
                        switch (operation.action) {
                            case 'create':
                                messageText = Uni.I18n.translate('administration.issueCreationRules.createSuccess.msg', 'ISU', 'Issue creation rule added');
                                break;
                            case 'update':
                                messageText = Uni.I18n.translate('administration.issueCreationRules.updateSuccess.msg', 'ISU', 'Issue creation rule updated');
                                break;
                        }
                        me.getApplication().fireEvent('acknowledge', messageText);
                        router.getRoute('administration/creationrules').forward();
                    } else {
                        json = Ext.decode(operation.response.responseText);
                        if (json && json.errors) {
                            form.markInvalid(json.errors);
                            formErrorsPanel.show();
                            me.comboTemplateResize(templateCombo);
                        }
                    }
                }
            });
        } else {
            formErrorsPanel.show();
            me.comboTemplateResize(templateCombo);
        }
    },

    addAction: function () {
        var router = this.getController('Uni.controller.history.Router'),
            rule = this.formToModel(this.ruleModel);

        this.getStore('Isu.store.Clipboard').set('issuesCreationRuleState', rule);

        router.getRoute('administration/creationrules/add/addaction').forward();
    },

    loadActionsToForm: function (actions) {
        var actionsGrid = this.getActionsGrid(),
            actionsStore = actionsGrid.getStore(),
            noActionsText = this.getPage().down('[name=noactions]'),
            phasesStore = this.getStore('Isu.store.CreationRuleActionPhases');

        if (actions.length) {
            phasesStore.load(function () {
                actionsStore.loadData(actions, false);
                actionsGrid.show();
                noActionsText.hide();
            });
        } else {
            actionsGrid.hide();
            noActionsText.show();
        }
    },

    loadActionsToModel: function (model) {
        var me = this,
            actionsGrid = me.getActionsGrid(),
            actionsStore = actionsGrid.getStore(),
            actions = actionsStore.getRange();

        model.actions().loadData(actions, false);
    },

    chooseActionOperation: function (menu, item) {
        var operation = item.action,
            actionId = menu.record.getId();

        switch (operation) {
            case 'delete':
                this.deleteAction(actionId);
                break;
        }
    },

    deleteAction: function (id) {
        var actionsGrid = this.getActionsGrid(),
            actionsStore = actionsGrid.getStore(),
            action = actionsStore.getById(id),
            noActionsText = this.getPage().down('[name=noactions]');

        actionsStore.remove(action);

        if (!actionsStore.getCount()) {
            actionsGrid.hide();
            noActionsText.show();
        }
    },

    checkDependencies: function (template) {
        var me = this,
            templateId = template ? template.getId() : me.getRuleForm().down('#ruleTemplate').getValue(),
            templateDetails = me.getTemplateDetails(),
            parametersFields = templateDetails.query('[isFormField=true]');

        Ext.Array.each(parametersFields, function (field) {
            var linkedFields = [];

            if (field.dependOn) {
                linkedFields.push(field);
                Ext.Array.each(field.dependOn, function (dependOnName) {
                    var dependOnField = templateDetails.down('[name=' + dependOnName + ']');
                    linkedFields.push(dependOnField);
                    dependOnField && dependOnField.on('blur', function () {
                        var data = {};
                        Ext.Array.each(linkedFields, function (linkedField) {
                            data[linkedField.name] = linkedField.getValue();
                        });
                        Ext.Ajax.request({
                            url: ' /api/isu/rules/templates/' + templateId + '/parameters/' + field.name,
                            method: 'PUT',
                            jsonData: Ext.encode(data),
                            success: function (response) {
                                var responseTextObj = Ext.decode(response.responseText, true),
                                    newControl = me.createControl(responseTextObj.data),
                                    oldControl = templateDetails.down('[name=' + newControl.name + ']'),
                                    index = templateDetails.query().indexOf(oldControl);
                                oldControl.destroy();
                                templateDetails.insert(index, newControl);
                                me.checkDependencies();
                            }
                        });
                    }, me, {single: true});
                });
            }
        });
    }
});

Ext.define('Isu.view.creationrules.List', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.layout.container.Column',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    alias: 'widget.issues-creation-rules-list',
    store: 'Isu.store.CreationRules',
    columns: {
        items: [
            {
                itemId: 'Name',
                header: Uni.I18n.translate('general.title.name', 'ISU', 'Name'),
                dataIndex: 'name',
                flex: 1
            },
            {
                itemId: 'templateColumn',
                header: Uni.I18n.translate('general.title.ruleTemplate', 'ISU', 'Rule template'),
                xtype: 'templatecolumn',
                tpl: '<tpl if="template">{template.name}</tpl>',
                flex: 1
            },
            {
                itemId : 'issueType',
                header: Uni.I18n.translate('general.title.issueType', 'ISU', 'Issue type'),
                xtype: 'templatecolumn',
                tpl: '<tpl if="issueType">{issueType.name}</tpl>',
                flex: 1
            },
            {   itemId: 'action',
                xtype: 'uni-actioncolumn',
                hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.creationRule'),
                items: 'Isu.view.creationrules.ActionMenu'
            }
        ]
    },

    initComponent: function () {
        var me = this;

        me.dockedItems = [
            {
                itemId: 'pagingtoolbartop',
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('administration.issueCreationRules.pagingtoolbartop.displayMsg', 'ISU', '{0} - {1} of {2} issue creation rules'),
                displayMoreMsg: Uni.I18n.translate('administration.issueCreationRules.pagingtoolbartop.displayMoreMsg', 'ISU', '{0} - {1} of more than {2} issue creation rules'),
                emptyMsg: Uni.I18n.translate('administration.issueCreationRules.pagingtoolbartop.emptyMsg', 'ISU', 'There are no issue creation rules to display'),
                items: [
                    '->',
                    {
                        itemId: 'createRule',
                        xtype: 'button',
                        text: Uni.I18n.translate('administration.issueCreationRules.add', 'ISU', 'Add rule'),
                        hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.creationRule'),
                        href: '#/administration/creationrules/add',
                        action: 'create'
                    }
                ]
            },
            {
                itemId: 'pagingtoolbarbottom',
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('administration.issueCreationRules.pagingtoolbarbottom.itemsPerPage', 'ISU', 'Issue creation rules per page')
            }
        ];

        this.callParent(arguments);
    }
});

Ext.define('Isu.view.creationrules.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.creation-rule-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'edit',
            text: 'Edit',
            action: 'edit'
        },
        {
            itemId: 'delete',
            text: 'Delete',
            action: 'delete'
        }
    ]
});

Ext.define('Isu.view.creationrules.Item', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Isu.view.creationrules.ActionMenu'
    ],
    alias: 'widget.issue-creation-rules-item',
    title: 'Details',
    itemId: 'issue-creation-rules-item',
    frame: true,
    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'ISU', 'Actions'),
            hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.creationRule'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'creation-rule-action-menu'
            }
        }
    ],
    items: {
        xtype: 'form',
        layout: 'column',
        defaults: {
            xtype: 'container',
            layout: 'form',
            columnWidth: 0.5
        },
        items: [
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('general.title.name', 'ISU', 'Name'),
                        name: 'name'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.title.ruleTemplate', 'ISU', 'Rule template'),
                        name: 'template_name'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.title.issueType', 'ISU', 'Issue type'),
                        name: 'issueType_name'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.title.issueReason', 'ISU', 'Issue reason'),
                        name: 'reason_name'
                    },

                    {
                        fieldLabel: Uni.I18n.translate('general.title.dueIn', 'ISU', 'Due in'),
                        name: 'due_in'
                    }
                ]
            },
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('general.title.created', 'ISU', 'Created'),
                        name: 'creationDate',
                        renderer: Ext.util.Format.dateRenderer('M d, Y H:i')
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.title.lastModified', 'ISU', 'Last modified'),
                        name: 'modificationDate',
                        renderer: Ext.util.Format.dateRenderer('M d, Y H:i')
                    }
                ]
            }
        ]
    }
});

Ext.define('Isu.view.creationrules.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.issue-creation-rules-overview',
    itemId: 'creation-rules-overview',

    requires: [
        'Isu.view.creationrules.List',
        'Isu.view.creationrules.Item',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('administration.issueCreationRules.title', 'ISU', 'Issue creation rules'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        itemId: 'creation-rules-list',
                        xtype: 'issues-creation-rules-list'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('administration.issueCreationRules.empty.title', 'ISU', 'No issue creation rules found'),
                        reasons: [
                            Uni.I18n.translate('administration.issueCreationRules.empty.list.item1', 'ISU', 'No issue creation rules have been defined yet.'),
                            Uni.I18n.translate('administration.issueCreationRules.empty.list.item2', 'ISU', 'No issue creation rules comply to the filter.')
                        ],
                        stepItems: [
                            {
                                itemId: 'createRule',
                                text: Uni.I18n.translate('administration.issueCreationRules.add', 'ISU', 'Add rule'),
                                privileges:['privilege.administrate.creationRule'],
                                href: '#/administration/creationrules/add',
                                action: 'create'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'issue-creation-rules-item'
                    }
                }
            ]
        }
    ]
});

Ext.define('Isu.controller.CreationRules', {
    extend: 'Ext.app.Controller',

    stores: [
        'Isu.store.CreationRules'
    ],

    views: [
        'Isu.view.creationrules.Overview'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'issue-creation-rules-overview'
        },
        {
            ref: 'itemPanel',
            selector: 'issue-creation-rules-overview issue-creation-rules-item'
        },
        {
            ref: 'rulesGridPagingToolbarTop',
            selector: 'issue-creation-rules-overview issues-creation-rules-list pagingtoolbartop'
        }
    ],

    init: function () {
        this.control({
            'issue-creation-rules-overview issues-creation-rules-list': {
                select: this.showPreview
            },
            'issues-creation-rules-list uni-actioncolumn': {
                menuclick: this.chooseAction
            },
            'creation-rule-action-menu': {
                click: this.chooseAction
            },
            'issue-creation-rules-overview button[action=create]': {
                click: this.createRule
            }
        });
    },

    showOverview: function () {
        var widget = Ext.widget('issue-creation-rules-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    showPreview: function (selectionModel, record) {
        var itemPanel = this.getItemPanel(),
            form = itemPanel.down('form');

        itemPanel.setLoading(true);

        this.getModel('Isu.model.CreationRule').load(record.getId(), {
            success: function (record) {
                if (!form.isDestroyed) {
                    form.loadRecord(record);
                    form.up('panel').down('menu').record = record;
                    itemPanel.setLoading(false);
                    itemPanel.fireEvent('afterChange',itemPanel);
                    itemPanel.setTitle(record.data.title);
                }
            }
        });
    },

    chooseAction: function (menu, item) {
        var action = item.action;
        var id = menu.record.getId();
        var router = this.getController('Uni.controller.history.Router');

        switch (action) {
            case 'delete':
                this.showDeleteConfirmation(menu.record);
                break;
            case 'edit':
                router.getRoute('administration/creationrules/edit').forward({id: id});
                break;
        }
    },

    createRule: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('administration/creationrules/add').forward();
    },

    showDeleteConfirmation: function (rule) {
        var me = this;

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('administration.issueCreationRules.deleteConfirmation.msg', 'ISU', 'This issue creation rule will disappear from the list.<br>Issues will not be created automatically by this rule.'),
            title: Ext.String.format(Uni.I18n.translate('administration.issueCreationRules.deleteConfirmation.title', 'ISU', 'Delete rule "{0}"?'), rule.get('name')),
            config: {
                me: me,
                rule: rule
            },
            fn: function (state) {
                switch (state) {
                    case 'confirm':
                        this.close();
                        me.deleteRule(rule);
                        break;
                    case 'cancel':
                        this.close();
                        break;
                }
            }
        });
    },

    deleteRule: function (rule) {
        var me = this,
            store = this.getStore('Isu.store.CreationRules'),
            page = this.getPage();

        page.setLoading('Removing...');
        rule.destroy({
            params: {
                version: rule.get('version')
            },
            callback: function (model, operation) {
                page.setLoading(false);
                if (operation.response.status == 204) {
                    page.down('#creation-rules-list pagingtoolbartop').totalCount = 0;
                    store.loadPage(1);
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('administration.issueCreationRules.deleteSuccess.msg', 'ISU', 'Issue creation rule deleted'));
                }
            }
        });
    }
});

Ext.define('Isu.controller.IssueDetail', {
    extend: 'Ext.app.Controller',

    showOverview: function (id, issueModel, issuesStore, widgetXtype) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget(widgetXtype, {
                router: router
            });

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        me.getModel(issueModel).load(id, {
            callback: function () {
                widget.setLoading(false);
            },
            success: function (record) {
                if (!widget.isDestroyed) {
                    me.getApplication().fireEvent('issueLoad', record);
                    widget.down('#issue-detail-top-title').setTitle(record.get('title'));
                    me.getDetailForm().loadRecord(record);
                    widget.down('issues-action-menu').record = record;
                    me.loadComments(record);
                    me.setNavigationButtons(record, me.getStore(issuesStore));
                }
            },
            failure: function () {
                router.getRoute(router.currentRoute.replace('/view', '')).forward();
            }
        });
    },

    loadComments: function (record) {
        var commentsView = this.getPage().down('#issue-comments-view'),
            commentsStore = record.comments();

        commentsStore.getProxy().url = record.getProxy().url + '/' + record.getId() + '/comments';
        commentsView.bindStore(commentsStore);
        commentsView.setLoading(true);
        commentsStore.load(function (records) {
            if (!commentsView.isDestroyed) {
                commentsStore.add(records);
                commentsView.setLoading(false);
                commentsView.previousSibling('#no-issue-comments').setVisible(!records.length);
            }
        });
        if (this.getController('Uni.controller.history.Router').queryParams.addComment) {
            this.showCommentForm();
        }
    },

    showCommentForm: function () {
        var commentsPanel = this.getCommentsPanel();

        commentsPanel.down('#issue-add-comment-form').show();
        commentsPanel.down('#issue-add-comment-area').focus();
        commentsPanel.down('#issue-comments-add-comment-button').hide();
    },

    hideCommentForm: function () {
        var commentsPanel = this.getCommentsPanel();

        commentsPanel.down('#issue-add-comment-form').hide();
        commentsPanel.down('#issue-add-comment-area').reset();
        commentsPanel.down('#issue-comments-add-comment-button').show();
    },

    validateCommentForm: function (textarea, newValue) {
        this.getCommentsPanel().down('#issue-comment-save-button').setDisabled(!newValue.trim().length);
    },

    addComment: function () {
        var me = this,
            commentsPanel = me.getCommentsPanel(),
            commentsStore = commentsPanel.down('#issue-comments-view').getStore();

        commentsStore.add(commentsPanel.down('#issue-add-comment-form').getValues());
        commentsStore.sync();
        commentsPanel.down('#no-issue-comments').hide();
        me.hideCommentForm();
    },

    setNavigationButtons: function (record, issuesStore) {
        var me = this,
            navigationPanel = this.getNavigation(),
            prevBtn = navigationPanel.down('[action=prev]'),
            nextBtn = navigationPanel.down('[action=next]'),
            currentIndex = issuesStore.indexOf(record),
            router = this.getController('Uni.controller.history.Router'),
            prevIndex,
            nextIndex;

        if (currentIndex !== -1) {
            currentIndex && (prevIndex = currentIndex - 1);
            (issuesStore.getCount() > (currentIndex + 1)) && (nextIndex = currentIndex + 1);

            if (prevIndex || prevIndex == 0) {
                prevBtn.setHref(router.getRoute(router.currentRoute).buildUrl({issueId: issuesStore.getAt(prevIndex).getId()}));
                prevBtn.on('click', function () {
                    router.getRoute(router.currentRoute).forward({issueId: issuesStore.getAt(prevIndex).getId()});
                }, me, {single: true});
            } else {
                prevBtn.setDisabled(true);
            }

            if (nextIndex) {
                nextBtn.setHref(router.getRoute(router.currentRoute).buildUrl({issueId: issuesStore.getAt(nextIndex).getId()}));
                nextBtn.on('click', function () {
                    router.getRoute(router.currentRoute).forward({issueId: issuesStore.getAt(nextIndex).getId()});
                }, me, {single: true});
            } else {
                nextBtn.setDisabled(true);
            }

            navigationPanel.show();
        } else {
            navigationPanel.hide();
        }
    },

    chooseAction: function (menu, menuItem) {
        if (!Ext.isEmpty(menuItem.actionRecord)) {
            this.applyActionImmediately(menu.record, menuItem.actionRecord);
        }
    },

    applyActionImmediately: function (issue, action) {
        var me = this,
            actionModel = Ext.create(issue.actions().model);

        actionModel.setId(action.getId());
        actionModel.set('parameters', {});
        actionModel.getProxy().url = issue.getProxy().url + '/' + issue.getId() + '/actions';
        actionModel.save({
            callback: function (model, operation, success) {
                var responseText = Ext.decode(operation.response.responseText, true);

                if (responseText) {
                    if (responseText.data.actions[0].success) {
                        me.getApplication().fireEvent('acknowledge', responseText.data.actions[0].message);
                        me.getModel(issue.$className).load(issue.getId(), {
                            success: function (record) {
                                var form = me.getDetailForm();

                                if (form) {
                                    form.loadRecord(record);
                                }
                            }
                        });
                    } else {
                        me.getApplication().getController('Uni.controller.Error').showError(model.get('name'), responseText.data.actions[0].message);
                    }
                }
            }
        });
    }
});

/**
 * This class is used as a mixin.
 *
 * This class is to be used to provide basic methods for controllers that handle events of combobox.
 */
Ext.define('Isu.util.IsuComboTooltip', {
    /**
     * Sets tooltip for combobox.
     * Combobox must has 'tooltipText' property otherwise it sets default text.
     */
    setComboTooltip: function (combo) {
        combo.tooltip = Ext.DomHelper.append(Ext.getBody(), {
            tag: 'div',
            html: Ext.String.htmlEncode(combo.tooltipText || 'Start typing'),
            cls: 'isu-combo-tooltip'
        }, true);

        combo.tooltip.hide();

        combo.on('destroy', function () {
            combo.tooltip.destroy();
        });
        combo.on('focus', this.onFocusComboTooltip, this);
        combo.on('blur', this.onBlurComboTooltip, this);
    },

    /**
     * Handle 'focus' event.
     * If value of combobox is null shows tooltip.
     */
    onFocusComboTooltip: function (combo) {
        var tooltip = combo.tooltip,
            comboEl = Ext.get(combo.getEl());

        tooltip.setStyle({
            width: comboEl.getWidth(false) + 'px',
            top: comboEl.getY() + comboEl.getHeight(false) + 'px',
            left: comboEl.getX() + 'px'
        });

        if (!combo.getValue()) {
            tooltip.show();
        }

        combo.on('change', this.clearComboTooltip, this);
    },

    /**
     * Handle 'blur' event.
     * Hides tooltip of combobox on blur.
     */
    onBlurComboTooltip: function (combo) {
        var tooltip = combo.tooltip;

        tooltip && tooltip.hide();

        combo.un('change', this.clearComboTooltip, this);
    },

    /**
     * Handle 'change' event.
     * If value of combobox is null resets combobox and shows tooltip otherwise hides tooltip
     * and shows list of values.
     */
    clearComboTooltip: function (combo, newValue) {
        var listValues = combo.picker,
            tooltip = combo.tooltip;

        if (newValue == null) {
            combo.reset();
            listValues && listValues.hide();
            tooltip && tooltip.show();
        } else {
            tooltip && tooltip.hide();
            if (listValues) {
                listValues.show();
                Ext.get(listValues.getEl()).setStyle({
                    visibility: 'visible'
                });
            }
        }
    },

    limitNotification: function (combo) {
        var picker = combo.getPicker();

        if (picker) {
            picker.un('refresh', this.triggerLimitNotification, this);
            picker.on('refresh', this.triggerLimitNotification, this);
        }
    },

    triggerLimitNotification: function (view) {
        var store = view.getStore(),
            el = view.getEl().down('.' + Ext.baseCSSPrefix + 'list-plain');

        if (store.getTotalCount() > store.getCount()) {
            el.appendChild({
                tag: 'li',
                html: 'Keep typing to narrow down',
                cls: Ext.baseCSSPrefix + 'boundlist-item isu-combo-limit-notification'
            });
        }
    }
});

Ext.define('Isu.controller.IssuesOverview', {
    extend: 'Ext.app.Controller',

    mixins: [
        'Isu.util.IsuComboTooltip'
    ],

    showOverview: function (issueType, widgetXtype) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            grouping = router.filter.get('grouping');

        if (router.queryParams.myopenissues) {
            delete router.queryParams.myopenissues;
            me.getStore('Isu.store.IssueAssignees').load({params: {me: true}, callback: function (records) {
                router.filter.set('assignee', records[0].getId());
                router.filter.set('status', 'status.open');
                router.filter.set('sorting', [
                    {type: 'dueDate', value: Uni.component.sort.model.Sort.ASC}
                ]);
                router.filter.save();
            }});
        } else if (!router.queryParams.filter) {
            router.filter.set('status', 'status.open');
            router.filter.set('sorting', [
                {type: 'dueDate', value: Uni.component.sort.model.Sort.ASC}
            ]);
            router.filter.save();
        } else {
            me.getStore('Isu.store.IssueStatuses').getProxy().setExtraParam('issueType', issueType);
            me.getStore('Isu.store.IssueReasons').getProxy().setExtraParam('issueType', issueType);

            me.getApplication().fireEvent('changecontentevent', Ext.widget(widgetXtype, {
                router: router,
                groupingType: grouping && grouping.type ? grouping.type : 'none'
            }));

            me.setFilter();
        }
    },

    showPreview: function (selectionModel, record) {
        var preview = this.getPreview();

        preview.setLoading(true);

        this.getModel(record.$className).load(record.getId(), {
            success: function (record) {
                if (!preview.isDestroyed) {
                    preview.loadRecord(record);
                    preview.down('issues-action-menu').record = record;
                    preview.down('#issue-view-details-link').setHref(preview.router.getRoute(preview.router.currentRoute + '/view').buildUrl({issueId: record.getId()}));
                    preview.setTitle(record.get('title'));
                }
            },
            callback: function () {
                if (!preview.isDestroyed) {
                    preview.setLoading(false);
                }
            }
        });
    },

    chooseAction: function (menu, menuItem) {
        if (!Ext.isEmpty(menuItem.actionRecord)) {
            this.applyActionImmediately(menu.record, menuItem.actionRecord);
        }
    },

    applyActionImmediately: function (issue, action) {
        var me = this,
            actionModel = Ext.create(issue.actions().model);

        actionModel.setId(action.getId());
        actionModel.set('parameters', {});
        actionModel.getProxy().url = issue.getProxy().url + '/' + issue.getId() + '/actions';
        actionModel.save({
            callback: function (model, operation, success) {
                var response = Ext.decode(operation.response.responseText, true);

                if (response) {
                    if (response.data.actions[0].success) {
                        me.getApplication().fireEvent('acknowledge', response.data.actions[0].message);
                        me.getIssuesGrid().getStore().load();
                    } else {
                        me.getApplication().getController('Uni.controller.Error').showError(model.get('name'), responseText.data.actions[0].message);
                    }
                }
            }
        });
    },

    setFilter: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            filterForm = me.getFilterForm();

        filterForm.setLoading(true);
        me.loadFilterFormDependencies(router.filter, function () {
            filterForm.loadRecord(router.filter);
            filterForm.setLoading(false);
            me.setFilterToolbar(filterForm);
        });
        me.setGrouping(router.filter);
        me.setSortingToolbar(router.filter);
    },

    setFilterToolbar: function (filterForm) {
        var filterToolbar = this.getFilterToolbar(),
            statusesGroupField = filterForm.down('[name=status]'),
            statusFieldLabel = statusesGroupField.getFieldLabel();

        Ext.Array.each(statusesGroupField.query('checkbox'), function (checkbox) {
            if (checkbox.getValue()) {
                filterToolbar.setFilter(checkbox.getName() + '|' + checkbox.inputValue, statusFieldLabel, checkbox.boxLabel);
            }
        });

        Ext.Array.each(filterForm.query('combobox'), function (combo) {
            var value = combo.getRawValue();

            if (!_.isEmpty(value)) {
                filterToolbar.setFilter(combo.getName(), combo.getFieldLabel(), value);
            }
        });
    },

    loadFilterFormDependencies: function (filterModel, callback) {
        var me = this,
            assigneesStore = this.getStore('Isu.store.IssueAssignees'),
            reasonsStore = this.getStore('Isu.store.IssueReasons'),
            metersStore = this.getStore('Isu.store.Devices'),
            assignee = filterModel.get('assignee'),
            reason = filterModel.get('reason'),
            meter = filterModel.get('meter'),
            dependenciesCount = 1,
            checkDependenciesLoading = function () {
                dependenciesCount--;
                if (dependenciesCount === 0) {
                    callback();
                }
            };

        if (!Ext.isEmpty(assignee)) {
            dependenciesCount++;
        }
        if (!Ext.isEmpty(reason)) {
            dependenciesCount++;
        }
        if (!Ext.isEmpty(meter)) {
            dependenciesCount++;
        }

        this.getStore('Isu.store.IssueStatuses').load(checkDependenciesLoading);
        if (!Ext.isEmpty(assignee)) {
            if (assigneesStore.getById(assignee)) {
                checkDependenciesLoading();
            } else {
                this.getModel('Isu.model.IssueAssignee').load(assignee, {
                    callback: checkDependenciesLoading,
                    success: function (record) {
                        assigneesStore.add(record);
                    }
                });
            }
        }
        if (!Ext.isEmpty(reason)) {
            if (reasonsStore.getById(reason)) {
                checkDependenciesLoading();
            } else {
                this.getModel('Isu.model.IssueReason').load(reason, {
                    callback: checkDependenciesLoading,
                    success: function (record) {
                        reasonsStore.add(record);
                    }
                });
            }
        }
        if (!Ext.isEmpty(meter)) {
            if (metersStore.getById(meter)) {
                checkDependenciesLoading();
            } else {
                this.getModel('Isu.model.Device').load(meter, {
                    callback: checkDependenciesLoading,
                    success: function (record) {
                        metersStore.add(record);
                    }
                });
            }
        }
    },

    applyFilter: function () {
        var me = this,
            filterForm = me.getFilterForm();

        filterForm.updateRecord();
        filterForm.getRecord().save();
    },

    resetFilter: function () {
        this.getController('Uni.controller.history.Router').filter.getProxy().destroy();
    },

    setFilterItem: function (button) {
        var me = this,
            filterModel = me.getController('Uni.controller.history.Router').filter;

        switch (button.filterBy) {
            case 'status':
                filterModel.set(button.filterBy, [button.filterValue.id]);
                break;
            case 'assignee':
                if (button.filterValue) {
                    filterModel.set(button.filterBy, [button.filterValue.id, button.filterValue.type].join(':'));
                } else {
                    filterModel.set(button.filterBy, '-1:UnexistingType');
                }
                break;
            case 'device':
                filterModel.set('meter', button.filterValue.serialNumber);
                break;
            default:
                filterModel.set(button.filterBy, button.filterValue.id);
        }

        filterModel.save();
    },

    removeFilterItem: function (key) {
        var filter = this.getController('Uni.controller.history.Router').filter,
            statusRegExp = /status\|\w+/;

        if (key.indexOf('status|') === 0) {
            if (Ext.isArray(filter.get('status'))) {
                Ext.Array.remove(filter.get('status'), key.split('|')[1]);
            }
        } else {
            filter.set(key, null);
        }

        filter.save();
    },

    setGroupingType: function (combo, newValue) {
        var filter = this.getController('Uni.controller.history.Router').filter;

        filter.set('grouping', {
            type: newValue
        });
        filter.save();
    },

    setGroupingValue: function (selectionModel, record) {
        var filter = this.getController('Uni.controller.history.Router').filter;

        filter.get('grouping').value = record.getId();
        filter.save();
    },

    setGrouping: function (filter) {
        var me = this,
            grouping = filter.get('grouping'),
            groupGrid = me.getGroupGrid(),
            groupStore = groupGrid.getStore(),
            groupProxyParams = {issueType: 'datacollection'},
            previewContainer = me.getPreviewContainer();

        if (grouping && grouping.type !== 'none') {
            groupGrid.show();
            groupProxyParams.field = grouping.type;
            Ext.iterate(filter.getData(), function (key, value) {
                if (value) {
                    switch (key) {
                        case 'assignee':
                            groupProxyParams.assigneeId = value.split(':')[0];
                            groupProxyParams.assigneeType = value.split(':')[1];
                            break;
                        case 'grouping':
                            break;
                        case grouping.type:
                            groupProxyParams.id = value;
                            break;
                        default:
                            groupProxyParams[key] = value;
                    }
                }
            });
            groupStore.load({
                params: groupProxyParams,
                callback: function () {
                    var groupingTitle = me.getGroupingTitle(),
                        groupingRecord = groupStore.getById(grouping.value);

                    if (grouping.value && groupingRecord) {
                        groupGrid.getSelectionModel().select(groupingRecord);
                        groupingTitle.setTitle(Ext.String.format(groupingTitle.title, grouping.type, groupingRecord.get('reason')));
                        groupingTitle.show();
                    } else {
                        groupingTitle.hide();
                    }
                }
            });
            if (!grouping.value) {
                previewContainer.hide();
                me.getNoGroupSelectedPanel().show();
            }
        } else {
            groupGrid.hide();
        }
    },

    setSortingToolbar: function (filter) {
        var me = this;

        me.getSortingToolbar().addSortButtons(filter.get('sorting'));
    },

    addSortingItem: function (menu, menuItem) {
        var filter = this.getController('Uni.controller.history.Router').filter,
            sorting = filter.get('sorting');

        if (Ext.isArray(sorting)) {
            sorting.push({
                type: menuItem.action,
                value: Uni.component.sort.model.Sort.ASC
            });
        } else {
            sorting = [
                {
                    type: menuItem.action,
                    value: Uni.component.sort.model.Sort.ASC
                }
            ];
        }

        filter.save();
    },

    removeSortingItem: function (sortType) {
        var filter = this.getController('Uni.controller.history.Router').filter,
            sorting = filter.get('sorting');

        if (Ext.isArray(sorting)) {
            Ext.Array.remove(sorting, Ext.Array.findBy(sorting, function (item) {
                return item.type === sortType
            }));
        }

        filter.save();
    },

    changeSortDirection: function (sortType) {
        var filter = this.getController('Uni.controller.history.Router').filter,
            sorting = filter.get('sorting'),
            sortingItem;

        if (Ext.isArray(sorting)) {
            sortingItem = Ext.Array.findBy(sorting, function (item) {
                return item.type === sortType
            });
            if (sortingItem) {
                if (sortingItem.value === Uni.component.sort.model.Sort.ASC) {
                    sortingItem.value = Uni.component.sort.model.Sort.DESC;
                } else {
                    sortingItem.value = Uni.component.sort.model.Sort.ASC;
                }
            }
        }

        filter.save();
    }
});

Ext.define('Isu.controller.history.Administration', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',
    previousPath: '',
    currentPath: null,

    routeConfig: {
        administration : {
            title: Uni.I18n.translate('route.administration', 'ISU', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                assignmentrules: {
                    title: Uni.I18n.translate('route.assignmentRules', 'ISU', 'Assignment Rules'),
                    route: 'assignmentrules',
                    controller: 'Isu.controller.AssignmentRules',
                    privileges: ['privilege.view.assignmentRule']
                },
                creationrules: {
                    title: Uni.I18n.translate('route.issueCreationRules', 'ISU', 'Issue creation rules'),
                    route: 'creationrules',
                    controller: 'Isu.controller.CreationRules',
                    privileges: ['privilege.administrate.creationRule','privilege.view.creationRule'],
                    items: {
                        add: {
                            title: Uni.I18n.translate('route.addIssueCreationRule', 'ISU', 'Add issue creation rule'),
                            route: 'add',
                            controller: 'Isu.controller.CreationRuleEdit',
                            privileges: ['privilege.administrate.creationRule'],
                            action: 'showCreate',
                            items: {
                                addaction: {
                                    title: Uni.I18n.translate('route.addAction', 'ISU', 'Add action'),
                                    route: 'addaction',
                                    controller: 'Isu.controller.CreationRuleActionEdit',
                                    action: 'showCreate'
                                }
                            }
                        },
                        edit: {
                            title: 'Edit',
                            route: '{id}/edit',
                            controller: 'Isu.controller.CreationRuleEdit',
                            action: 'showEdit',
                            privileges: ['privilege.administrate.creationRule']
                        }
                    }
                }
            }
        }
    },

    init :function() {
        var router = this.getController('Uni.controller.history.Router');
        router.addConfig(this.routeConfig);
    }
});

Ext.define('Isu.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation',
        'Uni.controller.Configuration',
        'Uni.controller.history.EventBus',
        'Uni.model.PortalItem',
        'Uni.store.PortalItems',
        'Uni.store.MenuItems'
    ],

    controllers: [
        'Isu.controller.history.Administration',
        'Isu.controller.AssignmentRules',
        'Isu.controller.CreationRules',
        'Isu.controller.CreationRuleEdit',
        'Isu.controller.CreationRuleActionEdit',
        'Isu.controller.IssuesOverview',
        'Isu.controller.IssueDetail',
        'Isu.controller.ApplyIssueAction'
    ],

    init: function () {
        this.initMenu();
    },

    initMenu: function () {
        var me = this,
            issuemanagement = null,
            issuemanagementItems = [],
            router = me.getController('Uni.controller.history.Router'),
            historian = me.getController('Isu.controller.history.Administration'); // Forces route registration.

        if (Uni.Auth.hasAnyPrivilege(['privilege.view.assignmentRule','privilege.administrate.creationRule','privilege.view.creationRule'])){
            Uni.store.MenuItems.add(Ext.create('Uni.model.MenuItem', {
                text: 'Administration',
                glyph: 'settings',
                portal: 'administration',
                index: 10
            }));
        }

        if (Uni.Auth.hasAnyPrivilege(['privilege.view.assignmentRule','privilege.administrate.creationRule','privilege.view.creationRule'])){
            if(Uni.Auth.hasAnyPrivilege(['privilege.view.assignmentRule'])) {
                issuemanagementItems.push(
                    {
                        text: 'Issue assignment rules',
                        href: router.getRoute('administration/assignmentrules').buildUrl()
                    }
                );
            }
            if(Uni.Auth.hasAnyPrivilege(['privilege.administrate.creationRule','privilege.view.creationRule'])) {
                issuemanagementItems.push(
                    {
                        text: 'Issue creation rules',
                        href: router.getRoute('administration/creationrules').buildUrl()
                    }
                );
            }
            issuemanagement = Ext.create('Uni.model.PortalItem', {
                title: 'Issue management',
                portal: 'administration',
                route: 'issuemanagement',
                items: issuemanagementItems
            });
        }

        if (issuemanagement !== null) {
            Uni.store.PortalItems.add(issuemanagement);
        }
    }
});

Ext.define('Isu.model.Device', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int'},
        {name: 'name', type: 'string'},
        {name: 'serialNumber', type: 'string'},
        {name: 'usagePoint', type: 'auto'},
        {name: 'serviceLocation', type: 'auto'},
        {name: 'serviceCategory', type: 'auto'},
        {name: 'version', type: 'int'}
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/meters',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

Ext.define('Isu.model.IssueStatus', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'auto'},
        {name: 'name', type: 'string'},
        {name: 'allowForClosing', type: 'boolean'}
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/statuses',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

Ext.define('Isu.model.IssueAssignee', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'idx',
            type: 'string',
            convert: function (value, record) {
                var idx = null,
                    id = record.get('id'),
                    type = record.get('type');

                if (id && type) {
                    idx = id + ':' + type;
                }

                return idx;
            }
        },
        {
            name: 'type',
            type: 'auto'
        },
        {
            name: 'name',
            type: 'auto'
        }
    ],

    idProperty: 'idx',

    // GET ?like="operator"
    proxy: {
        type: 'rest',
        url: '/api/isu/assignees',
        reader: {
            type: 'json',
            root: 'data'
        },
        buildUrl: function(request) {
            var idx = request.params.id,
                params;

            if (idx) {
                params = idx.split(':');
                return this.url + '/' + params[0] + '?assigneeType=' + params[1];
            } else {
                return this.url
            }
        }
    }
});

Ext.define('Isu.model.IssueComment', {
    extend: 'Ext.data.Model',
    requires: [
        'Ext.data.proxy.Rest'
    ],

    fields: [
        {
            name: 'author',
            type: 'auto'
        },
        {
            name: 'comment',
            type: 'string'
        },
        {
            name: 'creationDate',
            dateFormat: 'time',
            type: 'date'
        },
        {
            name: 'version',
            type: 'int'
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/idc/issue/{issue_id}/comments',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

Ext.define('Isu.model.IssueAction', {
    extend: 'Isu.model.Action',
    proxy: {
        type: 'rest',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

Ext.define('Isu.model.Issue', {
    extend: 'Ext.data.Model',
    requires: [
        'Isu.model.IssueReason',
        'Isu.model.IssueStatus',
        'Isu.model.Device',
        'Isu.model.IssueAssignee',
        'Isu.model.IssueComment',
        'Isu.model.IssueAction'
    ],
    fields: [
        {name: 'id', type: 'int'},
        {name: 'dueDate', type: 'date', dateFormat: 'time'},
        {name: 'creationDate', type: 'date', dateFormat: 'time'},
        {name: 'version', type: 'int'},
        {name: 'status', type: 'auto'},
        {name: 'assignee', type: 'auto'},
        {name: 'reason', type: 'auto'},
        {name: 'device', type: 'auto'},
        {
            name: 'title',
            persist: false,
            mapping: function (data) {
                return data.reason.name + (data.device ? ' to ' + data.device.name + ' ' + data.device.serialNumber : '');
            }
        },
        {name: 'reason_name', persist: false, mapping: 'reason.name'},
        {name: 'status_name', persist: false, mapping: 'status.name'},
        {name: 'device_name', persist: false, mapping: 'device.name'},
        {name: 'assignee_name', persist: false, mapping: 'assignee.name'},
        {name: 'assignee_type', persist: false, mapping: 'assignee.type'},
        {name: 'usage_point', persist: false, mapping: 'device.usagePoint.info'},
        {name: 'service_location', persist: false, mapping: 'device.serviceLocation.info'},
        {name: 'service_category', persist: false, mapping: 'device.serviceCategory.info'}
    ],
    associations: [
        {
            type: 'hasOne',
            model: 'Isu.model.IssueReason',
            associationKey: 'reason',
            name: 'reason',
            getterName: 'getReason',
            setterName: 'setReason'
        },
        {
            type: 'hasOne',
            model: 'Isu.model.IssueStatus',
            associationKey: 'status',
            name: 'status',
            getterName: 'getStatus',
            setterName: 'setStatus'
        },
        {
            type: 'hasOne',
            model: 'Isu.model.Device',
            associationKey: 'device',
            name: 'device',
            getterName: 'getDevice',
            setterName: 'setDevice'
        },
        {
            type: 'hasOne',
            model: 'Isu.model.IssueAssignee',
            associationKey: 'assignee',
            name: 'assignee',
            getterName: 'getAssignee',
            setterName: 'setAssignee'
        },
        {
            type: 'hasMany',
            model: 'Isu.model.IssueComment',
            associationKey: 'comments',
            name: 'comments'
        },
        {
            type: 'hasMany',
            model: 'Isu.model.IssueAction',
            associationKey: 'actions',
            name: 'actions'
        }
    ]
});

Ext.define('Isu.model.IssuesFilter', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.data.proxy.QueryStringProxy'
    ],
    fields: [
        'status',
        'assignee',
        'reason',
        'meter',
        'grouping',
        'sorting'
    ],

    proxy: {
        type: 'querystring',
        root: 'filter',
        destroy: function () {
            var filter = Ext.decode(this.router.queryParams[this.root]);

            Ext.iterate(filter, function (key, value) {
                if (key !== 'grouping' && key !== 'sorting') {
                    filter[key] = '';
                }
            });

            this.router.queryParams[this.root] = Ext.encode(filter);
            this.router.getRoute().forward(this.router.arguments, this.router.queryParams);
        }
    }
});

Ext.define('Isu.store.Devices', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.Device',
    pageSize: 50,
    autoLoad: false
});

Ext.define('Isu.store.IssueActions', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    model: 'Isu.model.Action',
    autoLoad: false,
    proxy: {
        type: 'rest',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

Ext.define('Isu.store.IssueAssignees', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    model: 'Isu.model.IssueAssignee',
    pageSize: 100,
    groupField: 'type',
    autoLoad: false,
    sorters: [{
        sorterFn: function(o1, o2){
            return o1.get('name').toUpperCase() > o2.get('name').toUpperCase()
        }
    }]
});

Ext.define('Isu.store.IssueGrouping', {
    extend: 'Ext.data.Store',
    fields: [
        {
            name: 'id',
            type: 'string'
        },
        {
            name: 'value',
            type: 'string'
        }
    ],
    data: [
        {
            id: 'none',
            value: 'None'
        },
        {
            id: 'reason',
            value: 'Reason'
        }
    ]
});

Ext.define('Isu.store.IssueStatuses', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.IssueStatus',
    autoLoad: false
});

Ext.define('Isu.view.component.AssigneeCombo', {
    extend: 'Ext.ux.Rixo.form.field.GridPicker',

    alias: 'widget.issues-assignee-combo',
    store: 'Isu.store.IssueAssignees',
    displayField: 'name',
    valueField: 'idx',

    triggerAction: 'query',
    queryMode: 'remote',
    queryParam: 'like',
    allQuery: '',
    lastQuery: '',
    queryDelay: 100,
    minChars: 0,
    disableKeyFilter: true,
    queryCaching: false,

    formBind: true,
    typeAhead: true,

    anchor: '100%',

    forceSelection: true,

    gridConfig: {
        emptyText: 'No assignee found',
        resizable: false,
        stripeRows: true,

        features: [
            {
                ftype: 'grouping',
                groupHeaderTpl: '{name}',
                collapsible: false
            }
        ],
        columns: [
            {
                header: false,
                xtype: 'templatecolumn',
                tpl: '<tpl if="type"><span class="isu-icon-{type} isu-assignee-type-icon"></span></tpl> {name}',
                flex: 1
            }
        ]
    },
    listeners: {
        focus: {
            fn: function(combo){
                if (!combo.getValue()) {
                    combo.doQuery(combo.getValue());
                }
            }
        },
        change: {
            fn: function(combo, newValue){
                if (!newValue){
                    combo.reset();
                }
            }
        },
        beforequery: {
            fn: function(queryPlan) {
                var store = queryPlan.combo.store;
                if (queryPlan.query) {
                    store.group('type');
                } else {
                    store.clearGrouping();
                }
            }
        }
    }
});


Ext.define('Isu.view.issues.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.issues-action-menu',
    store: 'Isu.store.IssueActions',
    plain: true,
    border: false,
    shadow: false,
    defaultAlign: 'tr-br?',
    minHeight: 60,
    router: null,
    mixins: {
        bindable: 'Ext.util.Bindable'
    },
    predefinedItems: [
        {
            text: Uni.I18n.translate('issues.actionMenu.addComment', 'ISU', 'Add comment'),
            hidden: Uni.Auth.hasNoPrivilege('privilege.comment.issue'),
            action: 'addComment'
        }
    ],
    listeners: {
        show: {
            fn: function () {
                var me = this;

                me.removeAll();
                if (me.record) {
                    me.store.getProxy().url = me.record.getProxy().url + '/' + me.record.getId() + '/actions';
                    me.store.load(function () {
                        me.onLoad();
                        me.setLoading(false);
                    });
                    setTimeout(function () {
                        me.setLoading(true);
                    }, 1)
                } else {
                    console.error('Record for \'' + me.xtype + '\' is not defined');
                }
            }
        }
    },
    initComponent: function () {
        var me = this;

        if (!me.router) {
            console.error('Router for \'' + me.xtype + '\' is not defined');
        }

        me.bindStore(me.store || 'ext-empty-store', true);

        this.callParent(arguments);
    },

    onLoad: function () {
        var me = this,
            deviceMRID,
            comTaskId,
            comTaskSessionId,
            connectionTaskId,
            comSessionId;

        if (!me.router) {
            return
        }

        me.removeAll();

        // add dynamic actions
        me.store.each(function (record) {
            var isHidden = false;
            switch (record.get('name')) {
                case 'Assign issue':
                    isHidden = Uni.Auth.hasNoPrivilege('privilege.assign.issue');
                    break;
                case 'Close issue':
                    isHidden = Uni.Auth.hasNoPrivilege('privilege.close.issue');
                    break;
                case 'Retry now':
                    isHidden = Uni.Auth.hasNoPrivilege('privilege.view.scheduleDevice');
                    break;
                case 'Send someone to inspect':
                    isHidden = Uni.Auth.hasNoPrivilege('privilege.action.issue');
                    break;
                case 'Notify user':
                    isHidden = Uni.Auth.hasNoPrivilege('privilege.action.issue');
                    break;
            }

            var menuItem = {
                text: record.get('name'),
                hidden: isHidden
            };

            if (Ext.isEmpty(record.get('parameters'))) {
                menuItem.actionRecord = record;
            } else {
                menuItem.href = me.router.getRoute(me.router.currentRoute.replace('/view', '') + '/view/action').buildUrl({issueId: me.record.getId(), actionId: record.getId()});
            }
            me.add(menuItem);
        });

        // add predefined actions
        if (me.predefinedItems && me.predefinedItems.length) {
            Ext.Array.each(me.predefinedItems, function (menuItem) {
                switch (menuItem.action) {
                    case 'addComment':
                        delete menuItem.action;
                        menuItem.href = me.router.getRoute(me.router.currentRoute.replace('/view', '') + '/view').buildUrl({issueId: me.record.getId()}, {addComment: true});
                        break;
                }
            });
            me.add(me.predefinedItems);
        }

        // add specific actions
        if (Uni.Auth.hasAnyPrivilege(['privilege.administrate.device','privilege.view.device'])) {
            deviceMRID = me.record.get('deviceMRID');
            if (deviceMRID) {
                comTaskId = me.record.get('comTaskId');
                comTaskSessionId = me.record.get('comTaskSessionId');
                connectionTaskId = me.record.get('connectionTaskId');
                comSessionId = me.record.get('comSessionId');
                if (comTaskId && comTaskSessionId) {
                    me.add({
                        text: Uni.I18n.translate('issues.actionMenu.viewCommunicationLog', 'ISU', 'View communication log'),
                        href: me.router.getRoute('devices/device/communicationtasks/history/viewlog').buildUrl(
                            {
                                mRID: deviceMRID,
                                comTaskId: comTaskId,
                                historyId: comTaskSessionId
                            },
                            {
                                filter: {
                                    logLevels: ['Error', 'Warning', 'Information']
                                }
                            }
                        ),
                        hrefTarget: '_blank'
                    });
                }
                if (connectionTaskId && comSessionId) {
                    me.add({
                        text: Uni.I18n.translate('issues.actionMenu.viewConnectionLog', 'ISU', 'View connection log'),
                        href: me.router.getRoute('devices/device/connectionmethods/history/viewlog').buildUrl(
                            {
                                mRID: deviceMRID,
                                connectionMethodId: connectionTaskId,
                                historyId: comSessionId
                            },
                            {
                                filter: {
                                    logLevels: ['Error', 'Warning', 'Information'],
                                    logTypes: ['connections', 'communications']
                                }
                            }
                        ),
                        hrefTarget: '_blank'
                    });
                }
            }
        }
    }
});

Ext.define('Isu.view.issues.AddCommentForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.issue-add-comment-form',
    layout: 'fit',
    items: {
        itemId: 'issue-add-comment-area',
        xtype: 'textareafield',
        height: 100,
        fieldLabel: 'Comment',
        labelAlign: 'top',
        name: 'comment'
    },

    bbar: {
        layout: {
            type: 'hbox',
            align: 'left'
        },
        items: [
            {
                itemId: 'issue-comment-save-button',
                text: 'Add',
                ui: 'action',
                action: 'send',
                disabled: true
            },
            {
                itemId: 'issue-comment-cancel-adding-button',
                text: 'Cancel',
                action: 'cancel',
                ui: 'link'
            }
        ]
    }
});

Ext.define('Isu.view.issues.CommentsList', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Isu.view.issues.AddCommentForm',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
    alias: 'widget.issue-comments',
    title: 'Comments',
    ui: 'medium',
    buttonAlign: 'left',
    items: [
        {
            xtype: 'no-items-found-panel',
            itemId: 'no-issue-comments',
            title: 'No comments found',
            reasons: [
                'No comments created yet on this issue'
            ],
            hidden: true
        },
        {
            xtype: 'dataview',
            itemId: 'issue-comments-view',
            title: 'User Images',
            itemSelector: 'div.thumb-wrap',
            tpl: new Ext.XTemplate(
                '<tpl for=".">',
                '{[xindex > 1 ? "<hr>" : ""]}',
                '<p><span class="isu-icon-USER"></span><b>{author.name}</b> added a comment - {[values.creationDate ? this.formatCreationDate(values.creationDate) : ""]}</p>',
                '<p>{comment}</p>',
                '</tpl>',
                {
                    formatCreationDate: function (date) {
                        date = Ext.isDate(date) ? date : new Date(date);
                        return Ext.Date.format(date, 'M d, Y (H:i)');
                    }
                }
            ),
            header: 'Name',
            dataIndex: 'name'
        },
        {
            xtype: 'issue-add-comment-form',
            itemId: 'issue-add-comment-form',
            hidden: true
        }
    ],

    buttons: [
        {
            itemId: 'issue-comments-add-comment-button',
            ui: 'action',
            text: 'Add comment',
            hidden: Uni.Auth.hasNoPrivilege('privilege.comment.issue'),
            action: 'add'
        }
    ]
});

Ext.define('Isu.view.issues.DetailNavigation', {
    extend: 'Ext.toolbar.Toolbar',
    alias: 'widget.issue-detail-navigation',
    defaultButtonUI: 'link',
    rtl: false,
    items: [
        {
            xtype: 'tbfill'
        },
        {
            itemId: 'data-collection-issue-navigation-previous',
            text: Uni.I18n.translate('general.previous', 'ISU', 'Previous'),
            action: 'prev'
        },
        {
            itemId: 'data-collection-issue-navigation-next',
            text: Uni.I18n.translate('general.next', 'ISU', 'Next'),
            action: 'next'
        }
    ]
});

Ext.define('Isu.view.issues.DetailTop', {
    extend: 'Ext.container.Container',
    alias: 'widget.issue-detail-top',
    requires: [
        'Isu.view.issues.ActionMenu'
    ],
    layout: {
        type: 'hbox',
        align: 'middle'
    },
    router: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'issue-detail-top-title',
                title: '&nbsp;',
                ui: 'large',
                flex: 1
            },
            {
                xtype: 'button',
                itemId: 'issue-detail-top-actions-button',
                text: Uni.I18n.translate('general.actions', 'ISU', 'Actions'),
                hidden:  Uni.Auth.hasAnyPrivilege(['privilege.comment.issue','privilege.close.issue','privilege.assign.issue','privilege.action.issue',
                    'privilege.administrate.device','privilege.view.device','privilege.view.scheduleDevice']),
                iconCls: 'x-uni-action-iconD',
                menu: {
                    xtype: 'issues-action-menu',
                    itemId: 'issue-detail-action-menu',
                    predefinedItems: null,
                    router: me.router
                },
                listeners: {
                    click: function () {
                        this.showMenu();
                    }
                }
            }
        ];

        me.callParent(arguments);
    }
});

Ext.define('Isu.view.issues.Grid', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.grid.column.Date',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Isu.view.issues.ActionMenu',
        'Isu.view.component.AssigneeColumn'
    ],
    alias: 'widget.issues-grid',
    router: null,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                itemId: 'issues-grid-title',
                header: Uni.I18n.translate('general.title.title', 'ISU', 'Title'),
                dataIndex: 'title',
                flex: 2,
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute(me.router.currentRoute + '/view').buildUrl({issueId: record.getId()});

                    return '<a href="' + url + '">' + value + '</a>';
                }
            },
            {
                itemId: 'issues-grid-due-date',
                header: Uni.I18n.translate('general.title.dueDate', 'ISU', 'Due date'),
                dataIndex: 'dueDate',
                xtype: 'datecolumn',
                format: 'M d Y',
                width: 140
            },
            {
                itemId: 'issues-grid-status',
                header: Uni.I18n.translate('general.title.status', 'ISU', 'Status'),
                dataIndex: 'status_name',
                width: 100
            },
            {
                itemId: 'issues-grid-assignee',
                header: Uni.I18n.translate('general.title.assignee', 'ISU', 'Assignee'),
                xtype: 'isu-assignee-column',
                dataIndex: 'assignee',
                flex: 1
            },
            {
                itemId: 'action',
                xtype: 'uni-actioncolumn',
                hidden:  Uni.Auth.hasAnyPrivilege(['privilege.comment.issue','privilege.close.issue','privilege.assign.issue','privilege.action.issue',
                    'privilege.administrate.device','privilege.view.device','privilege.view.scheduleDevice']),
                menu: {
                    xtype: 'issues-action-menu',
                    itemId: 'issues-overview-action-menu',
                    router: me.router
                }
            }
        ];

        me.dockedItems = [
            {
                itemId: 'pagingtoolbartop',
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('workspace.issues.pagingtoolbartop.displayMsg', 'ISU', '{0} - {1} of {2} issues'),
                displayMoreMsg: Uni.I18n.translate('workspace.issues.pagingtoolbartop.displayMoreMsg', 'ISU', '{0} - {1} of more than {2} issues'),
                emptyMsg: Uni.I18n.translate('workspace.issues.pagingtoolbartop.emptyMsg', 'ISU', 'There are no issues to display'),
                items: [
                    '->',
                    {
                        xtype: 'button',
                        itemId: 'issues-bulk-action',
                        text: Uni.I18n.translate('general.title.bulkActions', 'ISU', 'Bulk action'),
                        hidden: !Uni.Auth.hasAnyPrivilege(['privilege.close.issue', 'privilege.assign.issue']),
                        action: 'issuesBulkAction',
                        href: me.router.getRoute(me.router.currentRoute + '/bulkaction').buildUrl()
                    }
                ]
            },
            {
                itemId: 'pagingtoolbarbottom',
                xtype: 'pagingtoolbarbottom',
                dock: 'bottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('workspace.issues.pagingtoolbarbottom.itemsPerPage', 'ISU', 'Issues per page')
            }
        ];
        me.callParent(arguments);
    }
});

Ext.define('Isu.view.issues.GroupGrid', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    alias: 'widget.issues-group-grid',
    columns: [
        {
            itemId: 'reason',
            text: 'Reason',
            dataIndex: 'reason',
            flex: 1
        },
        {
            itemId: 'issues_num',
            text: 'Issues',
            dataIndex: 'number',
            width: 100
        }
    ],

    initComponent: function () {
        var me = this;

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('workspace.issues.groupGrid.pagingtoolbartop.displayMsg', 'ISU', '{0} - {1} of {2} items'),
                displayMoreMsg: Uni.I18n.translate('workspace.issues.groupGrid.pagingtoolbartop.displayMoreMsg', 'ISU', '{0} - {1} of more than {2} items'),
                emptyMsg: '0 reasons'
            },
            {
                xtype: 'pagingtoolbarbottom',
                dock: 'bottom',
                store: me.store,
                deferLoading: true,
                itemsPerPageMsg: Uni.I18n.translate('workspace.issues.groupGrid.pagingtoolbartop.itemsPerPageMsg', 'ISU', 'Items per page')
            }
        ];

        me.callParent(arguments);
    }
});

Ext.define('Isu.view.issues.GroupingTitle', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.issues-grouping-title',
    ui: 'medium',
    title: Uni.I18n.translate('general.issuesFor', 'ISU', 'Issues for {0}: {1}'),
    padding: 0
});

Ext.define('Isu.view.issues.GroupingToolbar', {
    extend: 'Uni.view.panel.FilterToolbar',
    alias: 'widget.issues-grouping-toolbar',
    title: Uni.I18n.translate('general.group', 'ISU', 'Group'),
    showClearButton: false,
    store: 'Isu.store.IssueGrouping',
    groupingType: null,
    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'combobox',
            itemId: 'issues-grouping-toolbar-combo',
            store: me.store,
            editable: true,
            forceSelection: true,
            value: me.groupingType,
            queryMode: 'local',
            displayField: 'value',
            valueField: 'id'
        };

        me.callParent(arguments);
    }
});

Ext.define('Isu.view.issues.NoGroupSelectedPanel', {
    extend: 'Uni.view.notifications.NoItemsFoundPanel',
    alias: 'widget.no-issues-group-selected-panel',
    title: Uni.I18n.translate('issues.group.empty.title', 'ISU', 'No group selected'),
    reasons: [
        Uni.I18n.translate('issues.group.empty.list.item1', 'ISU', 'No group have been selected yet.')
    ],
    stepItems: [
        {
            xtype: 'component',
            html: Uni.I18n.translate('issues.group.selectGroup', 'ISU', 'Select a group of issues.')
        }
    ]
});

Ext.define('Isu.view.issues.NoIssuesFoundPanel', {
    extend: 'Uni.view.notifications.NoItemsFoundPanel',
    alias: 'widget.no-issues-found-panel',
    title: Uni.I18n.translate('issues.empty.title', 'ISU', 'No issues found'),
    reasons: [
        Uni.I18n.translate('workspace.issues.empty.list.item1', 'ISU', 'No issues have been defined yet.'),
        Uni.I18n.translate('workspace.issues.empty.list.item2', 'ISU', 'No issues comply to the filter.')
    ]
});

Ext.define('Isu.view.issues.SideFilter', {
    extend: 'Ext.form.Panel',
    alias: 'widget.issues-side-filter',
    cls: 'filter-form',
    width: 200,
    title: Uni.I18n.translate('general.title.filter', 'ISU', 'Filter'),
    ui: 'filter',
    requires: [
        'Isu.view.component.AssigneeCombo',
        'Uni.view.form.CheckboxGroup',
        'Uni.component.filter.view.Filter'
    ],
    defaults: {
        labelAlign: 'top'
    },

    items: [
        {
            itemId: 'filter-by-status',
            xtype: 'checkboxstore',
            store: 'Isu.store.IssueStatuses',
            name: 'status',
            fieldLabel: Uni.I18n.translate('general.title.status', 'ISU', 'Status'),
            columns: 1,
            vertical: true
        },
        {
            itemId: 'filter-by-assignee',
            xtype: 'issues-assignee-combo',
            name: 'assignee',
            fieldLabel: Uni.I18n.translate('general.title.assignee', 'ISU', 'Assignee'),
            forceSelection: true,
            anyMatch: true,
            emptyText: 'select an assignee',
            tooltipText: 'Start typing for assignee'
        },
        {
            itemId: 'filter-by-reason',
            xtype: 'combobox',
            name: 'reason',
            fieldLabel: Uni.I18n.translate('general.title.reason', 'ISU', 'Reason'),

            displayField: 'name',
            valueField: 'id',
            forceSelection: true,
            store: 'Isu.store.IssueReasons',

            listConfig: {
                cls: 'isu-combo-color-list',
                emptyText: 'No reason found'
            },

            queryMode: 'remote',
            queryParam: 'like',
            queryDelay: 100,
            queryCaching: false,
            minChars: 1,

            triggerAction: 'query',
            anchor: '100%',
            emptyText: 'select a reason',
            tooltipText: 'Start typing for reason'
        },
        {
            itemId: 'filter-by-meter',
            xtype: 'combobox',
            name: 'meter',
            fieldLabel: Uni.I18n.translate('general.title.meter', 'ISU', 'Meter'),

            displayField: 'name',
            valueField: 'name',
            forceSelection: true,
            store: 'Isu.store.Devices',

            listConfig: {
                cls: 'isu-combo-color-list',
                emptyText: 'No meter found'
            },

            queryMode: 'remote',
            queryParam: 'like',
            queryDelay: 100,
            queryCaching: false,
            minChars: 1,

            triggerAction: 'query',
            anchor: '100%',
            emptyText: 'select a MRID of the meter',
            tooltipText: 'Start typing for a MRID'
        }
    ],

    dockedItems: [
        {
            xtype: 'toolbar',
            dock: 'bottom',
            items: [
                {
                    itemId: 'issues-filter-apply',
                    ui: 'action',
                    text: Uni.I18n.translate('general.apply', 'ISU', 'Apply'),
                    action: 'applyFilter'
                },
                {
                    itemId: 'issues-filter-reset',
                    text: Uni.I18n.translate('general.clearAll', 'ISU', 'Clear all'),
                    action: 'resetFilter'
                }
            ]
        }
    ]
});

Ext.define('Isu.view.issues.SortingMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.issues-sorting-menu',
    shadow: false,
    border: false,
    plain: true,
    items: [
        {
            itemId: 'issues-sorting-menu-item-by-due-date',
            text: 'Due date',
            action: 'dueDate'
        }
    ]
});

Ext.define('Isu.view.issues.SortingToolbar', {
    extend: 'Uni.view.panel.FilterToolbar',
    requires: [
        'Uni.view.button.SortItemButton',
        'Isu.view.issues.SortingMenu'
    ],
    alias: 'widget.issues-sorting-toolbar',
    title: Uni.I18n.translate('general.sort', 'ISU', 'Sort'),
    emptyText: Uni.I18n.translate('general.none', 'ISU', 'None'),
    showClearButton: false,
    tools: [
        {
            itemId: 'addSort',
            xtype: 'button',
            action: 'addSort',
            text: 'Add sort',
            menu: {
                xtype: 'issues-sorting-menu',
                itemId: 'issues-sorting-menu'
            }
        }
    ],
    addSortButtons: function (sorting) {
        var me = this,
            container = me.getContainer(),
            menuItem,
            cls;

        container.removeAll();
        if (Ext.isArray(sorting)) {
            Ext.Array.each(sorting, function (sortItem) {
                if (sortItem.value) {
                    menuItem = me.down('#issues-sorting-menu [action=' + sortItem.type + ']');
                    cls = sortItem.value === Uni.component.sort.model.Sort.ASC
                        ? 'x-btn-sort-item-asc'
                        : 'x-btn-sort-item-desc';

                    menuItem.hide();
                    container.add({
                        xtype: 'sort-item-btn',
                        itemId: 'issues-sort-by-' + sortItem.type + '-button',
                        text: menuItem.text,
                        sortType: sortItem.type,
                        sortDirection: sortItem.value,
                        iconCls: cls,
                        listeners: {
                            closeclick: function () {
                                me.fireEvent('removeSort', this.sortType, this.sortDirection);
                            },
                            click: function () {
                                me.fireEvent('changeSortDirection', this.sortType, this.sortDirection);
                            }
                        }
                    });
                }
            });
        }
    }
});

