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
        'Idv.controller.MainOverview',
        'Idv.controller.Detail',
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
        this.getApplication().fireEvent('initIssueType', 'datavalidation');
    },

    initMenu: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            dataCollection = null,
            items = [];

        if (Isu.privileges.Issue.canViewAdminDevice()) {
            Uni.store.MenuItems.add(Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.workspace','IDV','Workspace'),
                glyph: 'workspace',
                portal: 'workspace',
                index: 30
            }));
        }

        if (Isu.privileges.Issue.canViewAdminDevice()) {

            if (Isu.privileges.Issue.canViewAdminDevice()) {
                items.push({
                    text: Uni.I18n.translate('general.issues','IDV','Issues'),
                    href: router.getRoute('workspace/issues').buildUrl({}, {issueType: ['datavalidation']})
                });
                items.push({
                    text: Uni.I18n.translate('datavalidation.myOpenIssues','IDV','My open issues'),
                    itemId: 'datavalidation-my-open-issues',
                    href: router.getRoute('workspace/issues').buildUrl({}, {issueType: ['datavalidation'], myopenissues: true, status: ['status.open', 'status.in.progress']})
                });
                items.push({
                    text: Uni.I18n.translate('datavalidation.myWorkgroupsIssues', 'IDV', 'My workgroups issues'),
                    itemId: 'datavalidation-my-workgroup-issues',
                    href: router.getRoute('workspace/issues').buildUrl({}, {issueType: ['datavalidation'], myworkgroupissues: true, status: ['status.open', 'status.in.progress']})
                });
            }

            dataCollection = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.dataValidation','IDV','Data validation'),
                portal: 'workspace',
                route: 'datavalidation',
                items:  items
            });
        }

        if (dataCollection !== null) {
            Uni.store.PortalItems.add(dataCollection);
        }
    }
});