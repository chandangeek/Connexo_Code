# Development Guidelines

* Avoid the use of "FieldContainer" xtype with and non empty fieldlabel

Reason: "fieldlabel" is not aligned properly

Solution: Use multiple components instead of wrapping them in a "FieldContainer".
Set the "fieldlabel" on the first component and set the fieldlabels of the other components to '&nbsp'.

Example:

                {
                    xtype: 'container',
                    layout: 'vbox',
                    itemId: 'readingTypesArea',
                    items: [
                        {
                            xtype: 'displayfield',
                            fieldLabel: 'Reading types',
                            value: ...
                        },
                        {
                            xtype: 'displayfield',
                            fieldLabel: '&nbsp',
                            value: ...
                        },
                        {
                            xtype: 'displayfield',
                            fieldLabel: '&nbsp',
                            value: ...
                        }

                    ]
                }


instead of


                 xtype: 'fieldcontainer',
                 fieldLabel: 'Reading types',
                 layout: 'vbox',
                items: [{
                                xtype: 'displayfield',
                                fieldLabel: '&nbsp',
                                value: ...
                        }, {
                                xtype: 'displayfield',
                                fieldLabel: '&nbsp',
                                value: ...
                        }, {
                                xtype: 'displayfield',
                                fieldLabel: '&nbsp',
                                value: ...
                        }
                ]