/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.datavalidation.controller.Main', {
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
        'Imt.datavalidation.controller.MainOverview',
        'Imt.datavalidation.controller.Detail',
        'Isu.controller.MessageWindow',
        'Imt.datavalidation.controller.ApplyAction',
        'Imt.datavalidation.controller.SetPriority'

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
        this.getApplication().fireEvent('initIssueType', 'usagepointdatavalidation');
    },

    initMenu: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            dataCollection = null,
            items = [];

        if (Isu.privileges.Issue.canViewAdminDevice()) {

            if (Isu.privileges.Issue.canViewAdminDevice()) {
                items.push({
                    text: Uni.I18n.translate('general.issues', 'IMT', 'Issues'),
                    href: router.getRoute('workspace/issues').buildUrl({}, {issueType: ['usagepointdatavalidation']})
                });
                items.push({
                    text: Uni.I18n.translate('datavalidation.myOpenIssues', 'IMT', 'My open issues'),
                    itemId: 'datavalidation-my-open-issues',
                    href: router.getRoute('workspace/issues').buildUrl({}, {
                        issueType: ['usagepointdatavalidation'],
                        myopenissues: true,
                        status: ['status.open', 'status.in.progress']
                    })
                });
                items.push({
                    text: Uni.I18n.translate('datavalidation.myWorkgroupsIssues', 'IMT', 'My workgroups issues'),
                    itemId: 'datavalidation-my-workgroup-issues',
                    href: router.getRoute('workspace/issues').buildUrl({}, {
                        issueType: ['usagepointdatavalidation'],
                        myworkgroupissues: true,
                        status: ['status.open', 'status.in.progress']
                    })
                });
            }

            dataCollection = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.dataValidation', 'IMT', 'Data validation'),
                portal: 'workspace',
                route: 'usagepointdatavalidation',
                items: items
            });
        }

        if (dataCollection !== null) {
            Uni.store.PortalItems.add(dataCollection);
        }
    }
});