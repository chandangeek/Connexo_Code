/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comtasks.parameters.time.Set', {
    extend: 'Ext.form.Panel',
    requires: [
        'Mdc.view.setup.comtasks.parameters.TimeCombo'
    ],
    alias: 'widget.communication-tasks-parameters-clock-set',
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
                    itemId: 'setMinNum',
                    name: 'minimumclockdifference',
                    maskRe: /[0-9]+/,
                    margin: '0 10 0 0',
                    value: 5,
                    flex: 1
                },
                {
                    xtype: 'communication-tasks-parameters-timecombo',
                    itemId: 'setMinTime',
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
            items: [
                {
                    xtype: 'textfield',
                    itemId: 'setMaxNum',
                    name: 'maximumclockdifference',
                    maskRe: /[0-9]+/,
                    margin: '0 10 0 0',
                    value: 1,
                    flex: 1
                },
                {
                    xtype: 'communication-tasks-parameters-timecombo',
                    itemId: 'setMaxTime',
                    value: 'hours',
                    flex: 3
                }
            ]
        }
    ]
});