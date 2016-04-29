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
        'Idc.controller.Detail',
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
        this.getApplication().fireEvent('initIssueType', 'datacollection');
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
                        href: router.getRoute('workspace/issues').buildUrl({}, {issueType: ['datacollection']})
                    },
                    {
                        text: Uni.I18n.translate('datacollection.myOpenIssues','IDC','My open issues'),
                        itemId: 'datacollection-my-open-issues',
                        href: router.getRoute('workspace/issues').buildUrl({}, {issueType: ['datacollection'], myopenissues: true})
                    }
                ]
            });
        }

        if (dataCollection !== null) {
            Uni.store.PortalItems.add(dataCollection);
        }
    }
});