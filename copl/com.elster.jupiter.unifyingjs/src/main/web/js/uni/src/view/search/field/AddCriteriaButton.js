Ext.define('Uni.view.search.field.AddCriteriaButton', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.uni-view-search-field-add-criteria-button',
    xtype: 'uni-view-search-field-add-criteria-button',
    layout: 'hbox',
    requires: [
        'Uni.store.search.Removables'
        ],
    width: 120,

    initComponent: function () {
        var me = this;
        var store = Ext.getStore('Uni.store.search.Removables');
        var tooltipMessage = 'Connection properties become available as soon as a search value has been specified for Device type, Device configuration and Connection properties.';
        var menu = Ext.create('Ext.menu.Menu', {
            id: 'mainMenu',
            plain: true,
            style: {
                overflow: 'visible',
                arrowAlign: 'left'
            },
            arrowAlign: 'left',
            items: [
                {
                    text: 'Batch',
                    checked: false,
                    //checkHandler: onItemCheck
                },
                {
                    text: 'Has open data collection issues',
                    checked: false
                },
                {
                    text: 'Service category',
                    checked: false
                },
                {
                    text: 'Shared schedule name',
                    checked: false
                },
                {
                    text: 'Usage point',
                    checked: false
                },
                {
                    text: 'Year of certification',
                    checked: false
                },
                {
                    arrowAlign: 'left',
                    text: 'Load profile',
                    menu: {
                        items: [
                            {
                                text: 'Last reading',
                                checked: false,
                                //checkHandler: onItemCheck
                            }, {
                                text: 'Interval',
                                checked: false,
                                //checkHandler: onItemCheck
                            }, {
                                text: 'OBIS code',
                                checked: false,
                                //checkHandler: onItemCheck
                            }, {
                                text: 'Name',
                                checked: false,
                                //checkHandler: onItemCheck
                            }
                        ]
                    }
                },
                {
                    text: 'Communication task',
                    disabled: true,
                    tooltip: {

                        title: 'Enable connection properties',
                        text: tooltipMessage,
                        maxWidth: 150
                    },

                    menu: {
                        items: [
                            {
                                text: 'Last reading',
                                checked: false,
                                //checkHandler: onItemCheck
                            }, {
                                text: 'Interval',
                                checked: false,
                                disabled: true
                                //checkHandler: onItemCheck
                            }, {
                                text: 'OBIS code',
                                checked: false,
                                //checkHandler: onItemCheck
                            }, {
                                text: 'Name',
                                checked: false,
                                //checkHandler: onItemCheck
                            }
                        ]
                    }
                }]
        });
        this.items = [
            {
                xtype: 'button',
                style: {
                    'background-color': '#71adc7'
                },
                itemId: 'addcriteria',
                text: Uni.I18n.translate('search.overview.addCriteria.emptyText', 'UNI', 'Add criteria'),
                arrowAlign: 'right',
                menuAlign: 'tr-br',
                menu: menu,
                setValue: function(value) {
                    var item = this.menu.items.findBy(function(item){return item.value == value});
                    if (item) {
                        item.setActive();
                        this.setText(item.text);
                        this.fireEvent('change', this);
                    }

                }
            }
        ];

        this.callParent(arguments);

        var button = me.down('#addcriteria');
        Ext.suspendLayouts();

        store.load(function () {
            var menu = button.menu;
            //menu.removeAll();
            store.each(function (item) {
                menu.add({
                    text: item.get('displayValue'),
                    checked: false,
                    value: item.get('name')
                })
            });
        });
        Ext.resumeLayouts(true);
    }
});

