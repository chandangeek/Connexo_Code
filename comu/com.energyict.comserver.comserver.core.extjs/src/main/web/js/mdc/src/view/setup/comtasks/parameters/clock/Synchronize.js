Ext.define('Mdc.view.setup.comtasks.parameters.clock.Synchronize', {
    extend: 'Ext.form.Panel',
    requires: [
        'Mdc.view.setup.comtasks.parameters.TimeCombo'
    ],
    alias: 'widget.communication-tasks-parameters-clock-synchronize',
    name: 'parameters',
    items: [
        {
            xtype: 'container',
            layout: 'column',
            margin: '0 0 5 0',
            items: [
                {
                    xtype: 'textfield',
                    itemId: 'syncMinNum',
                    name: 'minimumclockdifference',
                    fieldLabel: Uni.I18n.translate('comtask.minimum.clock.difference','MDC','Minimum clock difference'),
                    labelWidth: 197,
                    width: 247,
                    maskRe: /[0-9]+/,
                    margin: '0 10 0 0',
                    value: 5,
                    labelPad: 18
                },
                {
                    xtype: 'communication-tasks-parameters-timecombo',
                    itemId: 'syncMinTime',
                    value: 'seconds'
                }
            ]
        },
        {
            xtype: 'container',
            layout: 'column',
            margin: '0 0 5 0',
            items: [
                {
                    xtype: 'textfield',
                    itemId: 'syncMaxNum',
                    name: 'maximumclockdifference',
                    fieldLabel: Uni.I18n.translate('comtask.maximum.clock.difference','MDC','Maximum clock difference'),
                    labelWidth: 197,
                    width: 247,
                    maskRe: /[0-9]+/,
                    margin: '0 10 0 0',
                    value: 1,
                    labelPad: 18
                },
                {
                    xtype: 'communication-tasks-parameters-timecombo',
                    itemId: 'syncMaxTime',
                    value: 'hours'
                }
            ]
        },
        {
            xtype: 'container',
            layout: 'column',
            items: [
                {
                    xtype: 'textfield',
                    itemId: 'syncMaxNumShift',
                    name: 'maximumclockshift',
                    fieldLabel: Uni.I18n.translate('comtask.maximum.clock.shift','MDC','Maximum clock shift'),
                    labelWidth: 197,
                    width: 247,
                    maskRe: /[0-9]+/,
                    margin: '0 10 0 0',
                    value: 1,
                    labelPad: 18
                },
                {
                    xtype: 'communication-tasks-parameters-timecombo',
                    itemId: 'syncMaxTimeShift',
                    value: 'minutes'
                }
            ]
        }
    ]
});