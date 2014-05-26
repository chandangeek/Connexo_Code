Ext.define('Mdc.view.setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailChannelPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.loadProfileConfigurationDetailChannelPreview',
    itemId: 'loadProfileConfigurationDetailChannelPreview',
    height: 310,
    frame: true,
    editActionName: null,
    deleteActionName: null,

    items: [
        {
            xtype: 'form',
            name: 'loadProfileConfigurationChannelDetailsForm',
            itemId: 'loadProfileConfigurationChannelDetailsForm',
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
                            fieldLabel: 'Measurement type: ',
                            name: 'name'
                        },
                        {
                            xtype: 'displayfield',
                            fieldLabel: 'OBIS code: ',
                            name: 'overruledObisCode'
                        },
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: 'CIM reading type',
                            layout: 'column',
                            defaults: {
                                columnWidth: 0.5
                            },
                            items: [
                                {
                                    xtype: 'displayfield',
                                    name: 'measurementType',
                                    renderer: function (value) {
                                        var readingType = value.readingType;
                                        if (!Ext.isEmpty(readingType)) {
                                            return value.readingType.mrid;
                                        }
                                    }
                                },
                                {
                                    xtype: 'button',
                                    icon: '../mdc/resources/images/information.png',
                                    cls: 'uni-btn-transparent',
                                    handler: function (item, test) {
                                        var record = this.up('#loadProfileConfigurationChannelDetailsForm').form.getRecord();
                                        this.fireEvent('showReadingTypeInfo', record.getMeasurementType());
                                    },
                                    itemId: 'channelsReadingTypeBtn'
                                }
                            ]
                        },
                        {
                            xtype: 'displayfield',
                            fieldLabel: 'Multiplier: ',
                            name: 'multiplier'
                        }
                    ]
                },
                {
                    items: [
                        {
                            xtype: 'displayfield',
                            fieldLabel: 'Overflow value: ',
                            name: 'overflowValue'

                        },
                        {
                            xtype: 'displayfield',
                            fieldLabel: 'Unit of measure: ',
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