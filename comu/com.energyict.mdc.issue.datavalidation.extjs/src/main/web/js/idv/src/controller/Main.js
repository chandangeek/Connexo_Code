Ext.define('Idv.controller.Main', {
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
        'Idv.controller.history.Workspace',
        'Idv.controller.MainOverview',
        'Idv.controller.Overview',
        'Idv.controller.Detail',
        'Idv.controller.ApplyAction',
        'Idv.controller.BulkChangeIssues',
        'Idv.controller.MessageWindow'
    ],

    stores: [
        'Idv.store.Issues'
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
            historian = me.getController('Idv.controller.history.Workspace'); // Forces route registration.

        if (Isu.privileges.Issue.canViewAdminDevice()) {
            Uni.store.MenuItems.add(Ext.create('Uni.model.MenuItem', {
                text: 'Workspace',
                glyph: 'workspace',
                portal: 'workspace',
                index: 30
            }));
        }

        if (Isu.privileges.Issue.canViewAdminDevice()) {
            dataCollection = Ext.create('Uni.model.PortalItem', {
                title: 'Data validation',
                portal: 'workspace',
                route: 'datavalidation',
                items: [
                    {
                        text: 'Issues',
                        href: router.getRoute('workspace/datavalidationissues').buildUrl()
                    },
                    {
                        text: 'My open issues',
                        itemId: 'my-open-issues',
                        href: router.getRoute('workspace/datavalidationissues').buildUrl({}, {myopenissues: true})
                    }
                ]
            });
        }

        if (dataCollection !== null) {
            Uni.store.PortalItems.add(dataCollection);
        }
    }
});