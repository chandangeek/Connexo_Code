/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.model.AppServer', {
    extend: 'Uni.model.Version',
    fields: [
        'name', 'active', 'executionSpecs', 'importServices', 'endPointConfigurations',
        {
            name: 'version',
            defaultValue: 0
        },
        {
            name: 'id',
            persist: false,
            mapping: function (data) {
                return data.name;
            }
        },
        {
            name: 'exportDirectory'
        },
        {
            name: 'importDirectory'
        },
        {
            name: 'status',
            persist: false,
            mapping: function (data) {
                if (data.active) {
                    return Uni.I18n.translate('general.active', 'APR', 'Active');
                } else {
                    return Uni.I18n.translate('general.inactive', 'APR', 'Inactive');
                }
            }
        },
        {
            name: 'importServicesCount',
            persist: false,
            mapping: function(data){
                if (data.importServices !== undefined || data.importServices != null) {
                    return data.importServices.length;
                } else {
                    return '';
                }
            }
        },
        {
            name: 'messageServicesCount',
            persist: false,
            mapping: function(data){
                if (data.executionSpecs !== undefined || data.executionSpecs != null) {
                    return data.executionSpecs.length;
                } else {
                    return '';
                }
            }
        },
        {
            name: 'webserviceEndpointsCount',
            persist: false,
            mapping: function(data){
                if (data.endPointConfigurations !== undefined || data.endPointConfigurations != null) {
                    return data.endPointConfigurations.length;
                } else {
                    return '';
                }
            }
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/apr/appserver',
        reader: {
            type: 'json'
        }
    }
});