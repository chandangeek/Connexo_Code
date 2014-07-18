Ext.define('Mdc.view.setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailChannelPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.loadProfileConfigurationDetailChannelPreview',
    itemId: 'loadProfileConfigurationDetailChannelPreview',
    height: 310,
    frame: true,
    editActionName: null,
    deleteActionName: null,
    requires: [
        'Uni.form.field.ObisDisplay',
        'Uni.grid.column.ReadingType'
    ],
    items: [
        {
            xtype: 'form',
            name: 'loadProfileConfigurationChannelDetailsForm',
            itemId: 'loadProfileConfigurationChannelDetailsForm',
            layout: 'column',
            defaults: {
                xtype: 'container',
                layout: 'form'

            },
            items: [
                {
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 200
                    },
                    columnWidth: 0.6,
                    items: [
                        {
                            fieldLabel: 'Measurement type',
                            name: 'name'
                        },
                        {
                            xtype: 'reading-type-displayfield',
                            fieldLabel: 'CIM reading type',
                            name: 'readingType'
                        },
                        {
                            xtype: 'obis-displayfield',
                            name: 'overruledObisCode'
                        },
                        {
                            fieldLabel: 'Multiplier',
                            name: 'multiplier'
                        }
                    ]
                },
                {
                    columnWidth: 0.4,
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 200
                    },
                    items: [
                        {

                            fieldLabel: 'Overflow value',
                            name: 'overflowValue'

                        },
                        {
                            fieldLabel: 'Unit of measure',
                            name: 'unitOfMeasure',
                            renderer: function (value) {
                                return value.name;
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
                iconCls: 'x-uni-action-iconD',
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