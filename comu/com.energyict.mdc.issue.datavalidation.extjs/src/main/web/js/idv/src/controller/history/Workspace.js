Ext.define('Idv.controller.history.Workspace', {
    extend: 'Uni.controller.history.Converter',
    requires:[
        'Isu.privileges.Issue'
    ],
    rootToken: 'workspace',
    previousPath: '',
    currentPath: null,

    routeConfig: {
        "workspace/datavalidationoverview": {
            title: Uni.I18n.translate('validation.validationOverview.title', 'IDV', 'Validation overview'),
            route: 'workspace/datavalidationoverview',
            controller: 'Ddv.controller.ValidationOverview',
            action: 'showOverview',
            filter: 'Ddv.model.ValidationOverviewFilter'
        }
    },

    init: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.addConfig(this.routeConfig);
    }
});
