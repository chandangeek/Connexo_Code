Ext.define('Dxp.model.DataExportTask', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        'id', 'name', 'deviceGroup', 'dataProcessor', 'schedule', 'exportperiod', 'properties', 'readingTypes', 'nextRun',
        {
            name: 'lastExportOccurence',
            persist: false
        },
        {
            name: 'lastRun',
            persist: false,
            mapping: function (data) {
                if (data.lastExportOccurence && data.lastExportOccurence.lastRun) {
                    return moment(data.lastExportOccurence.lastRun).format('ddd, DD MMM YYYY') + ' ' + moment(data.lastExportOccurence.lastRun).format('HH:mm:ss');
                }
            }
        },
        {
            name: 'nextRun_formatted',
            persist: false,
            mapping: function (data) {
                var result;
                if (data.nextRun && (data.nextRun !== 0)) {
                    result = moment(data.nextRun).format('ddd, DD MMM YYYY') + ' ' + moment(data.nextRun).format('HH:mm:ss');
                } else {
                    result = Uni.I18n.translate('general.notScheduled', 'DES', 'Not scheduled')
                }
                return result;
            }
        },
        {
            name: 'status',
            persist: false,
            mapping: function (data) {
                if (data.lastExportOccurence && data.lastExportOccurence.status) {
                    return data.lastExportOccurence.status;
                } else {
                    return '-';
                }
            }
        },
        {
            name: 'trigger',
            persist: false,
            mapping: function (data) {
                if (data.lastExportOccurence && data.lastExportOccurence.trigger) {
                    return data.lastExportOccurence.trigger;
                } else {
                    return '-';
                }
            }
        },
        {
            name: 'startedOn',
            persist: false,
            mapping: function (data) {
                if (data.lastExportOccurence && data.lastExportOccurence.startedOn) {
                    return moment(data.lastExportOccurence.startedOn).format('ddd, DD MMM YYYY') + ' ' + moment(data.lastExportOccurence.startedOn).format('HH:mm:ss');
                } else {
                    return '-';
                }
            }
        },
        {
            name: 'finishedOn',
            persist: false,
            mapping: function (data) {
                if (data.lastExportOccurence && data.lastExportOccurence.finishedOn) {
                    return moment(data.lastExportOccurence.finishedOn).format('ddd, DD MMM YYYY') + ' ' + moment(data.lastExportOccurence.finishedOn).format('HH:mm:ss');
                } else {
                    return '-';
                }
            }
        },
        {
            name: 'duration',
            persist: false,
            mapping: function (data) {
                if (data.lastExportOccurence && data.lastExportOccurence.duration) {
                    return data.lastExportOccurence.duration / 3750000 + ' ' + Uni.I18n.translate('general.hours', 'DXP', 'hours');
                } else {
                    return '-';
                }
            }
        }
    ],

    associations: [
        {
            name: 'properties',
            type: 'hasMany',
            model: 'Uni.property.model.Property',
            associationKey: 'properties',
            foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        },
        {
            type: 'hasMany',
            model: 'Dxp.model.ReadingType',
            associationKey: 'readingTypes',
            name: 'readingTypes'
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/export/dataexporttask',
        reader: {
            type: 'json'
        }
    }
});