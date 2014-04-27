Ext.define('Isu.view.ext.button.ItemAction', {
    extend: 'Ext.button.Button',
    text: 'Actions',
    iconCls: 'x-uni-action-iconA',
    alias: 'widget.item-action',
    handler: function(){
        this.showMenu()
    }
});