Ext.define('CSMonitor.view.logging.Criteria', {
    extend: 'Ext.panel.Panel',
    xtype: 'criteria',
    border: false,
    layout: {
        type: 'hbox',
        align: 'stretch'
    },
    defaults: { flex: 1, margins: '15, 15, 15, 15' }, // top, right, bottom, left
    items: [
        {
            xtype: 'container',
            itemId: 'criteriaContainer',
            layout: {
                type: 'vbox',
                align: 'center'
            },
            border: true,
            style: {
                borderColor: 'white',
                borderStyle: 'solid'
            },
            items: [
                {
                    xtype: 'component',
                    html: '<b>Selection criteria</b>',
                    margins: '5 0 15 0'
                },
                {
                    xtype: 'textfield',
                    name: 'deviceId',
                    labelWidth: 150,
                    labelAlign: 'right',
                    fieldLabel: 'Device id'
                },
                {
                    xtype: 'textfield',
                    name: 'comportId',
                    labelWidth: 150,
                    labelAlign: 'right',
                    fieldLabel: 'Communication port id'
                },
                {
                    xtype: 'textfield',
                    name: 'connectionId',
                    fieldLabel: 'Connection id',
                    labelWidth: 150,
                    labelAlign: 'right'
                }
            ]
        },
        {
            xtype: 'container'
        }
    ],

    warnForEmptyCriteria: function() {
        var me = this,
            taskSetWhiteAgain = new Ext.util.DelayedTask(function() {
                me.setBorderColor('white');
            });
        this.setBorderColor('red');
        taskSetWhiteAgain.delay(1000);
    },

    setBorderColor: function(color) {
        this.down('#criteriaContainer').getEl().setStyle('borderColor', color);
    }

});