/**
 * MultiElement slaves are named to their multi-element device and device configuration
 */
Ext.define('Mdc.util.SlaveNameProposal', {
    alias: 'SlaveNameProposal',
    requires: ['Mdc.model.Device',
        'Mdc.model.DeviceConfiguration'
    ],
    singleton: true,
    get: function(datalogger, deviceConfiguration){
        var proposal = '';
        if (!Ext.isEmpty(datalogger)){
            proposal = datalogger.get('name');
        }
        if (!Ext.isEmpty(deviceConfiguration)) {
            if (proposal.length > 0) {
                proposal += '-'
            }
            proposal += deviceConfiguration.get('name');
        }
        return proposal;
    }
});