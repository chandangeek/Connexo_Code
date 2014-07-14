Ext.define('Isu.controller.IssueCreationRulesEdit', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    stores: [
        'Isu.store.CreationRule',
        'Isu.store.IssueType',
        'Isu.store.CreationRuleTemplate',
        'Isu.store.DueinType',
        'Isu.store.Clipboard',
        'Isu.store.CreationRuleActionPhases'
    ],
    views: [
        'Isu.view.administration.datacollection.issuecreationrules.Edit',
        'Isu.view.workspace.issues.MessagePanel'
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
        'Isu.util.IsuGrid',
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
    },

    showCreate: function (id) {
        var widget = Ext.widget('issues-creation-rules-edit');

        this.setPage(id, 'create');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    showEdit: function (id) {
        var widget = Ext.widget('issues-creation-rules-edit');

        this.setPage(id, 'edit');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    setPage: function (id, action) {
        var self = this,
            ruleActionBtn = self.getRuleActionBtn(),
            clipboard = this.getStore('Isu.store.Clipboard'),
            savedData = clipboard.get('issuesCreationRuleState'),
            page = self.getPage(),
            prefix,
            btnTxt;

        switch (action) {
            case 'edit':
                prefix = 'Edit ';
                btnTxt = 'Save';
                if (savedData) {
                    self.ruleModel = savedData;
                    clipboard.clear('issuesCreationRuleState');
                    page.on('afterrender', function () {
                        self.modelToForm(self.ruleModel);
                    }, self, {single: true});
                } else {
                    self.getModel('Isu.model.CreationRule').load(id, {
                        success: function (record) {
                            self.ruleModel = record;
                            delete self.ruleModel.data.creationDate;
                            delete self.ruleModel.data.modificationDate;
                            if (page.isVisible()) {
                                self.modelToForm(record);
                            } else {
                                page.on('afterrender', function () {
                                    self.modelToForm(record);
                                }, self, {single: true});
                            }
                        }
                    });
                }
                break;
            case 'create':
                prefix = btnTxt = 'Create ';
                if (savedData) {
                    self.ruleModel = savedData;
                    clipboard.clear('issuesCreationRuleState');
                } else {
                    self.ruleModel = Isu.model.CreationRule.create();
                    delete self.ruleModel.data.id;
                    self.ruleModel.data.actions = [];
                }
                page.on('afterrender', function () {
                    self.modelToForm(self.ruleModel);
                }, self, {single: true});
                break;
        }

        self.getPageTitle().title = prefix + 'issue creation rule';
        ruleActionBtn.setText(btnTxt);
    },

    setDataToModel: function (data, model) {
        for (var field in data) {
            model.set(field, data[field]);
        }
    },

    modelToForm: function (record) {
        var self = this,
            form = self.getRuleForm(),
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
            page = self.getPage();

        if (record.get('template') && record.get('template').uid) {
            page.setLoading(true);
            self.on('templateloaded', function () {
                var formField,
                    name,
                    value;

                if (data.parameters) {
                    for (name in data.parameters) {
                        formField = templateDetails.down('[name=' + name + ']');
                        value = data.parameters[name];

                        formField.setValue(value);
                    }
                }

                page.setLoading(false);
            }, self, {single: true});
        }

        nameField.setValue(data.name);
        issueTypeField.getStore().load(function () {
            issueTypeField.setValue(data.issueType.uid || issueTypeField.getStore().getAt(0).get('uid'));
            templateField.getStore().on('load', function () {
                templateField.setValue(data.template.uid);
            }, self, {single: true});
        });
        reasonField.getStore().load(function () {
            reasonField.setValue(data.reason.id);
        });
        if (data.dueIn.number) {
            dueDateTrigger.setValue({dueDate: true});
            dueInNumberField.setValue(data.dueIn.number);
            dueInTypeField.setValue(data.dueIn.type || dueInTypeField.getStore().getAt(0).get('name'));
        } else {
            dueDateTrigger.setValue({dueDate: false});
        }
        commentField.setValue(data.comment);

        self.loadActionsToForm(data.actions);
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
            templateStoreProxy = templateStore.getProxy();

        if (newValue) {
            templateStoreProxy.setExtraParam('issuetype', newValue);
            templateField.reset();
            templateStore.load();
        }
    },

    setRuleTemplate: function (combo, newValue) {
        var self = this,
            templateDetails = self.getTemplateDetails(),
            templateModel = combo.getStore().model,
            formItem;

        templateDetails.removeAll();

        if (newValue) {
            templateModel.load(newValue, {
                success: function (template) {
                    var description = template.get('description'),
                        parameters = template.get('parameters');

                    self.addTemplateDescription(combo, description);

                    Ext.Array.each(parameters, function (obj) {
                        formItem = self.createControl(obj);
                        formItem && templateDetails.add(formItem);
                    });
                    self.fireEvent('templateloaded');
                }
            });
        }
    },

    addTemplateDescription: function (combo, descriptionText) {
        if (descriptionText) {
            if (!combo.templateDescriptionIcon) {
                combo.templateDescriptionIcon = Ext.DomHelper.append(this.getRuleForm().getEl(), {
                    tag: 'div',
                    cls: 'isu-icon-help-circled isu-creation-rule-template-description'
                }, true);
            }

            this.comboTemplateResize(combo);
            combo.templateDescriptionIcon.clearListeners();

            combo.templateDescriptionIcon.on('click', function () {
                combo.templateDescriptionWindow = Ext.Msg.show({
                    title: 'Template description',
                    msg: descriptionText,
                    buttons: Ext.MessageBox.CANCEL,
                    buttonText: {cancel: 'Close'},
                    modal: false,
                    animateTarget: combo.templateDescriptionIcon
                });
            });
        } else {
            this.removeTemplateDescription();
        }
    },

    comboTemplateResize: function (combo) {
        var comboEl = combo.getEl(),
            form = this.getRuleForm(),
            formEl = form.getEl();

        combo.templateDescriptionIcon && combo.templateDescriptionIcon.setStyle({
            top: comboEl.getY() - formEl.getY() + 'px',
            left: comboEl.getX() + comboEl.getWidth(false) - 65 + 'px'
        });
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
        var self = this,
            form = self.getRuleForm().getForm(),
            rule = self.formToModel(self.ruleModel),
            formErrorsPanel = self.getRuleForm().down('[name=form-errors]'),
            store = self.getStore('Isu.store.CreationRule'),
            templateCombo = self.getRuleForm().down('combobox[name=template]'),
            router = this.getController('Uni.controller.history.Router'),
            page = self.getPage();

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
                                messageText = Uni.I18n.translate('administration.issueCreationRules.createSuccess.msg', 'ISE', 'Issue creation rule created');
                                break;
                            case 'update':
                                messageText = Uni.I18n.translate('administration.issueCreationRules.updateSuccess.msg', 'ISE', 'Issue creation rule updated');
                                break;
                        }
                        self.getApplication().fireEvent('acknowledge', messageText);
                        router.getRoute('administration/issue/creationrules').forward();
                    } else {
                        json = Ext.decode(operation.response.responseText);
                        if (json && json.errors) {
                            form.markInvalid(json.errors);
                            formErrorsPanel.show();
                            self.comboTemplateResize(templateCombo);
                        }
                    }
                }
            });
        } else {
            formErrorsPanel.show();
            self.comboTemplateResize(templateCombo);
        }
    },

    addAction: function () {
        var router = this.getController('Uni.controller.history.Router'),
            rule = this.formToModel(this.ruleModel);

        this.getStore('Isu.store.Clipboard').set('issuesCreationRuleState', rule);

        router.getRoute('administration/issue/creationrules/create/addaction').forward();
    },

    loadActionsToForm: function (actions) {
        var actionsGrid = this.getActionsGrid(),
            actionsStore = actionsGrid.getStore(),
            noActionsText = this.getPage().down('[name=noactions]'),
            phasesStore = this.getStore('Isu.store.CreationRuleActionPhases');

        actionsStore.removeAll();

        if (actions.length) {
            phasesStore.load(function () {
                actionsStore.add(actions);
                actionsGrid.show();
                noActionsText.hide();
            });
        } else {
            actionsGrid.hide();
            noActionsText.show();
        }
    },

    loadActionsToModel: function (model) {
        var self = this,
            actionsGrid = this.getActionsGrid(),
            actionsStore = actionsGrid.getStore(),
            actions = [];

        actionsStore.each(function (action) {
            actions.push(action.getData());
        }, self);

        model.set('actions', actions);
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
    }
});
