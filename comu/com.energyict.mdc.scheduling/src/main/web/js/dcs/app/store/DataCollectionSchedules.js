Ext.define('Dcs.store.DataCollectionSchedules', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Dcs.model.DataCollectionSchedule',


    /*proxy: {
        type: 'rest',
        url: '/api/dcs/scheduling',
        reader: {
            type: 'json',
            root: 'dataCollectionSchedules',
            totalProperty: 'total'
        }
    }  */

    proxy: {
     type: 'ajax',
     url: '../dcs/resources/data/datacollectionschedules.json',
     reader: {
         type: 'json',
         root: 'dataCollectionSchedules'
         }
     }


});