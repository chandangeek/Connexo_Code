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
            this.getStore('Isu.store.Clipboard').set('issuesCreationRuleState', Ext.create('Isu.model.CreationRule'));
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
            Ext.suspendLayouts();
            Ext.Array.each(records, function (record, index) {
                phasesRadioGroup.add({
                    boxLabel: record.get('title'),
                    name: 'phase',
                    inputValue: record.get('uuid'),
                    afterSubTpl: '<span style="color: #686868; font-style: italic">' + record.get('description') + '</span>',
                    checked: !index
                });
            });
            Ext.resumeLayouts();
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
        model.beginEdit();
        model.set('type', action.getData());
        delete model.get('type').parameters;
        model.set('phase', {
            uuid: phaseField.getValue().phase
        });
        Ext.Array.each(form.down('[name=actionTypeDetails]').query(), function (formItem) {
            if (formItem.isFormField) {
                if (!parameters[formItem.name])
                     parameters[formItem.name] = formItem.getValue() ? formItem.getValue() : "";
            }
        });
        model.set('parameters', parameters);
        model.endEdit();
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