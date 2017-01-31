/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.RuleDeviceConfiguration', {
    extend: 'Ext.data.Model',
    fields: [
        'config',
        'deviceType',
        {
            name: 'config_name',
            persist: false,
            mapping: function (data) {
                return data.config.name;
            }
        },
        {
            name: 'config_name',
            persist: false,
            mapping: function (data) {
                return data.config.name;
            }
        },
        {
            name: 'config_name_link',
            persist: false,
            mapping: function (data) {
                return '<a href="#/administration/devicetypes/' + data.deviceType.id + '/deviceconfigurations/' + data.config.id + '">' + Ext.String.htmlEncode(data.config.name) + '</a>';
            }
        },
        {
            name: 'config_description',
            persist: false,
            mapping: function (data) {
                return data.config.description;
            }
        },
        {
            name: 'config_active',
            persist: false,
            mapping: function (data) {
                if (data.config.active) {
                    return Uni.I18n.translate('general.active', 'MDC', 'Active')
                } else {
                    return Uni.I18n.translate('general.inactive', 'MDC', 'Inactive')
                }
            }
        },
        {
            name: 'config_loadProfileCount',
            persist: false,
            mapping: function (data) {
                return '<a href="#/administration/devicetypes/' + data.deviceType.id + '/deviceconfigurations/' + data.config.id + '/loadprofiles">'
                    + Uni.I18n.translatePlural('general.loadProfileConfigurations.counter', data.config.loadProfileCount, 'MDC',
                        '{0} load profile configurations', '{0} load profile configuration', '{0} load profile configurations')
                    + '</a>';
            }
        },
        {
            name: 'config_registerCount',
            persist: false,
            mapping: function (data) {
                return '<a href="#/administration/devicetypes/' + data.deviceType.id + '/deviceconfigurations/' + data.config.id + '/registerconfigurations">'
                    + Uni.I18n.translatePlural('general.registerConfigurations.counter', data.config.registerCount, 'MDC',
                        '{0} register configurations', '{0} register configuration', '{0} register configurations')
                    + '</a>';
            }
        },
        {
            name: 'deviceType_name',
            persist: false,
            mapping: function (data) {
                return data.deviceType.name;
            }
        }
    ]
});


