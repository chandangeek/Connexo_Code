Ext.define('Mdc.view.setup.property.DefaultButton', {
    extend: 'Ext.button.Button',
    alias: 'widget.defaultButton',
    border: 0,
    icon: '../mdc/resources/images/redo.png',
    style: 'background-color: transparent; padding: 5px 3px 2px 3px;',
    tooltip: 'Restore to default value',
    scale: 'small',
    action: 'delete',
    disabled: true,
    margin: '0 0 5 5',

    initComponent: function(){
        this.name = 'btn_delete_' + this.key;
        this.itemId = 'btn_delete_' + this.key;
    }
});