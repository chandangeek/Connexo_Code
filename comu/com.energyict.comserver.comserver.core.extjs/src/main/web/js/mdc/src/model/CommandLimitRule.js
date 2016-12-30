Ext.define('Mdc.model.CommandLimitRule', {
    extend: 'Uni.model.Version',

    requires: [
        'Mdc.model.DualControlInfo',
        'Mdc.model.Command'
    ],

    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'active', type: 'boolean'},
        {name: 'dayLimit', type: 'int'},
        {name: 'weekLimit', type: 'int'},
        {name: 'monthLimit', type: 'int'},
        {name: 'commands', persist: false},
        {name: 'statusMessage', type: 'string', useNull: true},
        {name: 'availableActions', type: 'auto', persist: false},
        {name: 'currentCounts', type: 'auto', persist: false},

        {
            name: 'statusWithMessage',
            persist: false,
            mapping: function (data) {
                var pendingChanges = data.statusMessage,
                    icon = Ext.isEmpty(pendingChanges) ? '' :
                    '<span class="icon-info" style="margin-left:10px; margin-top:2px; position:absolute;" data-qtip="' + pendingChanges + '"></span>',
                    text = data.active ? Uni.I18n.translate('general.active', 'MDC', 'Active') : Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                return text + icon;
            }
        },

        {
            name: 'dayLimitWithMessage',
            persist: false,
            mapping: function (data) {
                if (data.dayLimit === 0) {
                    return Uni.I18n.translate('general.none', 'MDC', 'None');
                } else {
                    var count = data.dayLimit,
                        icon,
                        text,
                        currentCount;
                    currentCount = _.find(data.currentCounts, function(obj) { return obj.type == 'DAY' });
                    text = Uni.I18n.translate('commandRules.currentDayLimit', 'MDC', 'Current amount of commands already added on {0} is {1}',
                        [Uni.DateTime.formatDateShort(new Date(currentCount.from)), currentCount.currentCount]);
                    icon = '<span class="icon-info" style="margin-left:10px; margin-top:2px; position:absolute;" data-qtip="' + text + '"></span>';
                    return count + icon;
                }
            }
        },

        {
            name: 'weekLimitWithMessage',
            persist: false,
            mapping: function (data) {
                if (data.weekLimit === 0) {
                    return Uni.I18n.translate('general.none', 'MDC', 'None');
                } else {
                    var count = data.weekLimit,
                        icon,
                        text,
                        currentCount;
                    currentCount = _.find(data.currentCounts, function(obj) { return obj.type == 'WEEK' });
                    text = Uni.I18n.translate('commandRules.currentPeriodLimit', 'MDC', 'Current amount of commands already between {0} and {1} is {2}',
                        [Uni.DateTime.formatDateShort(new Date(currentCount.from)), Uni.DateTime.formatDateShort(new Date(currentCount.to -1)), currentCount.currentCount]);
                    icon = '<span class="icon-info" style="margin-left:10px; margin-top:2px; position:absolute;" data-qtip="' + text + '"></span>';
                    return count + icon;
                }
            }
        },

        {
            name: 'monthLimitWithMessage',
            persist: false,
            mapping: function (data) {
                if (data.monthLimit === 0) {
                    return Uni.I18n.translate('general.none', 'MDC', 'None');
                } else {
                    var count = data.monthLimit,
                        icon,
                        text,
                        currentCount;
                    currentCount = _.find(data.currentCounts, function(obj) { return obj.type == 'MONTH' });
                    text = Uni.I18n.translate('commandRules.currentPeriodLimit', 'MDC', 'Current amount of commands already between {0} and {1} is {2}',
                        [Uni.DateTime.formatDateShort(new Date(currentCount.from)), Uni.DateTime.formatDateShort(new Date(currentCount.to -1)), currentCount.currentCount]);
                    icon = '<span class="icon-info" style="margin-left:10px; margin-top:2px; position:absolute;" data-qtip="' + text + '"></span>';
                    return count + icon;
                }
            }
        }
    ],

    associations: [
        {
            name: 'dualControl',
            type: 'hasOne',
            model: 'Mdc.model.DualControlInfo',
            associationKey: 'dualControl',
            getterName: 'getDualControl',
            setterName: 'setDualControl'
        },
        {
            name: 'commands',
            type: 'hasMany',
            model: 'Mdc.model.Command',
            associationKey: 'commands',
            foreignKey: 'commands',
            getTypeDiscriminator: function (node) {
                return 'Mdc.model.Command';
            }
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/crr/commandrules',
        reader: {
            type: 'json'
        }
    }
});
