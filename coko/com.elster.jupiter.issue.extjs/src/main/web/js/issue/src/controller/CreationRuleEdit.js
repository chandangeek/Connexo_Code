/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.controller.CreationRuleEdit', {
    extend: 'Ext.app.Controller',

    stores: [
        'Isu.store.CreationRules',
        'Isu.store.IssueTypes',
        'Isu.store.CreationRuleTemplates',
        'Isu.store.DueinTypes',
        'Isu.store.Clipboard',
        'Isu.store.CreationRuleActionPhases',
        'Isu.store.CreationRuleReasons',
        'Isu.store.CreationRuleIssueTypes'
    ],

    views: [
        'Isu.view.creationrules.Edit',
        'Isu.view.creationrules.EditAction',
        'Isu.view.creationrules.EditActionForm'
    ],

    models: [
        'Isu.model.CreationRuleAction',
        'Isu.model.CreationRule',
        'Isu.model.FilteredDeviceGroup',
        'Isu.model.ExcludedDeviceGroup'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'issues-creation-rules-edit'
        },
        {
            ref: 'actionPage',
            selector: 'issues-creation-rules-edit-action-form'
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
            ref: 'excludeDeviceGroupsGrid',
            selector: 'issues-creation-rules-edit issues-creation-rules-excl-device-groups-list'
        }
    ],

    editActionRecord: null,

    init: function () {
        this.control({
            'issues-creation-rules-edit button[action=save]': {
                click: this.ruleSave
            },
            'issues-creation-rules-edit button[action=addAction]': {
                click: this.addAction
            },
            'issues-creation-rules-edit button[action=excludeDeviceGroup]': {
                click: this.excludeDeviceGroup
            },
            'creation-rule-action-list-menu': {
                click: this.chooseActionListMenu
            },
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
            issueTypesStore = me.getStore('Isu.store.CreationRuleIssueTypes'),
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
            if (me.editActionRecord && clipboard.get('creationRuleActionState')) {
                clipboard.clear('creationRuleActionState');
                savedData.actionsStore.add(me.editActionRecord);
            }
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
            issueReasonsStore = me.getStore('Isu.store.CreationRuleReasons');

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
            backUrl: me.getController('Uni.controller.history.Router').getRoute('administration/creationrules').buildUrl(),
            callback: function (record, operation, success) {
                var messageText,
                    json;

                page.setLoading(false);
                if (success && form.validateIssueReason()) {
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
            addRuleActionRoute = router.currentRoute + '/addaction',
            route,
            form = me.getRuleForm(),
            clipboard = me.getStore('Isu.store.Clipboard');
        clipboard.set('issuesCreationRuleState', form.getRecord());
        if (!clipboard.get('creationRuleActionState')) {
            clipboard.clear("creationRuleActionEdit", false);
        }
        me.actionArray = [];
        form.updateRecord();
        route = router.getRoute(addRuleActionRoute);
        if (clipboard.get('creationRuleActionState')) {
            route.setTitle(Uni.I18n.translate('dataExport.editDestination', 'ISU', 'Edit Action'));
        } else {
            route.setTitle(Uni.I18n.translate('dataExport.addDestination', 'ISU', 'Add Action'));
        }
        route.forward();
    },

    excludeDeviceGroup: function () {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            form = me.getRuleForm(),
            excludedGroups = me.getExcludeDeviceGroupsGrid().getStore();
        form.updateRecord();
        me.getStore('Isu.store.Clipboard').set('issuesCreationRuleState', form.getRecord());
        router.getRoute(router.currentRoute + '/addexclgroups').forward();
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
                var grid = page.down("#issues-creation-rules-actions-grid");
                Ext.suspendLayouts();
                if (grid.getStore()) {
                    grid.getStore().remove(menu.record);
                } else {
                    grid.remove(menu.record);
                }
                if (grid.getStore().count() == 0) {
                    grid.hide();
                    page.down('#issues-creation-rule-no-actions').show()
                }
                Ext.resumeLayouts(true);
                break;
            case 'edit':
                me.editActionRecord = menu.record;
                var page = me.getPage(),
                    grid = page.down("#issues-creation-rules-actions-grid"),
                    clipboard = this.getStore('Isu.store.Clipboard');
                clipboard.set('issuesCreationRuleState', me.getRuleForm().getRecord());
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