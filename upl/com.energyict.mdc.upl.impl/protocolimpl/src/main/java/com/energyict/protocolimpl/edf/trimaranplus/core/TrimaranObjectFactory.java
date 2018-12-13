/*
 * TrimaranObjectFactory.java
 *
 * Created on 16 februari 2007, 15:19
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaranplus.core;

import com.energyict.protocolimpl.edf.trimaranplus.TrimaranPlus;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class TrimaranObjectFactory {
    
    private TrimaranPlus trimaran;
    EnergieIndex energieIndex = null; 
    PmaxValues pmaxValues = null;
    DureeDepassementValues dureeDepassementValues = null;
    DepassementQuadratiqueValues depassementQuadratiqueValues = null;
    TempsFonctionnementValues tempsFonctionnementValues = null;
    ParametresPplus1 parametresPplus1 = null;
    ParametresP parametresP = null;
    ParametresP parametresPmoins1 = null;
    ParametresP parametresPmoins2 = null;
    
    
    /** Creates a new instance of TrimaranObjectFactory */
    public TrimaranObjectFactory(TrimaranPlus trimaranPlus) {
        this.setTrimaran(trimaranPlus);
    }
    
    public TempsFonctionnementValues readTempsFonctionnementValues() throws IOException {
        if (tempsFonctionnementValues == null) {
           tempsFonctionnementValues = new TempsFonctionnementValues();
           for (int i=152;i<=160;i+=8){
              tempsFonctionnementValues.addTempsFonctionnement(readTempsFonctionnementReader(i).getTempsFonctionnement());
           }
           for (int i=272;i<=280;i+=8) {
			tempsFonctionnementValues.addTempsFonctionnement(readTempsFonctionnementReader(i).getTempsFonctionnement());
           }
//           for (int i=392;i<=400;i+=8)
//              tempsFonctionnementValues.addTempsFonctionnement(readTempsFonctionnementReader(i).getTempsFonctionnement());
        }
        return tempsFonctionnementValues;
    }   
    
    private TempsFonctionnementReader readTempsFonctionnementReader(int variableName) throws IOException {
        TempsFonctionnementReader obj = new TempsFonctionnementReader(this);
        obj.setVariableName(variableName);
        obj.read();
        return obj;
    }   
    

    public DepassementQuadratiqueValues readDepassementQuadratiqueValues() throws IOException {
        if (depassementQuadratiqueValues == null) {
           depassementQuadratiqueValues = new DepassementQuadratiqueValues();
           for (int i=136;i<=144;i+=8) {
			depassementQuadratiqueValues.addDepassementQuadratique(readDepassementQuadratiqueReader(i).getDepassementQuadratique());
		}
           for (int i=256;i<=264;i+=8) {
			depassementQuadratiqueValues.addDepassementQuadratique(readDepassementQuadratiqueReader(i).getDepassementQuadratique());
//           for (int i=376;i<=384;i+=8)
//              depassementQuadratiqueValues.addDepassementQuadratique(readDepassementQuadratiqueReader(i).getDepassementQuadratique());
		}
        }
        return depassementQuadratiqueValues;
    }   
    
    private DepassementQuadratiqueReader readDepassementQuadratiqueReader(int variableName) throws IOException {
        DepassementQuadratiqueReader obj = new DepassementQuadratiqueReader(this);
        obj.setVariableName(variableName);
        obj.read();
        return obj;
    }   
    
    public DureeDepassementValues readDureeDepassementValues() throws IOException {
        if (dureeDepassementValues == null) {
           dureeDepassementValues = new DureeDepassementValues();
           for (int i=120;i<=128;i+=8) {
			dureeDepassementValues.addDureeDepassement(readDureeDepassementReader(i).getDureeDepassement());
		}
           for (int i=240;i<=248;i+=8) {
			dureeDepassementValues.addDureeDepassement(readDureeDepassementReader(i).getDureeDepassement());
//           for (int i=360;i<=368;i+=8)
//              dureeDepassementValues.addDureeDepassement(readDureeDepassementReader(i).getDureeDepassement());
		}
        }
        return dureeDepassementValues;
    }   
    
    private DureeDepassementReader readDureeDepassementReader(int variableName) throws IOException {
        DureeDepassementReader obj = new DureeDepassementReader(this);
        obj.setVariableName(variableName);
        obj.read();
        return obj;
    }   
    
    
    public PmaxValues readPmaxValues() throws IOException {
        if (pmaxValues == null) {
           pmaxValues = new PmaxValues();
           for (int i=104;i<=112;i+=8) {
			pmaxValues.addPmax(readPmaxReader(i).getPmax());
		}
           for (int i=224;i<=232;i+=8) {
			pmaxValues.addPmax(readPmaxReader(i).getPmax());
//           for (int i=344;i<=352;i+=8)
//              pmaxValues.addPmax(readPmaxReader(i).getPmax());
		}
        }
        return pmaxValues;
    }   
    
    private PmaxReader readPmaxReader(int variableName) throws IOException {
        PmaxReader obj = new PmaxReader(this);
        obj.setVariableName(variableName);
        obj.read();
        return obj;
    }   
    
    public EnergieIndex readEnergieIndex() throws IOException {
        if (energieIndex == null) {
           energieIndex = new EnergieIndex();
           for (int i=56;i<=96;i+=8) {
			energieIndex.addEnergie(readEnergieIndexReader(i).getEnergie());
		}
           for (int i=176;i<=216;i+=8) {
			energieIndex.addEnergie(readEnergieIndexReader(i).getEnergie());
//           for (int i=296;i<=336;i+=8)
//              energieIndex.addEnergie(readEnergieIndexReader(i).getEnergie());
		}
        }
        return energieIndex;
    }   
    
    private EnergieIndexReader readEnergieIndexReader(int variableName) throws IOException {
        EnergieIndexReader obj = new EnergieIndexReader(this);
        obj.setVariableName(variableName);
        obj.read();
        return obj;
    }   
    
    
    public AsservissementClient readAsservissementClient() throws IOException {
        AsservissementClient obj = new AsservissementClient(this);
        obj.read();
        return obj;
    }   
    
    public ParametresP readParametresP() throws IOException {
        if (parametresP == null) {
            parametresP = new ParametresP(this);
            parametresP.setVariableName(48);
            parametresP.read();
        }
        return parametresP;
    }
    public ParametresP readParametresPmoins1() throws IOException {
        if (parametresPmoins1 == null) {
            parametresPmoins1 = new ParametresP(this);
            parametresPmoins1.setVariableName(168);
            parametresPmoins1.read();
        }
        return parametresPmoins1;
    }
    public ParametresP readParametresPmoins2() throws IOException {
        if (parametresPmoins2 == null) {
            parametresPmoins2 = new ParametresP(this);
            parametresPmoins2.setVariableName(288);
            parametresPmoins2.read();
        }
        return parametresPmoins2;
    }
    
    public ParametresPplus1 readParametresPplus1() throws IOException {
        if (parametresPplus1 == null) {
            parametresPplus1 = new ParametresPplus1(this);
            parametresPplus1.read();
        }
        return parametresPplus1;
    }
    
    public DateCourante readDateCourante() throws IOException {
        DateCourante obj = new DateCourante(this);
        obj.read();
        return obj;
    }
    
    public AccessPartiel readAccessPartiel() throws IOException {
        AccessPartiel obj = new AccessPartiel(this);
        obj.read();
        return obj;
    }
    
    public CourbeCharge getCourbeCharge(Date from) throws IOException {
        CourbeCharge cc = new CourbeCharge(this);
        cc.collect(from);
        return cc;
    }
    
    protected void writeAccessPartiel(int nr) throws IOException {
        Calendar cal = ProtocolUtils.getCleanCalendar(getTrimaran().getTimeZone());
        cal.set(Calendar.YEAR,1992);
        cal.set(Calendar.MONTH,0);
        cal.set(Calendar.DAY_OF_MONTH,1);
        cal.set(Calendar.HOUR_OF_DAY,0);
        int num = nr;
        int M = num / 6000;
        num -= M * 6000;
        int S = num / 100;
        int C = num % 100;        
        cal.set(Calendar.MINUTE,M);
        cal.set(Calendar.SECOND,S);
        cal.set(Calendar.MILLISECOND,C*10);
        AccessPartiel obj = new AccessPartiel(this);
        obj.setDateAccess(cal.getTime());
        obj.setNomAccess(0xFF);
        obj.write();
    }
    protected void writeAccessPartiel(Date dateAccess) throws IOException {
        AccessPartiel obj = new AccessPartiel(this);
        obj.setDateAccess(dateAccess);
        obj.setNomAccess(1);
        obj.write();
    }
    
    public TrimaranPlus getTrimaran() {
        return trimaran;
    }

    public void setTrimaran(TrimaranPlus trimaranPlus) {
        this.trimaran = trimaranPlus;
    }
    
    protected CourbeChargePartielle getCourbeChargePartielle() throws IOException {
        CourbeChargePartielle obj = new CourbeChargePartielle(this);
        obj.read();
        return obj;
    }
    
}
