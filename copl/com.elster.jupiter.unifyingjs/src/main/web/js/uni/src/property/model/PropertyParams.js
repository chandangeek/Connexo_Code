/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.property.model.PropertyParams
 *
 * Additional parameters can be added for any property, if needed.
 */
Ext.define('Uni.property.model.PropertyParams', {
    extend: 'Ext.data.Model',
    fields: [
        // Additional parameter for Imt.processes.view.AvailableTransitions property
        {
            name: 'toStage',
            persist: false
        }
    ]
});
