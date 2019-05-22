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

    initComponent: function () {
        var me = this;

        me.content = [{
                xtype: 'issue-manually-creation-rules-item',
                itemId: 'issue-manually-creation-rules-item',
                title: me.bulkAction ? '' : Uni.I18n.translate('workspace.newManuallyIssue', 'ISU', 'Create issue'),
                ui: 'large',
                bulkAction: me.bulkAction,
                returnLink: me.returnLink,
                action: me.action
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
                me.setLoading(false);
            }
        };

        me.setLoading();
        Ext.Array.each(dependencies, function (store) {
            Ext.getStore(store).load(onDependenciesLoaded);
        });
    }
});