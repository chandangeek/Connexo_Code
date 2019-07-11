/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.controller.CreationRuleGroupsEdit', {
    extend: 'Ext.app.Controller',

    stores: [
        'Isu.store.CreationRules',
        'Isu.store.DeviceGroups',
        'Isu.store.Clipboard'
    ],

    views: [
        'Isu.view.creationrules.EditGroups'
    ],

    refs: [
        {
            ref: 'groupsForm',
            selector: 'issues-creation-rules-edit-groups form'
        },
        {
            ref: 'selectGroupsGrid',
            selector: 'issues-creation-rules-edit-groups isu-device-groups-selection-grid'
        }
    ],

    models: [
        'Isu.model.CreationRule',
        'Isu.model.DeviceGroup',
        'Isu.model.ExcludedDeviceGroup'
    ],

    init: function () {
        this.control({
            'issues-creation-rules-edit-groups button[action=saveGroupExclusions]': {
                click: this.saveAction
            }
        });
    },

    showEdit: function (id) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            clipboard = me.getStore('Isu.store.Clipboard'),
            widget = Ext.widget('issues-creation-rules-edit-groups', {
                router: router,
                returnLink: router.getRoute(router.currentRoute.replace('/addexclgroups', '')).buildUrl()
            }),
            exclGroups = null,
            filteredOutGroupIds = '',
            deviceGroups = me.getStore('Isu.store.DeviceGroups');

        Ext.util.History.on('change', this.checkRoute, me);

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        
        if(!clipboard.get('issuesCreationRuleState')) {
            if (id) {
                me.getModel('Isu.model.CreationRule').load(id, {
                    success: function (record) {
                        clipboard.set('issuesCreationRuleState', record);
                        me.getApplication().fireEvent('issueCreationRuleEdit', record);
                        exclGroups = clipboard.get('issuesCreationRuleState').exclGroups();
                        if(exclGroups && exclGroups.getCount() > 0) {
                            exclGroups.each(function(group) {
                                if (filteredOutGroupIds) {
                                    filteredOutGroupIds = filteredOutGroupIds.concat(',', group.get('deviceGroupId'));
                                } else {
                                    filteredOutGroupIds = filteredOutGroupIds.concat(group.get('deviceGroupId'));
                                }
                            });
                        }
                        deviceGroups.getProxy().setExcludedGroups(filteredOutGroupIds);
                        me.getSelectGroupsGrid().down('pagingtoolbartop').resetPaging();
                        me.getSelectGroupsGrid().down('pagingtoolbarbottom').resetPaging();
                        deviceGroups.load(function (records, operation, success) {
                            if (success == true) {
                                widget.setLoading(false);
                            }
                        });
                    }
                });
            }
        } else {
            exclGroups = clipboard.get('issuesCreationRuleState').exclGroups();
            if(exclGroups && exclGroups.getCount() > 0) {
                exclGroups.each(function(group) {
                    if (filteredOutGroupIds) {
                        filteredOutGroupIds = filteredOutGroupIds.concat(',', group.get('deviceGroupId'));
                    } else {
                        filteredOutGroupIds = filteredOutGroupIds.concat(group.get('deviceGroupId'));
                    }
                });
            }
            deviceGroups.getProxy().setExcludedGroups(filteredOutGroupIds);
            me.getSelectGroupsGrid().down('pagingtoolbartop').resetPaging();
            me.getSelectGroupsGrid().down('pagingtoolbarbottom').resetPaging();
            deviceGroups.load(function (records, operation, success) {
                if (success == true) {
                    widget.setLoading(false);
                }
            });
        }
    },

    checkRoute: function (token) {
        var me = this,
            currentRoute = me.getController('Uni.controller.history.Router').currentRoute,
            allowableRoutes = [
                'administration/creationrules/add',
                'administration/creationrules/edit'
            ];

        Ext.util.History.un('change', me.checkRoute, me);

        if (!currentRoute.startsWith('administration/creationrules')) {
            me.getStore('Isu.store.Clipboard').clear('issuesCreationRuleState');
        }
    },

    saveAction: function () {
        var me = this,
            form = me.getGroupsForm(),
            exclGroupsStore = me.getStore('Isu.store.Clipboard').get('issuesCreationRuleState').exclGroups();

        form.setLoading();
        
        Ext.each(me.getSelectGroupsGrid().getSelectionModel().getSelection(), function (selectedGroup) {
            exclGroupsStore.add(Ext.create('Isu.model.ExcludedDeviceGroup', 
                {
                    deviceGroupId: selectedGroup.get('id'),
                    deviceGroupName: selectedGroup.get('name'),
                    isGroupDynamic: selectedGroup.get('dynamic')
                }
            ));
        });
        window.location.href = form.returnLink;
    }
});