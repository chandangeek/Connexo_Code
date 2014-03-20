/**
 * @class Uni.view.navigation.SubMenu
 *
 * Common submenu that supports adding buttons and toggling.
 *
 * Styling will also be applied automatically to anything that is in the component.
 *
 * How and where content is shown after clicking a button in the submenu is free to choose.
 * Switching between panels can easily be done using a card layout.
 * Toggling is done automatically: when the url changes, the button with the current href is selected.
 *
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
 *                   href: '...',
 *                   ...
 *               });
 *               ...
 *
 *               getMenuCmp: function () {
 *                   return this.down('#myMenu');
 *               }
 *
 *
 */
Ext.define('Uni.view.navigation.SubMenu', {
    extend: 'Ext.container.Container',
    alias: 'widget.navigationSubMenu',

    baseCls: Uni.About.baseCssPrefix + 'submenu',

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

    /**
     * @cfg {Object/Ext.Component}
     *
     * Configuration of the menu items, buttons can be added here.
     */
    items: [
    ],

    initComponent: function () {
        this.defaults.toggleGroup = 'submenu-' + this.getId();
        var me = this;
        Ext.util.History.addListener('change', function (token) {
            me.checkNavigation(token);
        });
        this.callParent(this);
    },

    toggleMenuItem: function (index) {
        this.items.items[index].toggle(true);
    },

    checkNavigation: function (token) {
        for (var i = 0; i < this.items.items.length; i++) {
            var item = this.items.items[i];
            if ((item.getHref() != null) && (Ext.String.endsWith(item.getHref(), token))) {
                this.toggleMenuItem(i);
                break;
            }
        }
    }

});