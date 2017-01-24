Ext.define('CSMonitor.view.logging.Criteria', {
    extend: 'Ext.form.Panel',
    xtype: 'criteria',
    border: false,
    items: [
        {
            xtype:'fieldset',
            itemId: 'criteriaContainer',
            columnWidth: 0.5,
            title: '<h3>Filter</h3>',
            collapsible: false,
            defaultType: 'textfield',
            defaults: {anchor: '100% 100%'},
            border: true,
            style: {
                borderColor: 'white',
                borderStyle: 'solid'
            },
            defaults: {
                labelWidth: 200,
                width:800
            },
            items: [
                {
                    name: 'deviceName',
                    itemId: 'deviceName',
                    fieldLabel: 'Device name'
                },
                {
                    name: 'comportName',
                    itemId: 'comPortName',
                    fieldLabel: 'Communication port name'
                }
            ]
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