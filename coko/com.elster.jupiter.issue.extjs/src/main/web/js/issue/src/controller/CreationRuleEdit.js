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
        'Isu.store.DeviceGroups'
    ],
    
    views: [
        'Isu.view.creationrules.Edit',
        'Isu.view.creationrules.ExcludeDeviceGroupsWindow'
    ],
    
    models: [
        'Isu.model.CreationRuleAction',
        'Isu.model.CreationRule',
        'Isu.model.DeviceGroup',
        'Isu.model.ExcludedDeviceGroup'
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
            ref: 'excludeDeviceGroupsGrid',
            selector: 'issues-creation-rules-edit issues-creation-rules-excl-device-groups-list'
        },
        {
            ref: 'excludeDeviceGroupsWindow',
            selector: 'issues-creation-rules-exclude-device-groups-window'
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
            'issues-creation-rules-edit button[action=excludeDeviceGroup]': {
                click: this.excludeDeviceGroup
            },
            'issues-creation-rules-exclude-device-groups-window button[action=saveGroupExclusions]': {
                click: this.saveSelectedExclusionsAction
            },
            'issues-creation-rules-exclude-device-groups-window button[action=cancelGroupExclusions]': {
                click: this.cancelSelectedExclusionsAction
            }
        });
        this.listen({
            store: {
                '#Isu.store.DeviceGroups': {
                    load: this.onDeviceGroupsStoreLoad
                }
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
    
    excludeDeviceGroup: function () {
        var me = this,
            deviceGroups = me.getStore('Isu.store.DeviceGroups');
        
        var selectGroupsWindow = Ext.widget('issues-creation-rules-exclude-device-groups-window');
        selectGroupsWindow.show();
        selectGroupsWindow.setLoading(true);
        deviceGroups.load(function (records, operation, success) {
            if (success == true) {
                selectGroupsWindow.setLoading(false);
            }
        });
    },

    onDeviceGroupsStoreLoad: function (store, records, successful, eOpts) {
    
        var me = this,
            form = me.getRuleForm(),
            excludedGroups = form.getRecord().exclGroups();
        
        if (successful == true) {
            Ext.each(records, function (devGroup) {
                var record = excludedGroups.findRecord('deviceGroupId', devGroup.get('id'), 0, false, true, true);
                if (record) {
                    devGroup.set('selected', true);
                } else {
                    devGroup.set('selected', false);
                }
                devGroup.commit();
            });
        }
    },
    
    cancelSelectedExclusionsAction: function () {
        var me = this;
        me.getExcludeDeviceGroupsWindow().close();
    },

    saveSelectedExclusionsAction: function () {
        var me = this,
            form = me.getRuleForm(),
            excludedGroupsStore = form.getRecord().exclGroups(),
            window = me.getExcludeDeviceGroupsWindow(),
            selectedGroupsGrid = window.down('isu-device-groups-selection-grid');
            
        window.setLoading(true);
        Ext.each(selectedGroupsGrid.getSelectionModel().getSelection(), function (selectedGroup) {
            if(selectedGroup.get('selected') == false) {
                excludedGroupsStore.add(Ext.create('Isu.model.ExcludedDeviceGroup', 
                    {
                        deviceGroupId: selectedGroup.get('id'),
                        deviceGroupName: selectedGroup.get('name'),
                        isGroupDynamic: selectedGroup.get('dynamic')
                    }
                ));
            }
        });
        me.getRuleForm().refreshExcludedGroupsGrid();
        window.setLoading(false);
        window.close();
    }
});