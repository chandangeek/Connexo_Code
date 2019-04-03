Ext.define('Itk.controller.CreationRuleEdit', {
    extend: 'Ext.app.Controller',

    stores: [
        'Itk.store.CreationRules',
        'Itk.store.CreationRuleTemplates',
        'Itk.store.DueinTypes',
        'Itk.store.Clipboard',
        'Itk.store.CreationRuleActionPhases',
        'Itk.store.IssueReasons'
    ],
    views: [
        'Itk.view.creationrules.Edit'
    ],

    models: [
        'Itk.model.CreationRuleAction',
        'Itk.model.CreationRule'
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
        },
        {
            ref: 'raisedEventTypesGrid',
            selector: '#raisedEventTypesGridPanel'
        },
        {
            ref: 'clearedEventTypesGrid',
            selector: '#clearedEventTypesGridPanel'
        },
        {
            ref: 'eventTypeWindow',
            selector: '#eventTypeWindow'
        },
        {
            ref: 'noRaisedEventTypesLabel',
            selector: '#raisedNoEventTypesLabel'
        },
        {
            ref: 'noClearedEventTypesLabel',
            selector: '#clearedNoEventTypesLabel'
        }
    ],

    comboBoxValueForAll: -1,

    init: function () {
        this.control({
            'issues-creation-rules-edit button[action=save]': {
                click: this.ruleSave
            },
            'issues-creation-rules-edit button[action=addAction]': {
                click: this.addAction
            }
        });
    },

    showEdit: function (id) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            clipboard = this.getStore('Itk.store.Clipboard'),
            savedData = clipboard.get('issuesCreationRuleState'),
            widget = Ext.widget('issues-creation-rules-edit', {
                router: router,
                isEdit: !!id
            }),
            dependenciesOnLoad = function () {
                if (widget.rendered) {
                    widget.setLoading(false);
                    widget.down('form').loadRecord(rule);
                }
            },
            rule;

        this.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading();

        if (savedData) {
            rule = savedData;
            clipboard.clear('issuesCreationRuleState');
            me.loadDependencies(dependenciesOnLoad);
        } else {
            if (id) {
                me.getModel('Itk.model.CreationRule').load(id, {
                    success: function (record) {
                        rule = record;
                        me.getApplication().fireEvent('issueCreationRuleEdit', rule);
                        me.loadDependencies(dependenciesOnLoad);
                    }
                });
            } else {
                rule = Ext.create('Itk.model.CreationRule');
                me.loadDependencies(dependenciesOnLoad);
            }
        }
    },

    loadDependencies: function (callback) {
        var me = this,
            templatesStore = me.getStore('Itk.store.CreationRuleTemplates'),
            issueReasonsStore = me.getStore('Itk.store.IssueReasons');

        templatesStore.getProxy();
        templatesStore.load(callback);
        issueReasonsStore.load();
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
            backUrl: me.getController('Uni.controller.history.Router').getRoute('administration/issuecreationrules').buildUrl(),
            callback: function (record, operation, success) {
                var messageText,
                    json;

                page.setLoading(false);
                if (success) {
                    switch (operation.action) {
                        case 'create':
                            messageText = Uni.I18n.translate('administration.issueCreationRules.createSuccess.msg', 'ITK', 'Issue creation rule added');
                            break;
                        case 'update':
                            messageText = Uni.I18n.translate('administration.issueCreationRules.updateSuccess.msg', 'ITK', 'Issue creation rule updated');
                            break;
                    }
                    me.getApplication().fireEvent('acknowledge', messageText);
                    me.getController('Uni.controller.history.Router').getRoute('administration/issuecreationrules').forward();
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
        me.getStore('Itk.store.Clipboard').set('issuesCreationRuleState', form.getRecord());

        router.getRoute(router.currentRoute + '/addaction').forward();
    }
});