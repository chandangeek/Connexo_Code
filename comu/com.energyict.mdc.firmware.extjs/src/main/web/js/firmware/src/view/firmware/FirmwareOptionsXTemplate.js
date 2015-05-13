/**
 * Created by pdo on 6/05/2015.
 * Rendering the Firmware Management Options as an unordered list
 */
Ext.define('Fwc.view.firmware.FirmwareOptionsXTemplate', {
    extend: 'Ext.XTemplate',
    constructor: function(){
        this.callParent([
                            '<tpl for=".">',
                                  '<p>{localizedValue}</p>',
                            '</tpl>',

                        ]);
    } ,
    alternateClassName: 'FirmwareOptionsXTemplate',
    alias: 'firmware-options-template',
    emptyTemplate: new Ext.XTemplate('<p>'+Uni.I18n.translate('deviceType.firmwaremanagementoptions.off', 'FWC', 'Firmware management is off')+'</p>'),
    apply : function(values){
        if (!values || values.length == 0){
            return this.emptyTemplate.apply(values);
        }
        return this.callParent(arguments);
    }
});