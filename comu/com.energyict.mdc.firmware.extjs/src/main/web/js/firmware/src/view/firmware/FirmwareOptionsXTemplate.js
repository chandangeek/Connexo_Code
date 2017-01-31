/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * Created by pdo on 6/05/2015.
 * Rendering the Firmware Management Options as an unordered list
 */
Ext.define('Fwc.view.firmware.FirmwareOptionsXTemplate', {
    extend: 'Ext.XTemplate',
    constructor: function(){
        this.callParent([   '<table>',
                                '<tbody>',
                                    '<tpl for=".">',
                                        '<tr><td>{localizedValue}</td></tr>',
                                    '</tpl>',
                                '</tbody>',
                            '</table>'
                        ]);
    } ,
    alternateClassName: 'FirmwareOptionsXTemplate',
    alias: 'firmware-options-template',
    emptyTemplate: new Ext.XTemplate('<table>',
                                     '  <tbody>',
                                            '<tr>',
                                                '<td>',
                                                    Uni.I18n.translate('deviceType.firmwaremanagementoptions.off', 'FWC', 'Firmware management is not allowed'),
                                                '</td>',
                                            '</tr>',
                                        '</tbody>',
                                     '</table>'),
    apply : function(values){
        if (!values || values.length == 0){
            return this.emptyTemplate.apply(values);
        }
        return this.callParent(arguments);
    }
});