/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isc.controller.Main', {
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
        'Isc.controller.MainOverview',
        'Isc.controller.Detail',
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
        this.getApplication().fireEvent('initIssueType', 'servicecall');
    },

    initMenu: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            servicecall = null,
            items = [];

            Uni.store.MenuItems.add(Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.workspace','ISC','Workspace'),
                glyph: 'workspace',
                portal: 'workspace',
                index: 30
            }));

                items.push({
                    text: Uni.I18n.translate('general.issues','ISC','Issues'),
                    itemId: 'servicecall-issues',
                    href: router.getRoute('workspace/issues').buildUrl({}, {issueType: ['servicecall']})
                });
                items.push({
                    text: Uni.I18n.translate('general.myOpenIssues','ISC','My open issues'),
                    itemId: 'servicecall-my-open-issues',
                    href: router.getRoute('workspace/issues').buildUrl({}, {issueType: ['servicecall'], myopenissues: true, status: ['status.open', 'status.in.progress']})
                });
                items.push({
                    text: Uni.I18n.translate('general.myWorkgroupsIssues', 'ISC', 'My workgroups issues'),
                    itemId: 'servicecall-my-workgroup-issues',
                    href: router.getRoute('workspace/issues').buildUrl({}, {issueType: ['servicecall'], myworkgroupissues: true, status: ['status.open', 'status.in.progress']})
                });
            servicecallIssue = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.serviceCalls','ISC','Service calls'),
                portal: 'workspace',
                items:  items
            });

        if (servicecallIssue !== null) {
            Uni.store.PortalItems.add(servicecallIssue);
        }
    }
});
