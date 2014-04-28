Ext.define('Skyline.panel.StepPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.step-panel',
    layout: {
        type: 'hbox',
        align: 'left'
    },

    initComponent: function () {
        var me = this;
        me.add([
            {
                name: 'step-button-side',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        name: 'topdots',
                        cls: 'x-panel-step-dots'
                    },
                    {
                        xtype: 'step-button',
                        text: '43'
                    },
                    {
                        name: 'bottomdots'
                    }
                ]

            },
            {
                name: 'step-label-side',
                layout: {
                    type: 'vbox',
                    align: 'left'
                }
            }
        ]);
        me.callParent(arguments)
    }

});
