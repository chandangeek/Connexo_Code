Ext.define('Cfg.model.RuleDeviceConfiguration', {
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
                return '<a href="#/administration/devicetypes/' + data.deviceType.id + '/deviceconfigurations/' + data.config.id + '">' + data.config.name + '</a>';
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
                    return Uni.I18n.translate('validation.active', 'CFG', 'Active')
                } else {
                    return Uni.I18n.translate('validation.inactive', 'CFG', 'Inactive')
                }
            }
        },
        {
            name: 'config_loadProfileCount',
            persist: false,
            mapping: function (data) {
                return '<a href="#/administration/devicetypes/' + data.deviceType.id + '/deviceconfigurations/' + data.config.id + '/loadprofiles">' + data.config.loadProfileCount + ' load profile configurations</a>';
            }
        },
        {
            name: 'config_registerCount',
            persist: false,
            mapping: function (data) {
                return '<a href="#/administration/devicetypes/' + data.deviceType.id + '/deviceconfigurations/' + data.config.id + '/registerconfigurations">' + data.config.registerCount + ' register configurations</a>';
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

