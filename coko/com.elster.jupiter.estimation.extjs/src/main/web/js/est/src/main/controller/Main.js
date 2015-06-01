Ext.define('Est.main.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation',
        'Uni.store.PortalItems',
        'Est.privileges.EstimationConfiguration'
    ],

    controllers: [
        'Est.main.controller.history.Setup',
        'Est.estimationrulesets.controller.EstimationRuleSets',
        'Est.estimationrules.controller.Overview',
        'Est.estimationrules.controller.Detail',
        'Est.estimationrules.controller.Edit',
        'Est.estimationrules.controller.AddReadingTypes',
        'Est.estimationtasks.controller.EstimationTasksOverview',
        'Est.estimationtasks.controller.EstimationTasksDetails',
        'Est.estimationtasks.controller.EstimationTasksHistory',
        'Est.estimationtasks.controller.EstimationTasksActionMenu',
        'Est.estimationtasks.controller.EstimationTasksAddEdit',
        'Est.estimationtasks.controller.EstimationTasksLog'
    ],

    init: function () {
        if (Est.privileges.EstimationConfiguration.canView()) {
            var me = this,
                historian = me.getController('Est.main.controller.history.Setup'),
                portalItem = Ext.create('Uni.model.PortalItem', {
                    title: Uni.I18n.translate('general.dataEstimation', 'EST', 'Data estimation'),
                    portal: 'administration',
                    route: 'dataestimation',
                    items: [
                        {
                            text: Uni.I18n.translate('estimationrulesets.estimationrulesets', 'EST', 'Estimation rule sets'),
                            href: '#/administration/estimationrulesets',
                            route: 'estimationrulesets',
                            privileges: Est.privileges.EstimationConfiguration.view
                        },
                        {
                            text: Uni.I18n.translate('estimationtasks.title', 'EST', 'Estimation tasks'),
                            href: '#/administration/estimationtasks',
                            route: 'estimationtasks',
                            privileges: Est.privileges.EstimationConfiguration.viewTask
                        }
                    ]
                });

            Uni.store.PortalItems.add(portalItem);
        }
    }
});
