/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Iws.controller.Main', {
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
        'Iws.controller.MainOverview',
        'Iws.controller.Detail',
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
        this.getApplication().fireEvent('initIssueType', 'webservice');
    },

    initMenu: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            webservice = null,
            items = [];

            Uni.store.MenuItems.add(Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.workspace','Iws','Workspace'),
                glyph: 'workspace',
                portal: 'workspace',
                index: 30
            }));

                items.push({
                    text: Uni.I18n.translate('general.issues','Iws','Issues'),
                    itemId: 'webservice-issues',
                    href: router.getRoute('workspace/issues').buildUrl({}, {issueType: ['webservice']})
                });
                items.push({
                    text: Uni.I18n.translate('general.myOpenIssues','Iws','My open issues'),
                    itemId: 'webservice-my-open-issues',
                    href: router.getRoute('workspace/issues').buildUrl({}, {issueType: ['webservice'], myopenissues: true, status: ['status.open', 'status.in.progress']})
                });
                items.push({
                    text: Uni.I18n.translate('general.myWorkgroupsIssues', 'Iws', 'My workgroups issues'),
                    itemId: 'webservice-my-workgroup-issues',
                    href: router.getRoute('workspace/issues').buildUrl({}, {issueType: ['webservice'], myworkgroupissues: true, status: ['status.open', 'status.in.progress']})
                });
            webserviceIssue = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.webservices','Iws','Service calls'),
                portal: 'workspace',
                items:  items
            });

        if (webserviceIssue !== null) {
            Uni.store.PortalItems.add(webserviceIssue);
        }
    }
});
