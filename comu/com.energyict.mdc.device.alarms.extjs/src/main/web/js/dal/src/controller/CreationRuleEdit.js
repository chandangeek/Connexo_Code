Ext.define('Dal.controller.CreationRuleEdit', {
    extend: 'Ext.app.Controller',

    stores: [
        'Dal.store.CreationRules',
        'Dal.store.CreationRuleTemplates',
        'Dal.store.DueinTypes',
        'Dal.store.Clipboard',
        'Dal.store.CreationRuleActionPhases',
        'Dal.store.AlarmReasons'
    ],
    views: [
        'Dal.view.creationrules.Edit'
    ],

    models: [
        'Dal.model.CreationRuleAction',
        'Dal.model.CreationRule'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'alarms-creation-rules-edit'
        },
        {
            ref: 'ruleForm',
            selector: 'alarms-creation-rules-edit form'
        },
        {
            ref: 'actionsGrid',
            selector: 'alarms-creation-rules-edit alarms-creation-rules-actions-list'
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
    editActionRecord: null,

    init: function () {
        this.control({
            'alarms-creation-rules-edit button[action=save]': {
                click: this.ruleSave
            },
            'alarms-creation-rules-edit button[action=addAction]': {
                click: this.addAction
            },
            'alarms-creation-rule-action-list-menu': {
                click: this.chooseActionListMenu
            },
        });
    },

    showEdit: function (id) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            clipboard = this.getStore('Dal.store.Clipboard'),
            savedData = clipboard.get('alarmsCreationRuleState'),
            widget = Ext.widget('alarms-creation-rules-edit', {
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
            clipboard.clear('alarmsCreationRuleState');
            if (me.editActionRecord && clipboard.get('creationRuleActionState')) {
                savedData.actionsStore.add(me.editActionRecord);
                clipboard.clear('creationRuleActionState');
            }
            me.loadDependencies(dependenciesOnLoad);
        } else {
            if (id) {
                me.getModel('Dal.model.CreationRule').load(id, {
                    success: function (record) {
                        rule = record;
                        me.getApplication().fireEvent('alarmCreationRuleEdit', rule);
                        me.loadDependencies(dependenciesOnLoad);
                    }
                });
            } else {
                rule = Ext.create('Dal.model.CreationRule');
                me.loadDependencies(dependenciesOnLoad);
            }
        }
    },

    loadDependencies: function (callback) {
        var me = this,
            templatesStore = me.getStore('Dal.store.CreationRuleTemplates'),
            alarmReasonsStore = me.getStore('Dal.store.AlarmReasons');

        templatesStore.getProxy();
        templatesStore.load(callback);
        alarmReasonsStore.load();
    },

    ruleSave: function () {
        var me = this,
            form = me.getRuleForm(),
            basicForm = form.getForm(),
            formErrorsPanel = me.getRuleForm().down('[name=form-errors]'),
            page = me.getPage();

        if (basicForm.isValid()) {
            basicForm.clearInvalid();
            formErrorsPanel.hide();
            page.setLoading();
            form.updateRecord();
            form.getRecord().save({
                backUrl: me.getController('Uni.controller.history.Router').getRoute('administration/alarmcreationrules').buildUrl(),
                callback: function (record, operation, success) {
                    var messageText,
                        json;

                    page.setLoading(false);
                    if (success) {
                        switch (operation.action) {
                            case 'create':
                                messageText = Uni.I18n.translate('administration.alarmCreationRules.createSuccess.msg', 'DAL', 'Alarm creation rule added');
                                break;
                            case 'update':
                                messageText = Uni.I18n.translate('administration.alarmCreationRules.updateSuccess.msg', 'DAL', 'Alarm creation rule updated');
                                break;
                        }
                        me.getApplication().fireEvent('acknowledge', messageText);
                        me.getController('Uni.controller.history.Router').getRoute('administration/alarmcreationrules').forward();
                    } else {
                        json = Ext.decode(operation.response.responseText, true);
                        if (json && json.errors) {
                            basicForm.markInvalid(json.errors);
                            formErrorsPanel.show();
                        }
                    }
                }
            });
        } else {
            formErrorsPanel.show();
        }
    },

    addAction: function () {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            addRuleActionRoute = router.currentRoute + '/addaction',
            route,
            form = me.getRuleForm(),
            clipboard = me.getStore('Dal.store.Clipboard');
        if (!clipboard.get('creationRuleActionState')) {
            clipboard.set("creationRuleActionEdit", false);
        }
        me.actionArray = [];
        form.updateRecord();
        clipboard.set('alarmsCreationRuleState', form.getRecord());
        route = router.getRoute(addRuleActionRoute);
        if (clipboard.get('creationRuleActionState')) {
            route.setTitle(Uni.I18n.translate('dataExport.editDestination', 'DAL', 'Edit Action'));
        } else {
            route.setTitle(Uni.I18n.translate('dataExport.addDestination', 'DAL', 'Add Action'));
        }
        route.forward();
    },

    chooseActionListMenu: function (menu, item) {
        var me = this;
        var action = item.action;
        var id = menu.record.getId();
        var router = this.getController('Uni.controller.history.Router');

        switch (action) {
            case 'remove':
                var me = this,
                    page = this.getPage();
                var grid = page.down("#alarms-creation-rules-actions-grid");
                Ext.suspendLayouts();
                if (grid.getStore()) {
                    grid.getStore().remove(menu.record);
                } else {
                    grid.remove(menu.record);
                }
                if (grid.getStore().count() == 0) {
                    grid.hide();
                    page.down('#alarms-creation-rule-no-actions').show()
                }
                Ext.resumeLayouts(true);
                break;
            case 'edit':
                me.editActionRecord = menu.record;
                var grid = me.getActionsGrid(),
                    clipboard = this.getStore('Dal.store.Clipboard');
                if (grid.getStore()) {
                    grid.getStore().remove(menu.record);
                    clipboard.set("creationRuleActionState", menu.record);
                    clipboard.set("creationRuleActionEdit", true);
                } else {
                    grid.remove(menu.record);
                }
                me.addAction();
                //router.getRoute(router.currentRoute + '/addaction').forward();
                break;
        }
    },
});