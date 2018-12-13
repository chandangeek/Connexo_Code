/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.store.task.Priorities', {
    extend: 'Ext.data.Store',
    model: 'Bpm.model.task.Priority',
    proxy: {
        type: 'memory'
    },

    data:  [
        {
            label: Uni.I18n.translate('bpm.task.priority.high', 'BPM', 'High'),
            name: 0
        },
        {
            label: Uni.I18n.translate('bpm.task.priority.high', 'BPM', 'High'),
            name: 1
        },
        {
            label: Uni.I18n.translate('bpm.task.priority.high', 'BPM', 'High'),
            name: 2
        },
        {
            label: Uni.I18n.translate('bpm.task.priority.high', 'BPM', 'High'),
            name: 3
        },
        {
            label: Uni.I18n.translate('bpm.task.priority.high', 'BPM', 'High'),
            name: 4
        },
        {
            label: Uni.I18n.translate('bpm.task.priority.medium', 'BPM', 'Medium'),
            name: 5
        },
        {
            label: Uni.I18n.translate('bpm.task.priority.medium', 'BPM', 'Medium'),
            name: 6
        },
        {
            label: Uni.I18n.translate('bpm.task.priority.medium', 'BPM', 'Medium'),
            name: 7
        },
        {
            label: Uni.I18n.translate('bpm.task.priority.low', 'BPM', 'Low'),
            name: 8
        },
        {
            label: Uni.I18n.translate('bpm.task.priority.low', 'BPM', 'Low'),
            name: 9
        },
        {
            label: Uni.I18n.translate('bpm.task.priority.low', 'BPM', 'Low'),
            name: 10
        }
    ]
});
