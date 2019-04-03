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
        'Itk.privileges.Issue'
    ],

    controllers: [
        'Itk.controller.history.Workspace',
        'Itk.controller.Issues',
        'Itk.controller.Detail',
        'Itk.controller.ApplyAction',
        'Itk.controller.StartProcess',
        'Itk.controller.Overview',
        'Itk.controller.SetPriority',
        'Itk.controller.BulkChangeIssues'
    ],

    stores: [
    ],

    refs: [],

    init: function () {
        this.initMenu();
    },

    initMenu: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            issues = null,
            issueManagement = null,
            historian = me.getController('Itk.controller.history.Workspace'); // Forces route registration.

        if (Itk.privileges.Issue.canViewAdmimIssue()) {
            Uni.store.MenuItems.add(Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.workspace', 'ITK', 'Workspace'),
                glyph: 'workspace',
                portal: 'workspace',
                index: 30
            }));
        }

        if (Itk.privileges.Issue.canViewAdmimIssue()) {
            issues = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.issues', 'ITK', 'Issues'),
                portal: 'workspace',
                route: 'issues',
                items: [
                    {
                        itemId: 'issues-item',
                        text: Uni.I18n.translate('device.issues', 'ITK', 'Issues'),
                        href: router.getRoute('workspace/issues').buildUrl({}, {
                            status: ['status.open', 'status.in.progress']
                        })
                    },
                    {
                        text: Uni.I18n.translate('device.issues.issuesOverview', 'ITK', 'Issues overview'),
                        itemId: 'issues-overview-item',
                        href: router.getRoute('workspace/issuesoverview').buildUrl()
                    },
                    {
                        itemId: 'my-open-issues-item',
                        text: Uni.I18n.translate('device.myOpenIssues','ITK','My open issues'),
                        href: router.getRoute('workspace/issues').buildUrl({}, {
                            status: ['status.open', 'status.in.progress'],
                            myopenissues: true
                        })
                    },
                    {
                        itemId: 'my-workgroup-issues-item',
                        text: Uni.I18n.translate('device.myWorkgroupsIssues', 'ITK', 'My workgroups issues'),
                        href: router.getRoute('workspace/issues').buildUrl({}, {
                            status: ['status.open', 'status.in.progress'],
                            myworkgroupissues: true
                        })
                    }
                ]
            });
        }
        if (Itk.privileges.Issue.canViewAdminIssueCreationRule()){
            issueManagement = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.issue.managemnet', 'ITK', 'Issue management'),
                portal: 'administration',
                route: 'issuemanagement',
                items: [
                    {
                        text: Uni.I18n.translate('general.issueCreationRules','ITK','Issue creation rules'),
                        href: router.getRoute('administration/issuecreationrules').buildUrl()
                    }
                ]
            });
        }

        if (issues !== null) {
            Uni.store.PortalItems.add(issues);
        }
        if (issueManagement !== null){
            Uni.store.PortalItems.add(issueManagement);
        }
    }
});