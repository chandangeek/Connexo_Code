/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.model.MetrologyConfiguration', {
    extend: 'Uni.model.Version',
    requires: [
        'Imt.metrologyconfiguration.model.MetrologyContract',
        'Imt.metrologyconfiguration.model.ReadingTypeDeliverable'
    ],
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'description', type: 'string'},
        {name: 'status', type: 'auto', defaultValue: null},
        {name: 'serviceCategory', type: 'auto', defaultValue: null},
        {name: 'meterRoles', type: 'auto', defaultValue: null},
        {name: 'purposes', type: 'auto', defaultValue: null},
        {name: 'usagePointRequirements', type: 'auto', defaultValue: null},
        {name: 'customPropertySets', type: 'auto', defaultValue: null, useNull: true}
    ],

    associations: [
        {
            name: 'metrologyContracts',
            type: 'hasMany',
            model: 'Imt.metrologyconfiguration.model.MetrologyContract',
            associationKey: 'metrologyContracts'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/ucr/metrologyconfigurations',
        reader: {
            type: 'json'
        }
    },

    getReadingTypeDeliverablesStore: function () {
        var me = this,
            data = [],
            store;

        me.metrologyContracts().each(function (metrologyContract) {
            var metrologyContractName = metrologyContract.get('name'),
                metrologyContractIsMandatory = metrologyContract.get('mandatory');

            metrologyContract.readingTypeDeliverables().each(function (readingTypeDeliverable) {
                data.push(Ext.merge(readingTypeDeliverable.getProxy().getWriter().getRecordData(readingTypeDeliverable), {
                    metrologyContract: metrologyContractName,
                    metrologyContractIsMandatory: metrologyContractIsMandatory
                }));
            });
        });

        store = Ext.create('Ext.data.Store', {
            model: 'Imt.metrologyconfiguration.model.ReadingTypeDeliverable',
            groupField: 'metrologyContract'
        });

        store.loadRawData(data);
        store.totalCount = data.length;

        return store;
    }
});