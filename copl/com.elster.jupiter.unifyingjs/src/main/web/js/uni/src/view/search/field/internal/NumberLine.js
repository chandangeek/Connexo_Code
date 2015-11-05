Ext.define('Uni.view.search.field.internal.NumberLine', {
    extend: 'Ext.panel.Panel',
    xtype: 'uni-search-internal-numberline',
    width: '455',
    layout: 'hbox',

    requires: [
        'Uni.view.search.field.internal.Operator',
        'Uni.model.search.Value'
    ],

    defaults: {
        margin: '0 10 0 0'
    },

    removable: false,

    getValue: function() {
        var value = this.down('#filter-input').getValue();

        return value ? Ext.create('Uni.model.search.Value', {
            operator: this.down('#filter-operator').getValue(),
            criteria: this.down('#filter-input').getValue()
        }) : null
    },

    reset: function() {
        this.down('#filter-operator').reset();
        this.down('#filter-input').reset();
        this.fireEvent('reset', this);
    },

    onChange: function() {
        this.fireEvent('change', this, this.getValue());
    },

    onRemove: Ext.emptyFn,

    initComponent: function () {
        var me = this;

        me.addEvents(
            "change",
            "reset"
        );

        me.items = [
            {
                itemId: 'filter-operator',
                xtype: 'uni-search-internal-operator',
                value: '==',
                margin: '0 5 0 0',
                operators: ['==', '!=', '>', '>=', '<', '<='],
                listeners: {
                    change: {
                        fn: me.onChange,
                        scope: me
                    }
                }
            },
            {
                xtype: 'textfield',
                itemId: 'filter-input',
                width: 180,
                margin: '0 5 0 0',
                listeners: {
                    change:{
                        fn: me.onChange,
                        scope: me
                    }
                }
            }
        ];

        if (me.removable) {
            me.rbar = {
                width: 15,
                items: {
                    xtype: 'button',
                    itemId: 'filter-clear',
                    ui: 'plain',
                    tooltip: Uni.I18n.translate('search.field.remove', 'UNI', 'Remove filter'),
                    iconCls: ' icon-close4',
                    margin: '0 10 0 0',
                    hidden: true,
                    style: {
                        fontSize: '16px'
                    },
                    handler: me.onRemove,
                    scope: me
                }
            };
        }


        me.callParent(arguments);

        if (me.removable) {
            me.on('render', function() {
                var button = me.down('#filter-clear');
                me.getEl().on('mouseover', function () {
                    button.setVisible(true);
                });
                me.getEl().on('mouseout', function () {
                    button.setVisible(false);
                });
            });
        }
    }
});