/**
 * @class Uni.view.search.Adapter
 */
Ext.define('Uni.view.search.Adapter', {
    extend: 'Ext.container.Container',
    xtype: 'uni-view-search-adapter',

    mixins: [
        'Uni.grid.filtertop.Base'
    ],

    layout: {
        type: 'hbox'
    },

    /**
     * Widget that needs to be wrapped.
     */
    widget: undefined,

    /**
     * Search property to be wrapped.
     */
    property: undefined,

    initComponent: function () {
        var me = this;

        if (Ext.isDefined(me.widget)) {
            me.property = me.widget.property;

            me.active = me.widget.active;
            me.dataIndex = me.widget.dataIndex;
            me.applyParamValue = me.widget.applyParamValue;

            me.defaults = {
                margin: 0
            };

            me.items = [
                me.widget,
                {
                    margin: '0 0 0 2',
                    xtype: 'button',
                    iconCls: ' icon-close2',
                    action: 'remove'
                }
            ];
        }

        me.callParent(arguments);

        if (Ext.isDefined(me.widget)) {
            me.down('button[action=remove]').on('click', me.removeHandler, me);
        }
    },

    getFilterValue: function () {
        return this.widget.getFilterValue();
    },

    setFilterValue: function (data) {
        this.widget.setFilterValue(data);
    },

    resetValue: function () {
        this.widget.resetValue();
    },

    getParamValue: function () {
        return this.widget.getParamValue();
    }
});

