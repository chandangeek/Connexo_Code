/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.estimationtasks.store.EstimationTasks', {
    extend: 'Ext.data.Store',
    requires: ['Est.estimationtasks.model.EstimationTask'],
    model: 'Est.estimationtasks.model.EstimationTask',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/est/estimation/tasks',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'estimationTasks'
        }
    }
});
