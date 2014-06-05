Ext.define('Isu.controller.IssueCreationRulesActionsEdit', {
    extend: 'Ext.app.Controller',

    stores: [
        'Isu.store.Actions',
        'Isu.store.CreationRuleActionPhases',
        'Isu.store.Clipboard'
    ],

    views: [
        'Isu.view.administration.datacollection.issuecreationrules.EditAction'
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
        var widget = Ext.widget('issues-creation-rules-edit-action');

        Ext.util.History.on('change', this.checkRoute, this);

        this.setPage(id, 'create');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    checkRoute: function (token) {
        var clipboard = this.getStore('Isu.store.Clipboard'),
            createRegexp = /administration\/issue\/creationrules\/create/,
            editRegexp = /administration\/issue\/creationrules\/\d+\/edit/;

        Ext.util.History.un('change', this.checkRoute, this);

        if (token.search(createRegexp) == -1 && token.search(editRegexp) == -1) {
            clipboard.clear('issuesCreationRuleState');
        }
    },

    setPage: function (id, action) {
        var self = this,
            actionTypesStore = self.getStore('Isu.store.Actions'),
            actionTypesPhases = self.getStore('Isu.store.CreationRuleActionPhases'),
            loadedStoresCount = 0,
            prefix,
            btnTxt;

        var checkLoadedStores = function () {
            loadedStoresCount++;

            if (loadedStoresCount == 2) {
                switch (action) {
                    case 'create':
                        prefix = btnTxt = 'Add ';
                        self.actionModel = {};
                        break;
                }

                self.getPageTitle().setTitle(prefix + 'action');
                self.getActionOperationBtn().setText(btnTxt);
            }
        };

        actionTypesStore.load(checkLoadedStores);
        actionTypesPhases.load(function (records) {
            var phasesRadioGroup = self.getPhasesRadioGroup();

            Ext.Array.each(records, function (record, index) {
                phasesRadioGroup.add({
                    boxLabel: record.get('title'),
                    name: 'phase',
                    inputValue: record.get('uuid'),
                    afterSubTpl: record.get('description'),
                    checked: !index
                });
            });
            checkLoadedStores();
        });
    },

    formToModel: function (model) {
        var form = this.getActionForm(),
            phaseField = form.down('[name=phasesRadioGroup]'),
            actionStore = this.getStore('Isu.store.Actions'),
            actionField = form.down('[name=actionType]'),
            action = actionStore.getById(actionField.getValue()),
            parameters = {};

        model.type = action.getData();
        delete model.type.parameters;
        model.phase = {
            uuid: phaseField.getValue().phase
        };
        Ext.Array.each(form.down('[name=actionTypeDetails]').query(), function (formItem) {
            if (formItem.isFormField) {
                parameters[formItem.name] = formItem.getValue();
            }
        });
        model.parameters = parameters;


        return model;
    },

    setActionTypeDetails: function (combo, newValue) {
        var self = this,
            actionTypesStore = self.getStore('Isu.store.Actions'),
            parameters = actionTypesStore.getById(newValue).get('parameters'),
            actionTypeDetails = self.getActionTypeDetails();

        actionTypeDetails.removeAll();

        Ext.Object.each(parameters, function(key, value) {
            var formItem = self.createControl(value);

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
                actions = rule.get('actions');

                formErrorsPanel.hide();

                if (Ext.isArray(actions)) {
                    actions.push(newAction);
                } else {
                    actions = [];
                    actions.push(newAction);
                }

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
                router.getRoute('administration/issue/creationrules/edit').forward({id: rule.getId()});
            } else {
                router.getRoute('administration/issue/creationrules/create').forward();
            }
        } else {
            router.getRoute('administration/issue/creationrules').forward();
        }
    }
});