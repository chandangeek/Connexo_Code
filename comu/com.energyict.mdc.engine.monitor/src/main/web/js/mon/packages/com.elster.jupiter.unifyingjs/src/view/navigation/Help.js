/**
 * @class Uni.view.navigation.Help
 */
Ext.define('Uni.view.navigation.Help', {
    extend: 'Ext.button.Button',
    alias: 'widget.navigationHelp',
    scale: 'medium',
    cls: 'nav-help',
    iconCls: 'icon-question3',
    href: 'help/index.html',
    hrefTarget: '_blank'
});