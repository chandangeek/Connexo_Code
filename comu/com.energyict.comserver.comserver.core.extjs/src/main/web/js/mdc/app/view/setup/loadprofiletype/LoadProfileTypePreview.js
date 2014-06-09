Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypePreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.loadProfileTypePreview',
    itemId: 'loadProfileTypePreview',
    requires: [
        'Mdc.view.setup.loadprofiletype.LoadProfileTypeActionMenu'
    ],
    title: '',
    frame: true,
    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'load-profile-type-action-menu'
            }
        }
    ],
    items: {
        xtype: 'form',
        layout: 'column',
        defaults: {
            xtype: 'container',
            layout: 'form',
            columnWidth: 0.5
        },
        items: [
            {
                defaults: {
                    xtype: 'displayfield'
                },
                items: [
                    {
                        fieldLabel: 'Name',
                        name: 'name'
                    },
                    {
                        fieldLabel: 'OBIS code',
                        name: 'obisCode'
                    },
                    {
                        fieldLabel: 'Interval',
                        name: 'timeDuration',
                        renderer: function (value) {
                            var intervalRecord = value ? Ext.getStore('Mdc.store.Intervals').getById(value.id) : null;

                            return intervalRecord ? intervalRecord.get('name') : '';
                        }
                    }
                ]
            },
            {
                defaults: {
                    xtype: 'displayfield'
                },
                items: [
                    {
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
        ]
    }
});


