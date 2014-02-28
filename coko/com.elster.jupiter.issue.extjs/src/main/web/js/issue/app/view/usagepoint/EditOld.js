Ext.define('Mtr.view.usagepoint.EditOld', {
    extend: 'Ext.window.Window',
    alias: 'widget.usagePointEditOld',
    title: 'Edit Usage Point',
    layout: 'fit',
    modal: true,
    constrain: true,
    autoShow: true,
    initComponent: function () {
        this.items = [
            {
                xtype: 'form',
                items: [
                    {
                        xtype: 'textfield',
                        name: 'id',
                        fieldLabel: 'Id'
                    },
                    {
                        xtype: 'textfield',
                        name: 'mRID',
                        fieldLabel: 'mRID'
                    },
                    {
                        xtype: 'textfield',
                        name: 'serviceCategory',
                        fieldLabel: 'Service Category'
                    },
                    {
                        xtype: 'textfield',
                        name: 'phaseCode',
                        fieldLabel: 'Phase Code'
                    },
                    {
                        xtype: 'textfield',
                        name: 'ratedPowerValue',
                        fieldLabel: 'Rated Power (kW)'
                    }
                ]
            }
        ];
        this.buttons = [
            {
                text: 'Clone',
                action: 'clone'
            },
            {
                text: 'Save',
                action: 'save'
            },
            {
                text: 'Cancel',
                scope: this,
                handler: this.close
            }
        ];
        this.callParent(arguments);
    }
});

