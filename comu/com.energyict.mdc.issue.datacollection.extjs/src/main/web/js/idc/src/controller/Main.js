Ext.define('Idc.controller.Main', {
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
        'Idc.controller.history.Workspace',
        'Idc.controller.MainOverview',
        'Idc.controller.Overview',
        'Idc.controller.Detail',
        'Idc.controller.ApplyAction',
        'Idc.controller.BulkChangeIssues',
        'Isu.controller.MessageWindow'
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

        if (Isu.privileges.Issue.canViewAdminDevice()) {
            Uni.store.MenuItems.add(Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.workspace','IDC','Workspace'),
                glyph: 'workspace',
                portal: 'workspace',
                index: 30
            }));
        }

        if (Isu.privileges.Issue.canViewAdminDevice()) {
            dataCollection = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.dataCollection','IDC','Data collection'),
                portal: 'workspace',
                route: 'datacollection',
                items: [
                    {
                        text: Uni.I18n.translate('datacollection.issues','IDC','Issues'),
                        href: router.getRoute('workspace/datacollectionissues').buildUrl()
                    },
                    {
                        text: Uni.I18n.translate('datacollection.myOpenIssues','IDC','My open issues'),
                        itemId: 'my-open-issues',
                        href: router.getRoute('workspace/datacollectionissues').buildUrl({}, {myopenissues: true})
                    }
                ]
            });
        }

        if (dataCollection !== null) {
            Uni.store.PortalItems.add(dataCollection);
        }
    }
});