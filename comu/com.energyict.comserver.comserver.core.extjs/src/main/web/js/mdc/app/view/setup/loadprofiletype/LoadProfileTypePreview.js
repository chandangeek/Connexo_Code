Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypePreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.loadProfileTypePreview',
    itemId: 'loadProfileTypePreview',
    height: 310,
    frame: true,
    intervalStore: null,
    editActionName: null,
    deleteActionName: null,

    items: [
        {
            xtype: 'form',
            name: 'loadProfileTypeDetails',
            layout: 'column',
            defaults: {
                xtype: 'container',
                layout: 'form',
                columnWidth: 0.5
            },
            items: [
                {
                    items: [
                        {
                            xtype: 'displayfield',
                            fieldLabel: 'Name: ',
                            name: 'name'
                        },
                        {
                            xtype: 'displayfield',
                            fieldLabel: 'OBIS code: ',
                            name: 'obisCode'
                        },
                        {
                            xtype: 'displayfield',
                            fieldLabel: 'Interval: ',
                            name: 'timeDuration',
                            renderer: function (value) {
                                var intervalRecord = this.up('#loadProfileTypePreview').intervalStore.findRecord('id', value.id);
                                if (!Ext.isEmpty(intervalRecord)) {
                                    return intervalRecord.getData().name;
                                }
                            }
                        }
                    ]
                },
                {
                    items: [
                        {
                            xtype: 'displayfield',
                            fieldLabel: 'Measurment Types: ',
                            name: 'measurementTypes',
                            renderer: function (value) {
                                var typesString = '';
                                if (!Ext.isEmpty(value)) {
                                    Ext.each(value, function (type) {
                                        typesString += type.name + '<br />';
                                    });
                                }
                                return typesString;
                            }
                        }
                    ]
                }
            ]
        }
    ],

    initComponent: function () {
        this.tools = [
            {
                xtype: 'button',
                text: 'Actions',
                iconCls: 'x-uni-action-iconA',
                menu: {
                    xtype: 'menu',
                    plain: true,
                    border: false,
                    shadow: false,
                    items: [
                        {
                            text: 'Edit',
                            action: this.editActionName
                        },
                        {
                            text: 'Remove',
                            action: this.deleteActionName
                        }
                    ]
                }
            }
        ];
        this.callParent(arguments);
    }

});


