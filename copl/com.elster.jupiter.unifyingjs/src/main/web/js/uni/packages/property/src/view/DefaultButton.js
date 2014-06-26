Ext.define('Uni.property.view.DefaultButton', {
    extend: 'Ext.button.Button',
    alias: 'widget.defaultButton',
    border: 0,
    icon: '../mdc/resources/images/redo.png',
    style: 'background-color: transparent; padding: 5px 3px 2px 3px;',
    scale: 'small',
    action: 'delete',
    margin: '0 0 5 5',
    hidden: true
});