Ext.define('Isu.controller.CreationRuleEdit', {
    extend: 'Ext.app.Controller',

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
        'Isu.model.CreationRuleAction',
        'Isu.model.CreationRule'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'issues-creation-rules-edit'
        },
        {
            ref: 'ruleForm',
            selector: 'issues-creation-rules-edit form'
        },
        {
            ref: 'actionsGrid',
            selector: 'issues-creation-rules-edit issues-creation-rules-actions-list'
        }
    ],

    init: function () {
        this.control({
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

    showEdit: function (id) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            clipboard = this.getStore('Isu.store.Clipboard'),
            savedData = clipboard.get('issuesCreationRuleState'),
            widget = Ext.widget('issues-creation-rules-edit', {
                router: router,
                isEdit: !!id
            }),
            issueTypesStore = me.getStore('Isu.store.IssueTypes'),
            dependencesCounter = 3,
            dependenciesOnLoad = function () {
                dependencesCounter--;
                if (!dependencesCounter) {
                    if (widget.rendered) {
                        widget.setLoading(false);
                        widget.down('form').loadRecord(rule);
                    }
                }
            },
            rule;

        this.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading();

        if (savedData) {
            rule = savedData;
            clipboard.clear('issuesCreationRuleState');
            me.loadDependencies(rule.getIssueType(), dependenciesOnLoad);
        } else {
            if (id) {
                dependencesCounter++;
                me.getModel('Isu.model.CreationRule').load(id, {
                    success: function (record) {
                        rule = record;
                        me.getApplication().fireEvent('issueCreationRuleEdit', rule);
                        dependenciesOnLoad();
                        me.loadDependencies(record.getIssueType(), dependenciesOnLoad);
                    }
                });
            } else {
                rule = Ext.create('Isu.model.CreationRule');
                issueTypesStore.on('load', function (store, types) {
                    rule.setIssueType(types[0]);
                    me.loadDependencies(types[0], dependenciesOnLoad);
                }, me, {single: true});
            }
        }

        issueTypesStore.load(dependenciesOnLoad);
    },

    loadDependencies: function (record, callback) {
        var me = this,
            templatesStore = me.getStore('Isu.store.CreationRuleTemplates'),
            issueReasonsStore = me.getStore('Isu.store.IssueReasons');

        templatesStore.getProxy().setExtraParam('issueType', record.getId());
        templatesStore.load(callback);
        issueReasonsStore.getProxy().setExtraParam('issueType', record.getId());
        issueReasonsStore.load(callback);
    },

    ruleSave: function () {
        var me = this,
            form = me.getRuleForm(),
            basicForm = form.getForm(),
            formErrorsPanel = me.getRuleForm().down('[name=form-errors]'),
            page = me.getPage();

        basicForm.clearInvalid();
        formErrorsPanel.hide();
        page.setLoading();
        form.updateRecord();
        form.getRecord().save({
            callback: function (record, operation, success) {
                var messageText,
                    json;

                page.setLoading(false);
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
                    me.getController('Uni.controller.history.Router').getRoute('administration/creationrules').forward();
                } else {
                    json = Ext.decode(operation.response.responseText, true);
                    if (json && json.errors) {
                        basicForm.markInvalid(json.errors);
                        formErrorsPanel.show();
                    }
                }
            }
        });
    },

    addAction: function () {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            form = me.getRuleForm();

        form.updateRecord();
        me.getStore('Isu.store.Clipboard').set('issuesCreationRuleState', form.getRecord());

        router.getRoute(router.currentRoute + '/addaction').forward();
    },

    chooseActionOperation: function (menu, item) {
        var actionId = menu.record.getId();

        switch (item.action) {
            case 'delete':
                this.deleteAction(actionId);
                break;
        }
    },

    deleteAction: function (id) {
        var me = this,
            actionsGrid = me.getActionsGrid(),
            actionsStore = actionsGrid.getStore(),
            noActionsText = me.getPage().down('[name=noactions]');

        actionsStore.remove(actionsStore.getById(id));

        if (!actionsStore.getCount()) {
            actionsGrid.hide();
            noActionsText.show();
        }
    }
});