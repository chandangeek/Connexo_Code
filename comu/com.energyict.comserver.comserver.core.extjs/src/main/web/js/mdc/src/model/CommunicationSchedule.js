/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.CommunicationSchedule',{
      extend: 'Uni.model.Version',
      fields: [
          {name: 'id', type: 'int', useNull: true},
          {name: 'name', type: 'string', useNull: true},
          {name: 'mRID', type: 'string', useNull: true},
          {name: 'temporalExpression'},
          {name: 'plannedDate', dateFormat: 'time', type: 'date'},
          {name: 'isInUse',type: 'boolean'},
          {name: 'startDate'},
          {name: 'comTaskUsages'}
      ],
    associations: [
        {name: 'comTaskUsages', type: 'hasMany', model: 'Mdc.model.CommunicationTask', associationKey: 'comTaskUsages',
            getTypeDiscriminator: function (node) {
                return 'Mdc.model.CommunicationTask';
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '../../api/scr/schedules',
        reader: {
            type: 'json'
        }
    }
});
