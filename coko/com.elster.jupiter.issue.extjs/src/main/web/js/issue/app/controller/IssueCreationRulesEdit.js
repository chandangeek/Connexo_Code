Ext.define('Isu.controller.IssueCreationRulesEdit', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem'
    ],

    stores: [
        'Isu.store.CreationRule',
        'Isu.store.IssueType',
        'Isu.store.CreationRuleTemplate',
        'Isu.store.DueinType'
    ],
    views: [
        'Isu.view.administration.datacollection.issuecreationrules.Edit',
        'Isu.view.workspace.issues.MessagePanel'
    ],

    refs: [
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
            ref: 'breadcrumbs',
            selectior: 'issues-creation-rules-edit breadcrumbTrail'
        }
    ],

    mixins: [
        'Isu.util.IsuGrid'
    ],

    init: function () {
        this.control({
            'issues-creation-rules-edit breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            },
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
            'issues-creation-rules-edit button[action=create]': {
                click: this.ruleSave
            },
            'issues-creation-rules-edit button[action=edit]': {
                click: this.ruleSave
            }
        });

        this.actionMenuXtype = 'issues-creation-rules-edit-actions-menu';
    },

    showOverview: function (id, action) {
        var widget = Ext.widget('issues-creation-rules-edit');
        this.setPage(id, action);
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    setBreadcrumb: function (breadcrumbs) {
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Administration',
                href: '#/issue-administration'
            }),
            breadcrumbChild1 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Issue creation rules',
                href: 'issuecreationrules'
            });
        breadcrumbParent.setChild(breadcrumbChild1).setChild(this.lastBreadcrumbChild);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    setPage: function (id, action) {
        var self = this,
            ruleActionBtn = self.getRuleActionBtn(),
            prefix,
            btnTxt;

        switch (action) {
            case 'edit':
                prefix = 'Edit ';
                btnTxt = 'Save';
                self.getModel('Isu.model.CreationRule').load(id, {
                    success: function (record) {
                        self.ruleModel = record;
                        delete self.ruleModel.data.creationDate;
                        delete self.ruleModel.data.modificationDate;
                        self.modelToForm(record);
                    }
                });
                break;
            case 'create':
                prefix = btnTxt = 'Create ';
                self.ruleModel = Isu.model.CreationRule.create();
                delete self.ruleModel.data.id;
                self.ruleModel.data.actions = [];
                self.modelToForm(self.ruleModel);
                break;
        }

        this.lastBreadcrumbChild = Ext.create('Uni.model.BreadcrumbItem', {
            text: prefix + 'issue creation rule'
        });
        self.getPageTitle().update('<h1>' + prefix + 'issue creation rule</h1>');
        ruleActionBtn.action = action;
        ruleActionBtn.setText(btnTxt);
    },

    modelToForm: function (record) {
        var self = this,
            form = self.getRuleForm(),
            data = record.getData(),
            nameField = form.down('[name=name]'),
            issueTypeField = form.down('[name=issueType]'),
            templateField = form.down('[name=template]'),
            reasonField = form.down('[name=reason]'),
            dueInNumberField = form.down('[name=dueIn.number]'),
            dueInTypeField = form.down('[name=dueIn.type]'),
            commentField = form.down('[name=comment]');

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
        dueInNumberField.setValue(data.dueIn.number || null);
        dueInTypeField.setValue(data.dueIn.type || dueInTypeField.getStore().getAt(0).get('name'));
        commentField.setValue(data.comment);
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

        Ext.Array.each(templateDetails.query('[formBind=false]'), function (item) {
            var value = item.getValue();

            if (value) {
                parameters[item.name] = value;
            }
        });

        ruleModel.set('parameters', parameters);

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

                    for (var fieldName in parameters) {
                        switch (parameters[fieldName].type) {
                            case 'number':
                                formItem = self.createTextField(parameters[fieldName], fieldName);
                                break;
                        }
                        templateDetails.add(formItem);
                    }
                }
            });
        }
    },

    addTemplateDescription: function (combo, descriptionText) {
        if (descriptionText) {
            var comboEl = combo.getEl();

            combo.templateDescriptionIcon = Ext.DomHelper.append(Ext.getBody(), {
                tag: 'div',
                cls: 'isu-icon-help-circled isu-creation-rule-template-description'
            }, true);

            this.comboTemplateResize(combo);

            combo.templateDescriptionIcon.on('click', function () {
                Ext.Msg.show({
                    title:'Template description',
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
        var comboEl = combo.getEl();

        combo.templateDescriptionIcon && combo.templateDescriptionIcon.setStyle({
            top: comboEl.getY() + 'px',
            left: comboEl.getX() + comboEl.getWidth(false) + 'px'
        });
    },

    removeTemplateDescription: function () {
        var combo = Ext.ComponentQuery.query('issues-creation-rules-edit form [name=template]')[0];

        if (combo && combo.templateDescriptionIcon) {
            combo.templateDescriptionIcon.destroy();
            delete combo.templateDescriptionIcon;
        }
    },

    createTextField: function (obj, name) {
        var formItem = {
            xtype: 'textfield',
            name: name,
            fieldLabel: obj.label,
            allowBlank: obj.optional,
            formBind: false,
            labelSeparator: !obj.optional ? ' *' : ''
        };

        if (this.ruleModel.get('parameters')[name]) {
            formItem.value = this.ruleModel.get('parameters')[name];
            delete this.ruleModel.get('parameters')[name];
        }

        return formItem;
    },

    ruleSave: function (button) {
        var self = this,
            form = self.getRuleForm().getForm(),
            rule = self.formToModel(self.ruleModel),
            formErrorsPanel = self.getRuleForm().down('[name=form-errors]'),
            store = self.getStore('Isu.store.CreationRule');

        if (form.isValid()) {
            button.setDisabled(true);
            formErrorsPanel.hide();
            rule.save({
                callback: function (model, operation, success) {
                    var messageText,
                        json;

                    button.setDisabled(false);

                    if (success) {
                        switch (operation.action) {
                            case 'create':
                                messageText = 'Issue creation rule created';
                                break;
                            case 'update':
                                messageText = 'Issue creation rule updated';
                                break;
                        }
                        self.getApplication().fireEvent('isushowmsg', {
                            type: 'notify',
                            msgBody: [
                                {
                                    style: 'msgHeaderStyle',
                                    text: messageText
                                }
                            ],
                            y: 10,
                            showTime: 5000
                        });
                        window.location.href = '#/issue-administration/issuecreationrules'
                    } else {
                        json = Ext.decode(operation.response.responseText);
                        if (json && json.errors) {
                            form.markInvalid(json.errors);
                            formErrorsPanel.show();
                        }
                    }
                }
            });
        } else {
            formErrorsPanel.show();
        }
    }
});
