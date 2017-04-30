/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.rulesets.model.MetrologyConfigurationPurpose', {
    extend: 'Uni.model.Version',
    fields: ['active', 'name', 'metrologyConfigurationInfo', 'outputs', 'lifeCycleStates',
        {
            name: 'lifeCycleStatesCount',
            persist: false,
            mapping: function (data) {
                return  data.lifeCycleStates && data.lifeCycleStates.length ? data.lifeCycleStates.length : Uni.I18n.translate('general.all', 'IMT', 'All');
            }
        }]
});