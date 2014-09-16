Ext.define('Mdc.view.setup.comtasks.parameters.clock.Set', {
    extend: 'Ext.form.Panel',
    requires: [
        'Mdc.view.setup.comtasks.parameters.TimeCombo'
    ],
    alias: 'widget.communication-tasks-parameters-clock-set',
    name: 'parameters',
    items: [
        {
            xtype: 'container',
            layout: 'column',
            margin: '0 0 5 0',
            items: [
                {
                    xtype: 'textfield',
                    itemId: 'setMinNum',
                    name: 'minimumclockdifference',
                    fieldLabel: 'Minimum clock difference',
                    labelWidth: 185,
                    width: 235,
                    maskRe: /[0-9]+/,
                    margin: '0 10 0 0',
                    value: 5,
                    labelPad: 18
                },
                {
                    xtype: 'communication-tasks-parameters-timecombo',
                    itemId: 'setMinTime',
                    value: 'seconds'
                }
            ]
        },
        {
            xtype: 'container',
            layout: 'column',
            items: [
                {
                    xtype: 'textfield',
                    itemId: 'setMaxNum',
                    name: 'maximumclockdifference',
                    fieldLabel: 'Maximum clock difference',
                    labelWidth: 185,
                    width: 235,
                    maskRe: /[0-9]+/,
                    margin: '0 10 0 0',
                    value: 1,
                    labelPad: 18
                },
                {
                    xtype: 'communication-tasks-parameters-timecombo',
                    itemId: 'setMaxTime',
                    value: 'hours'
                }
            ]
        }
    ]
});