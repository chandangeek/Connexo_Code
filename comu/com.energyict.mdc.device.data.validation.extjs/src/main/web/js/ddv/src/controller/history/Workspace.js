/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Ddv.controller.history.Workspace', {
    extend: 'Uni.controller.history.Converter',
    rootToken: 'workspace',

    routeConfig: {
        "workspace/dataquality": {
            title: Uni.I18n.translate('general.dataQuality', 'DDV', 'Data quality'),
            route: 'workspace/dataquality',
            controller: 'Ddv.controller.DataQuality',
            action: 'showDataQuality',
            privileges: Cfg.privileges.Validation.viewResultsOrAdministerDataQuality
        }
    }
});
