/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Yfn.controller.history.YellowfinReports', {
    extend: 'Uni.controller.history.Converter',

    requires: [
        'Yfn.privileges.Yellowfin',
        'Yfn.controller.YellowfinReportsController',
        'Yfn.controller.setup.GenerateReportWizard'
    ],

    rootToken: 'reports',
    previousPath: '',
    currentPath: null,

    routeConfig: {
        workspace: {
            title: Uni.I18n.translate('general.workspace', 'YFN', 'Workspace'),
            route: 'workspace',
            disabled: true,
            items: {
                generatereport: {
                    title: Uni.I18n.translate('generatereport.wizardMenu', 'YFN', 'Generate report'),
                    route: 'generatereport',
                    controller: 'Yfn.controller.setup.GenerateReportWizard',
                    privileges: Yfn.privileges.Yellowfin.view,
                    action: 'showGenerateReportWizard'
                }
            }
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