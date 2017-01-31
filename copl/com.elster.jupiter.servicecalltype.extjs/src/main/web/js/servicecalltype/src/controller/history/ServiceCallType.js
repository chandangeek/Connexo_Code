/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sct.controller.history.ServiceCallType', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',

    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration', 'SCT', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                servicecalltypes: {
                    title: Uni.I18n.translate('general.serviceCallTypes', 'SCT', 'Service call types'),
                    privileges: Sct.privileges.ServiceCallType.view,
                    route: 'servicecalltypes',
                    controller: 'Sct.controller.ServiceCallTypes',
                    action: 'showServiceCallTypes'
                }
            }
        }
    }
});
