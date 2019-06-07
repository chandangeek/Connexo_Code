/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation',
        'Uni.controller.Configuration',
        'Uni.controller.history.EventBus',
        'Uni.model.PortalItem',
        'Uni.store.PortalItems',
        'Uni.store.MenuItems',
        'Isu.privileges.Issue'
    ],

    controllers: [
        'Itk.controller.MainOverview',
        'Itk.controller.Detail',
        'Isu.controller.MessageWindow'
    ],

    stores: [
        'Isu.store.Issues'
    ],

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        },
        {
            ref: 'contentPanel',
            selector: 'viewport > #contentPanel'
        }
    ],

    init: function () {
        this.initMenu();
        this.getApplication().fireEvent('initIssueType', 'task');
    },

    initMenu: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            taskIssue = null,
            items = [];

            Uni.store.MenuItems.add(Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.workspace','ITK','Workspace'),
                glyph: 'workspace',
                portal: 'workspace',
                index: 30
            }));



                items.push({
                    text: Uni.I18n.translate('general.issues','ITK','Issues'),
                    href: router.getRoute('workspace/issues').buildUrl({}, {issueType: ['task']})
                });
                items.push({
                    text: Uni.I18n.translate('task.myOpenIssues','ITK','My open issues'),
                    itemId: 'datavalidation-my-open-issues',
                    href: router.getRoute('workspace/issues').buildUrl({}, {issueType: ['task'], myopenissues: true, status: ['status.open', 'status.in.progress']})
                });
                items.push({
                    text: Uni.I18n.translate('task.myWorkgroupsIssues', 'ITK', 'My workgroups issues'),
                    itemId: 'datavalidation-my-workgroup-issues',
                    href: router.getRoute('workspace/issues').buildUrl({}, {issueType: ['task'], myworkgroupissues: true, status: ['status.open', 'status.in.progress']})
                });
            taskIssue = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.task.issues','ITK','Task issues'),
                portal: 'workspace',
                route: 'task',
                items:  items
            });

        if (taskIssue !== null) {
            Uni.store.PortalItems.add(taskIssue);
        }
    }
});