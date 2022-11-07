/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comtasks.parameters.time.Synchronize', {
    extend: 'Ext.form.Panel',
    requires: [
        'Mdc.view.setup.comtasks.parameters.TimeCombo'
    ],
    alias: 'widget.communication-tasks-parameters-clock-synchronize',
    name: 'parameters',
    items: [
        {
            xtype: 'fieldcontainer',
            fieldLabel: Uni.I18n.translate('comtask.minimum.clock.difference','MDC','Minimum clock difference'),
            itemId: 'mdc-minimumClockDiff',
            msgTarget: 'under',
            labelWidth: 300,
            width: 500,
            layout: 'hbox',
            margin: '0 0 5 0',
            items: [
                {
                    xtype: 'textfield',
                    itemId: 'syncMinNum',
                    name: 'minimumclockdifference',
                    maskRe: /[0-9]+/,
                    margin: '0 10 0 0',
                    value: 5,
                    flex: 1
                },
                {
                    xtype: 'communication-tasks-parameters-timecombo',
                    itemId: 'syncMinTime',
                    value: 'seconds',
                    flex: 3
                }
            ]
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: Uni.I18n.translate('comtask.maximum.clock.difference','MDC','Maximum clock difference'),
            itemId: 'mdc-maximumClockDiff',
            msgTarget: 'under',
            labelWidth: 300,
            width: 500,
            layout: 'hbox',
            margin: '0 0 5 0',
            items: [
                {
                    xtype: 'textfield',
                    itemId: 'syncMaxNum',
                    name: 'maximumclockdifference',
                    maskRe: /[0-9]+/,
                    margin: '0 10 0 0',
                    value: 1,
                    flex: 1
                },
                {
                    xtype: 'communication-tasks-parameters-timecombo',
                    itemId: 'syncMaxTime',
                    value: 'hours',
                    flex: 3
                }
            ]
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: Uni.I18n.translate('comtask.maximum.clock.shift','MDC','Maximum clock shift'),
            itemId: 'mdc-maximumClockShift',
            msgTarget: 'under',
            labelWidth: 300,
            width: 500,
            layout: 'hbox',
            items: [
                {
                    xtype: 'textfield',
                    itemId: 'syncMaxNumShift',
                    name: 'maximumclockshift',
                    maskRe: /[0-9]+/,
                    margin: '0 10 0 0',
                    value: 1,
                    flex: 1
                },
                {
                    xtype: 'communication-tasks-parameters-timecombo',
                    itemId: 'syncMaxTimeShift',
                    value: 'minutes',
                    flex: 3
                }
            ]
        }
    ]
});