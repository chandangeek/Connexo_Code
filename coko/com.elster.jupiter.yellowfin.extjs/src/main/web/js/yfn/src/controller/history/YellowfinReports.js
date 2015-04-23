Ext.define('Yfn.controller.history.YellowfinReports', {
    extend: 'Uni.controller.history.Converter',

    requires: [
        'Yfn.controller.YellowfinReportsController',
        'Yfn.controller.setup.GenerateReportWizard'
    ],

    rootToken: 'reports',
    previousPath: '',
    currentPath: null,

    routeConfig: {
         generatereport: {
             title: Uni.I18n.translate('generatereport.reportGenerator', 'YFN', 'Report generator'),
                 route: 'administration/generatereport',
                 controller: 'Yfn.controller.setup.GenerateReportWizard',
                 privileges: Yfn.privileges.Yellowfin.view,
                 action: 'showGenerateReportWizard'
        },
        viewreport: {
            title: Uni.I18n.translate('generatereport.viewReport', 'YFN', 'View Report'),
            route: 'reports/view',
            controller: 'Yfn.controller.YellowfinReportsController',
            privileges: Yfn.privileges.Yellowfin.view,
            action: 'showReport'
        }
    }
});