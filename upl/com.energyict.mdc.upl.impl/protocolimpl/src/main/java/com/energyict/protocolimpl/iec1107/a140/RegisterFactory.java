package com.energyict.protocolimpl.iec1107.a140;


public class RegisterFactory {
    
    private final CumulativeRegister cumulative;
    private final TouRegister tou;
    private final HistoricalRegisterSet historicalSet;
    private final LoadProfileRegister loadProfile;
    private final TouSourceRegister touSource;
    private final ReverseRunningRegister reverseRun;
    private final PowerFailRegister powerFail;
    private final BillingRegister billing;
    private final ConfigureReadRegister configureRead;
    private final LoadProfileConfigRegister loadProfileConfig; 
    private final SerialNumberRegister serialNumber;
    private final TimeAndDateRegister timeAndDate;
    
    public RegisterFactory( A140 a140 ){
        
        cumulative        = new CumulativeRegister(a140, "507", 10, 1, Register.N ); 
        tou               = new TouRegister(a140, "508", 20, 1, Register.N );
        historicalSet     = new HistoricalRegisterSet(a140, "543", 190, 5, Register.N);
        loadProfile       = new LoadProfileRegister( a140, "550", 190, 0, Register.N );
        touSource         = new TouSourceRegister( a140, "667", 1, 1, Register.N );
        reverseRun        = new ReverseRunningRegister( a140, "694", 14, 1, Register.N );
        powerFail         = new PowerFailRegister( a140, "695", 14, 1, Register.N );
        billing           = new BillingRegister( a140, "699", 17, 1, Register.N );
        configureRead     = new ConfigureReadRegister( a140, "551", 2, 1, Register.RW  );
        loadProfileConfig = new LoadProfileConfigRegister(a140, "777", 1, 1, Register.N );
        serialNumber      = new SerialNumberRegister(a140, "798", 16, 1, Register.N );
        timeAndDate       = new TimeAndDateRegister( a140, "861", 7, 1, Register.RW | Register.NC );
        
    }

    public CumulativeRegister getCumulative() {
        return cumulative;
    }

    public HistoricalRegisterSet getHistoricalSet() {
        return historicalSet;
    }

    public LoadProfileRegister getLoadProfile() {
        return loadProfile;
    }
    
    public BillingRegister getBilling() {
        return billing;
    }

    public PowerFailRegister getPowerFail() {
        return powerFail;
    }

    public ReverseRunningRegister getReverseRun() {
        return reverseRun;
    }

    public ConfigureReadRegister getConfigureRead( ) {
        return configureRead;
    }

    public LoadProfileConfigRegister getLoadProfileConfig() {
        return loadProfileConfig;
    }

    public SerialNumberRegister getSerialNumber(){
        return serialNumber;
    }
    
    public TimeAndDateRegister getTimeAndDate() {
        return timeAndDate;
    }

    public TouRegister getTou() {
        return tou;
    }

    public TouSourceRegister getTouSource() {
        return touSource;
    }
    
}
