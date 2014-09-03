Ext.define('Mdc.model.RegisterData', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'timeStamp', type:'date', dateFormat: 'time'},
        {name: 'reportedDateTime', type:'date', dateFormat: 'time'},
        {name: 'validationStatus', type:'auto', useNull: true},
        {name: 'type', type:'string', useNull: true},
        {name: 'value', type:'auto', useNull: true},
        {name: 'dataValidated', type:'auto'},
        {name: 'suspectReason', type:'auto'},
        {name: 'validationResult', type:'auto'},
        {
            name: 'suspect_rules',
            persist: false,
            mapping: function (data) {
                if (!Ext.isEmpty(data.suspectReason)) {
                    var str = '',
                        prop,
                        failEqualDataValue,
                        intervalFlagsValue = '';
                    Ext.Array.each(data.suspectReason, function (rule) {
                        if (!Ext.isEmpty(rule.properties)) {
                            switch (rule.displayName) {
                                case 'Threshold violation':
                                    prop = ' - ' + rule.properties[0].key.charAt(0).toUpperCase() + rule.properties[0].key.substring(1) + ': ' + rule.properties[0].propertyValueInfo.value + ', ' +
                                        rule.properties[1].key.charAt(0).toUpperCase() + rule.properties[1].key.substring(1) + ': ' + rule.properties[1].propertyValueInfo.value;
                                    break;
                                case 'Register increase':
                                    if (rule.properties[0].propertyValueInfo.value) {
                                        failEqualDataValue = Uni.I18n.translate('general.yes', 'MDC', 'Yes');
                                    } else {
                                        failEqualDataValue = Uni.I18n.translate('general.no', 'MDC', 'No');
                                    }
                                    prop = ' - ' + Uni.I18n.translate('device.registerData.failEqualData', 'MDC', 'Fail equal data') + ': ' + failEqualDataValue;
                                    break;
                                case 'Interval state':
                                    Ext.Array.each(rule.properties[0].propertyValueInfo.value, function (idValue) {
                                        Ext.Array.each(rule.properties[0].propertyTypeInfo.predefinedPropertyValuesInfo.possibleValues, function (item) {
                                            if (idValue === item.id) {
                                                intervalFlagsValue += item.name + ', ';
                                            }
                                        });
                                    });
                                    intervalFlagsValue = intervalFlagsValue.slice(0, -2);
                                    prop = ' - ' + Uni.I18n.translate('deviceloadprofiles.intervalFlags', 'MDC', 'Interval flags') + ': ' + intervalFlagsValue;
                                    break;
                                default:
                                    prop = '';
                                    break;
                            }
                        } else {
                            prop = '';
                        }
                        if (rule.name === 'removed rule') {
                            str += rule.name + '<br>';
                        } else {
                            str += '<a href="#/administration/validation/rulesets/' + rule.ruleSet.id + '/rules/' + rule.id + '">' + rule.name + '</a>' + prop + '<br>';
                        }
                    });
                    return str;
                } else {
                    return '';
                }
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{mRID}/registers/{registerId}/data'
    }
});
