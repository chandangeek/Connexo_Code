/**
 * @class Mdc.privileges.DeviceCommands
 *
 * Class that defines privileges for DeviceCommands
 */
Ext.define('Mdc.privileges.DeviceCommands', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,

    executeCommands:['execute.device.message.level1','execute.device.message.level2','execute.device.message.level3','execute.device.message.level4'],
    level1:['execute.device.message.level1'],
    level2:['execute.device.message.level2'],
    level3:['execute.device.message.level3'],
    level4:['execute.device.message.level4'],

    any: function() {
        return Ext.Array.merge(Mdc.privileges.DeviceCommands.executeCommands);
    }

});
