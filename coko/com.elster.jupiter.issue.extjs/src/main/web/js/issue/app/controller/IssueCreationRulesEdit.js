Ext.define('Isu.controller.IssueCreationRulesEdit', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem',
//        'Isu.view.administration.datacollection.issuecreationrules.EditActionsMenu'
    ],

    stores: [
        'Isu.store.CreationRule',
        'Isu.store.IssueType',
        'Isu.store.CreationRuleTemplate',
        'Isu.store.DueinType'
    ],
    views: [
        'administration.datacollection.issuecreationrules.Edit'
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
            'issues-creation-rules-edit form [name=type]': {
                change: this.setRuleTemplateCombobox
            },
            'issues-creation-rules-edit form [name=template]': {
                change: this.setRuleTemplate
            },
            'issues-creation-rules-edit button[action=create]': {
                click: this.createRule
            },
            'issues-creation-rules-edit button[action=edit]': {
                click: this.editRule
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
                text: 'Data collection',
                href: 'datacollection'
            }),
            breadcrumbChild2 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Issue creation rules',
                href: 'issuecreationrules'
            });
        breadcrumbParent.setChild(breadcrumbChild1).setChild(breadcrumbChild2).setChild(this.lastBreadcrumbChild);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    setPage: function (id, action) {
        var self = this,
            ruleActionBtn = self.getRuleActionBtn(),
            prefix,
            btnTxt;

        self.ruleModel = Isu.model.CreationRule.create();

        switch (action) {
            case 'edit':
                prefix = 'Edit ';
                btnTxt = 'Save';
                self.ruleModel.load(id, {
                    success: function (record) {
                        self.modelToForm(record);
                    }
                });
                break;
            case 'create':
                prefix = btnTxt = 'Create ';
                self.modelToForm(self.ruleModel);
                break;
        }

        this.lastBreadcrumbChild = Ext.create('Uni.model.BreadcrumbItem', {
            text: prefix + 'issue creation rules'
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
            typeField = form.down('[name=type]'),
            templateField = form.down('[name=template]'),
            dueinNumberField = form.down('[name=duein.number]'),
            dueinTypeField = form.down('[name=duein.type]'),
            commentField = form.down('[name=comment]');

        nameField.setValue(data.name);
        typeField.getStore().load(function (store) {
            if (data.type.id) {
                typeField.setValue(data.type.id);
            } else {
                typeField.setValue(typeField.getStore().getAt(0).get('id'));
            }
            templateField.getStore().on('load', function () {
                templateField.setValue(data.template.uid);
            }, self, {single:true});
        });
        dueinNumberField.setValue(data.duein.number);
        dueinTypeField.setValue(data.duein.type);
        commentField.setValue(data.comment);
    },

    formToModel: function (model) {
        var form = this.getRuleForm(),
            ruleModel = model || Isu.model.CreationRule.create(),
            nameField = form.down('[name=name]'),
            typeField = form.down('[name=type]'),
            templateField = form.down('[name=template]'),
            dueinNumberField = form.down('[name=duein.number]'),
            dueinTypeField = form.down('[name=duein.type]'),
            commentField = form.down('[name=comment]'),
            templateDetails = this.getTemplateDetails(),
            parameters = {};

        ruleModel.set('name', nameField.getValue());
        ruleModel.set('type', {
            id: typeField.getValue()
        });
        ruleModel.set('template', {
            uid: templateField.getValue()
        });
        ruleModel.set('duein', {
            number: dueinNumberField.getValue() || '',
            type: dueinTypeField.getValue() || ''
        });
        ruleModel.set('comment', commentField.getValue());

        Ext.Array.each(templateDetails.items, function (item) {
            var formItem = item.down('[formBind=true]'),
                value = formItem.getValue();

            if (value) {
                parameters[formItem.name] = value;
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
                    template.description && templateDetails.add({
                        xtype: 'component',
                        html: template.description,
                        margin: '5 0 0 155',
                        cls: 'isu-creation-rule-description'
                    });

                    for (var fieldName in template.parameters) {
                        switch (template.parameters[fieldName].type) {
                            case 'number':
                                formItem = self.createTextField(template.parameters[fieldName], fieldName);
                                break;
                        }
                        templateDetails.add(formItem);
                    }
                }
            });
        }
    },

    createTextField: function (obj, name) {
        var formItem = {
            xtype: 'textfield',
            name: name,
            fieldLabel: obj.label + (!obj.optional ? '*' : ''),
            allowBlank: obj.optional,
            formBind: true
        };

        if (this.ruleModel.parameters[name]) {
            formItem.value = this.ruleModel.parameters[name];
            delete this.ruleModel.parameters[name];
        }

        return {
            xtype: 'container',
            layout: {
                type: 'hbox'
            },
            items: [
                formItem,
                {
                    xtype: 'component',
                    html: obj.suffix
                }
            ]
        }
    },

    createRule: function () {
        ruleAction('create');
    },

    editRule: function () {
        ruleAction('edit');
    },

    ruleAction: function (type) {
        var form = this.getRuleForm().getForm(),
            rule = this.formToModel(),
            formErrorsPanel = this.getRuleForm().down('[name=form-errors]'),
            store = this.getStore('Isu.store.CreationRule');

        if (form.isValid()) {
            formErrorsPanel.hide();
            store.on('datachanged', function () {
                window.location.href = '#/issue-administration/datacollection/issuecreationrules';
            }, this, {single: true});
            switch (type) {
                case 'edit':
                    rule.commit();
                    break;
                case 'create':
                    rule.save();
                    break;
            }
            rule.commit();
        } else {
            formErrorsPanel.show();
        }
    }
});
