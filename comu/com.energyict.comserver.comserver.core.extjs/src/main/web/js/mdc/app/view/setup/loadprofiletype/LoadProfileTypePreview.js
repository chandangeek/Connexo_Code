Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypePreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.loadProfileTypePreview',
    itemId: 'loadProfileTypePreview',
    requires: [
        'Mdc.view.setup.loadprofiletype.LoadProfileTypeActionMenu'
    ],
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
            items: [
                {
                    columnWidth: .4,
                    items: [
                        {
                            xtype: 'displayfield',
                            fieldLabel: 'Name',
                            name: 'name'
                        },
                        {
                            xtype: 'displayfield',
                            fieldLabel: 'OBIS code',
                            name: 'obisCode'
                        },
                        {
                            xtype: 'displayfield',
                            fieldLabel: 'Interval',
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
                    columnWidth: .6,
                    xtype: 'displayfield',
                    fieldLabel: 'Measurement types',
                    labelWidth: 200,
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
    ],

    initComponent: function () {
        var me = this;
        this.tools = [
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
                iconCls: 'x-uni-action-iconD',
                menu: {
                    xtype: 'load-profile-type-action-menu'
                }
            }
        ];
        this.callParent(arguments);
    }

});


