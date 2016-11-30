UPDATE FSM_EVENT_TYPE SET CONTEXT = 'System' WHERE USERNAME = 'Fsm Install' and CONTEXT is null;
UPDATE FSM_EVENT_TYPE SET CONTEXT = 'DLD' WHERE USERNAME = 'Jupiter Installer' and CONTEXT is null;
UPDATE MTR_METERACTIVATION SET meterrole='meter.role.meter.role.default' where meterrole is null;
COMMIT;