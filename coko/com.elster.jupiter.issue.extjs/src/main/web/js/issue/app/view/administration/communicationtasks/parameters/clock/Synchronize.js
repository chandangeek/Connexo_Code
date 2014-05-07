Ext.define('Isu.view.administration.communicationtasks.parameters.clock.Synchronize', {
    extend: 'Ext.form.Panel',
    requires: [
        'Isu.view.administration.communicationtasks.parameters.TimeCombo'
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
                    name: 'minimumclockdifference',
                    fieldLabel: 'Minimum clock difference',
                    labelWidth: 200,
                    width: 240,
                    margin: '0 20 0 0'
                },
                {
                    xtype: 'communication-tasks-parameters-timecombo'
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
                    name: 'maximumclockdifference',
                    fieldLabel: 'Maximum clock difference',
                    labelWidth: 200,
                    width: 240,
                    margin: '0 20 0 0'
                },
                {
                    xtype: 'communication-tasks-parameters-timecombo'
                }
            ]
        },
        {
            xtype: 'container',
            layout: 'column',
            items: [
                {
                    xtype: 'textfield',
                    name: 'maximumclockshift',
                    fieldLabel: 'Maximum clock shift',
                    labelWidth: 200,
                    width: 240,
                    margin: '0 20 0 0'
                },
                {
                    xtype: 'communication-tasks-parameters-timecombo'
                }
            ]
        }
    ],

    loadParams: function (params) {

    },

    getParams: function () {
        var params;

        return params;
    }
});