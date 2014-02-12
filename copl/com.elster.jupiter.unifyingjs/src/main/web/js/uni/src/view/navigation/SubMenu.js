/**
 * @class Uni.view.navigation.SubMenu
 */
Ext.define('Uni.view.navigation.SubMenu', {
    extend: 'Ext.container.Container',
    alias: 'widget.navigationSubMenu',

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    defaults: {
        xtype: 'button',
        enableToggle: true,
        allowDepress: false,
        hrefTarget: '_self',
        tooltipType: 'title',
        scale: 'large'
    },

    items: [
    ],

    initComponent: function () {
        this.defaults.toggleGroup = 'submenu-' + this.getId();

        this.callParent(this);
    },

    toggleMenuItem: function (index) {
        this.items.items[index].toggle(true);
    }

});