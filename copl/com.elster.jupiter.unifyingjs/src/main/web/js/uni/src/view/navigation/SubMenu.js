/**
 * @class Uni.view.navigation.SubMenu
 *
 * Common submenu that supports adding buttons and toggling.
 *
 * Styling will also be applied automatically to anything that is in the component.
 *
 * How and where content is show after clicking a button in the submenu is not fixed.
 * Switching between panels can easily be done using a card layout.
 *
 * # Example usage
 *
 *     @example
 *       side: [
 *        {
 *            xtype: 'navigationSubMenu',
 *            itemId: 'myMenu'
 *        }
 *        ],
 *        ...
 *        initComponent: function () {
 *               ...
 *               this.initMenu();
 *               ...
 *           },
 *
 *        initMenu: function () {
 *               var me = this;
 *               var menu = this.getMenuCmp();
 *
 *               var button1 = menu.add({
 *                   text: '...',
 *                   pressed: true,
 *                   href: '...',
 *                   ...
 *               });
 *
 *               var button2 = menu.add({
 *                   text: '...',
 *                   pressed: false,
 *                   itemId: 'rulesLink',
 *                   href: '...',
 *                   ...
 *               });
 *               ...
 *
 *               getMenuCmp: function () {
 *                   return this.down('#myMenu');
 *               },
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