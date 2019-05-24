/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.issues.AddManuallyRuleItem', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Isu.view.issues.ManuallyRuleItem',
        'Isu.store.IssueDevices',
        'Isu.store.IssueReasons',
        'Isu.store.DueinTypes',
        'Isu.store.IssueWorkgroupAssignees',
        'Isu.store.UserList'
    ],
    itemId: 'issue-manually-creation-rules-item-add',
    alias: 'widget.issue-manually-creation-rules-item-add',
    returnLink: null,
    action: null,
    bulkAction: false,
    deviceId: null,

    initComponent: function () {
        var me = this;

        me.content = [{
                xtype: 'issue-manually-creation-rules-item',
                itemId: 'issue-manually-creation-rules-item',
                title: me.bulkAction ? '' : Uni.I18n.translate('workspace.newManuallyIssue', 'ISU', 'Create issue'),
                ui: 'large',
                bulkAction: me.bulkAction,
                returnLink: me.returnLink,
                action: me.action,
                deviceId: me.deviceId
            }
        ];

        me.callParent(arguments);

        var manualIssue = Ext.create('Isu.model.ManuallyRuleItem'),
        dependencies = ['Isu.store.IssueDevices', 'Isu.store.IssueReasons'],
        dependenciesCounter = dependencies.length,
        onDependenciesLoaded = function () {
            dependenciesCounter--;
            if (!dependenciesCounter) {
                me.down('issue-manually-creation-rules-item').loadRecord(manualIssue);
                if (me.deviceId){
                    var deviceIdCombo =  me.down('issue-manually-creation-rules-item').child('#deviceId');
                    var device = deviceIdCombo.store.find('name', me.deviceId);
                    if (device !== -1){
                        deviceIdCombo.setRawValue(me.deviceId);
                        deviceIdCombo.setValue(deviceIdCombo.store.getAt(device).get('id'));
                    }

                }
                me.setLoading(false);
            }
        };

        me.setLoading();
        Ext.Array.each(dependencies, function (store) {
            Ext.getStore(store).load(onDependenciesLoaded);
        });
    }
});