Ext.define('Yfn.controller.history.YellowfinReports', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'reports',
    previousPath: '',
    currentPath: null,

    routeConfig: {
        administration: {
            title: 'Administration',
            route: 'administration',
            disabled: true,
            items: {
                generateReport: {
                    title: Uni.I18n.translate('generatereport.title', 'YFN', 'Report generator'),
                    route: 'generatereport',
                    controller: 'Yfn.controller.setup.GenerateReportWizard',
                    privileges: ['privilege.view.device'],
                    action: 'showGenerateReportWizard'
                }
            }
        },
        viewreport: {
            title: Uni.I18n.translate('generatereport.title', 'YFN', 'View Report'),
            route: 'reports/view',
            controller: 'Yfn.controller.YellowfinReportsController',
            privileges: ['privilege.view.device'],
            action: 'showReport'
        }
    }
});