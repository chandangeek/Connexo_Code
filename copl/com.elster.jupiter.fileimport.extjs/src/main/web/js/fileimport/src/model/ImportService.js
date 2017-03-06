/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fim.model.ImportService', {
    extend: 'Uni.model.Version',
    requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        'id',
        'name',
        'active',
        'deleted',
        'scheduled',
        'application',
        'importerAvailable',
        {
            name: 'applicationDisplay',
            persist: false,
            convert: function (value, record) {
                return Uni.I18n.translate(record.get('application'), 'FIM', record.get('application'));
            }
        },
        'importDirectory',
        'inProcessDirectory',
        'successDirectory',
        'failureDirectory',
        'pathMatcher',
        'importerName',
        'scanFrequency',
        'importerInfo',
        'activeInUI',
        {
            name: 'statusDisplay',
            persist: false,
            convert: function (value, record) {
                return record.get('deleted') ? Uni.I18n.translate('general.removed', 'FIM', 'Removed') :
                    !record.get('importerAvailable') ? Uni.I18n.translate('general.notAvailable', 'FIM', 'Not available') :
                        !record.get('active') ? Uni.I18n.translate('general.inactive', 'FIM', 'Inactive') :
                            !record.get('scheduled') ? Uni.I18n.translate('general.notScheduled', 'FIM', 'Not scheduled') :
                                Uni.I18n.translate('general.active', 'FIM', 'Active');
            }
        },
        {
            name: 'statusTooltip',
            persist: false,
            convert: function (value, record) {
                return record.get('deleted') ? Uni.I18n.translate('importService.status.removed', 'FIM', 'This import service has been removed.') :
                    !record.get('importerAvailable') ? Uni.I18n.translate('importService.status.notAvailable', 'FIM', "This import service's configured file importer is not available and it will not be executed.") :
                        !record.get('active') ? Uni.I18n.translate('importService.status.inactive', 'FIM', 'This import service is inactive and it will not be executed.') :
                            !record.get('scheduled') ? Uni.I18n.translate('importService.status.notScheduled', 'FIM', 'This import service has not been configured on any application server and it will not be executed.') :
                                Uni.I18n.translate('importService.status.active', 'FIM', 'This import service is active and it will be executed by an application server.');
            }
        },
        {
            name: 'scanFrequencyDisplay',
            persist: false,
            convert: function (value, record) {
                return Ext.String.format(Uni.I18n.translate('general.minutes', 'FIM', 'Every {0} minute(s)'), record.get('scanFrequency'));
            }
        },
        {
            name: 'fileImporter',
            persist: false,
            convert: function (value, record) {
                if (record.data.importerInfo && record.data.importerInfo.displayName) {
                    return record.data.importerInfo.displayName;
                }
                return null;
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
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/fir/importservices',
        reader: {
            type: 'json'
        }
    }
});