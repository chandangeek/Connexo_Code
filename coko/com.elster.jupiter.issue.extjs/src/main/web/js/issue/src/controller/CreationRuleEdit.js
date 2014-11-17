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
                title = Uni.I18n.translate('administration.issueCreationRules.title.editIssueCreationRule', 'ISU', 'Edit issue creation rule');
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