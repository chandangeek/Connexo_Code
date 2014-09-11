Ext.define('Isu.view.ext.button.ItemAction', {
    extend: 'Ext.button.Button',
    text: 'Actions',
    iconCls: 'x-uni-action-iconD',

    alias: 'widget.item-action',
    menuAlign: 'tr-br?',
    handler: function(){
        this.showMenu()
    }
});