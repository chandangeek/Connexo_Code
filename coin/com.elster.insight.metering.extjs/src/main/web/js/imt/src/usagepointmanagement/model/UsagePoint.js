/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.model.UsagePoint', {
    extend: 'Ext.data.Model',
    requires: [
        'Imt.customattributesonvaluesobjects.model.AttributeSetOnUsagePoint',
        'Imt.util.UsagePointType'
    ],
    idProperty: 'name',
    fields: [
        {name: 'id', type: 'int'},
        {name: 'mRID', type: 'string'},
        {name: 'serviceCategory', type: 'string'},
        {name: 'displayServiceCategory', type: 'auto', useNull: true, persist: false},
        {name: 'lifeCycle', type: 'auto', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'installationTime', type: 'int', defaultValue: null, useNull: true},
        {name: 'createTime', type: 'int', defaultValue: null, useNull: true},
        {name: 'lastTransitionTime', type: 'int', defaultValue: null, useNull: true},
        {name: 'isReadyForLinkingMC', type: 'boolean', defaultValue: true},
        {name: 'state', persist: false},
        {
            name: 'calendars',
            type: 'auto',
            defaultValue: null,
            useNull: true
        },
        {
            name: 'extendedGeoCoordinates',
            type: 'auto'
        },
        {
            name: 'extendedLocation',
            type: 'auto'
        },
        {name: 'version', type: 'int'},
        {name: 'purposes', type: 'auto', useNull: true, persist: false},
        {
            name: 'typeOfUsagePoint',
            persist: false,
            mapping: function () {
                return Imt.util.UsagePointType.mapping.apply(this, arguments);
            },
            // workaround for broken functionality of 'Ext.data.Field.serialize' in 'Uni.override.JsonWriterOverride.getRecordData'
            convert: function () {
                return Imt.util.UsagePointType.convert.apply(this, arguments);
            }
        },
        {name: 'isSdp', type: 'boolean', useNull: true},
        {name: 'isVirtual', type: 'boolean', useNull: true},
        {name: 'readRoute', type: 'string'},
        {name: 'connectionState', type: 'auto', defaultValue: null},
        {name: 'servicePriority', type: 'string'},
        {name: 'serviceDeliveryRemark', type: 'string'},
        {name: 'techInfo', type: 'auto', defaultValue: {}},
        {name: 'metrologyConfiguration', type: 'auto', defaultValue: null},
        {name: 'effectiveMetrologyConfiguration', type: 'auto', defaultValue: null},
        {name: 'meterRoles', type: 'auto', defaultValue: null},
        {name: 'hasEffectiveMCs', type: 'boolean', defaultValue: false},
        {name: 'meterActivations', type: 'auto', defaultValue: null},
        {
            name: 'metrologyConfiguration_id',
            persist: false,
            mapping: 'metrologyConfiguration.id'
        },
        {
            name: 'metrologyConfiguration_activationTime',
            persist: false,
            type: 'date',
            dateFormat: 'time',
            mapping: 'metrologyConfiguration.activationTime'
        },
        {
            name: 'metrologyConfiguration_name',
            persist: false,
            mapping: 'metrologyConfiguration.name'
        },
        {
            name: 'metrologyConfiguration_status',
            persist: false,
            mapping: 'metrologyConfiguration.status'
        },
        {
            name: 'metrologyConfiguration_meterRoles',
            persist: false,
            mapping: 'metrologyConfiguration.meterRoles'
        },
        {
            name: 'metrologyConfiguration_purposes',
            persist: false,
            mapping: 'metrologyConfiguration.purposes'
        },
        {
            // needed for adding life cycle transition during usage point creation
            name: 'transitionToPerform',
            type: 'auto',
            defaultValue: null
        }
    ],

    associations: [
        {
            name: 'customPropertySets',
            associationKey: 'customPropertySets',
            type: 'hasMany',
            model: 'Imt.customattributesonvaluesobjects.model.AttributeSetOnUsagePoint'
        }
    ],

    activateMeters: function (callback, failure) {
        var me = this;

        Ext.Ajax.request({
            url: '../../api/udr/usagepoints/' + encodeURIComponent(me.get('name')) + '/activatemeters',
            method: 'PUT',
            jsonData: Ext.JSON.encode(me.getProxy().getWriter().getRecordData(me)),
            success: callback,
            failure: failure
        });
    },

    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/',
        timeout: 240000,
        reader: {
            type: 'json'
        }
    },

    unlinkMetrologyConfiguration: function (options) {
        var me = this,
            url = me.getProxy().url + me.get('name') + '/unlinkmetrologyconfiguration';

        Ext.Ajax.request(Ext.Object.merge(
            {
                url: url,
                method: 'PUT',
                jsonData: me.getProxy().getWriter().getRecordData(me)
            }
            , options));
    }
});