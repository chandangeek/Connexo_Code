Ext.define('Idc.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation',
        'Uni.controller.Configuration',
        'Uni.controller.history.EventBus',
        'Uni.model.PortalItem',
        'Uni.store.PortalItems',
        'Uni.store.MenuItems'
    ],

    controllers: [
        'Idc.controller.history.Workspace',
        'Idc.controller.MainOverview',
        'Idc.controller.Overview',
        'Idc.controller.Detail',
        'Idc.controller.ApplyAction',
        'Idc.controller.BulkChangeIssues',
        'Idc.controller.MessageWindow'
    ],

    stores: [
        'Idc.store.Issues'
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
    },

    initMenu: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            dataCollection = null,
            historian = me.getController('Idc.controller.history.Workspace'); // Forces route registration.

        if (Uni.Auth.hasAnyPrivilege(['privilege.view.issue', 'privilege.comment.issue', 'privilege.close.issue', 'privilege.assign.issue', 'privilege.action.issue'])) {
            Uni.store.MenuItems.add(Ext.create('Uni.model.MenuItem', {
                text: 'Workspace',
                glyph: 'workspace',
                portal: 'workspace',
                index: 30
            }));
        }

        if (Uni.Auth.hasAnyPrivilege(['privilege.view.issue', 'privilege.comment.issue', 'privilege.close.issue', 'privilege.assign.issue', 'privilege.action.issue'])) {
            dataCollection = Ext.create('Uni.model.PortalItem', {
                title: 'Data collection',
                portal: 'workspace',
                route: 'datacollection',
                items: [
                    {
                        text: 'Overview',
                        href: router.getRoute('workspace/datacollection').buildUrl()
                    },
                    {
                        text: 'Issues',
                        href: router.getRoute('workspace/datacollection/issues').buildUrl()
                    },
                    {
                        text: 'My open issues',
                        itemId: 'my-open-issues',
                        href: router.getRoute('workspace/datacollection/issues').buildUrl({}, {myopenissues: true})
                    }
                ]
            });
        }

        if (dataCollection !== null) {
            Uni.store.PortalItems.add(dataCollection);
        }
    }
});