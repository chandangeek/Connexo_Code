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
        'Isu.privileges.Issue',

        'Cfg.privileges.Validation',
        'Ddv.controller.ValidationOverview',
        'Ddv.controller.Main',
        'Ddv.model.ValidationOverviewFilter'

    ],

    controllers: [
        'Idv.controller.history.Workspace',
        'Idv.controller.MainOverview',
        'Idv.controller.Overview',
        'Idv.controller.Detail',
        'Idv.controller.ApplyAction',
        'Idv.controller.BulkChangeIssues',
        'Isu.controller.MessageWindow'
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
            items = [],
            historian = me.getController('Idv.controller.history.Workspace'); // Forces route registration.

        if (Isu.privileges.Issue.canViewAdminDevice()) {
            Uni.store.MenuItems.add(Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.workspace','IDV','Workspace'),
                glyph: 'workspace',
                portal: 'workspace',
                index: 30
            }));
        }

        if (Isu.privileges.Issue.canViewAdminDevice() || Cfg.privileges.Validation.canView()) {

            if (Isu.privileges.Issue.canViewAdminDevice()) {
                items.push({
                    text: Uni.I18n.translate('general.issues','IDV','Issues'),
                    href: router.getRoute('workspace/datavalidationissues').buildUrl()
                });
                items.push({
                    text: Uni.I18n.translate('datavalidation.myOpenIssues','IDV','My open issues'),
                    itemId: 'my-open-issues',
                    href: router.getRoute('workspace/datavalidationissues').buildUrl({}, {myopenissues: true})
                });
            }

            if (Cfg.privileges.Validation.canView()) {
                items.push( {
                    text: Uni.I18n.translate('validation.validationOverview.title', 'IDV', 'Validation overview'),
                    href: '#/workspace/datavalidationoverview'
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