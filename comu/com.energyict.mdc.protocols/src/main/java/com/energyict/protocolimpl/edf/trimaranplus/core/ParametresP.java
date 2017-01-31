/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ParametresP.java
 *
 * Created on 16 februari 2007, 15:22
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaranplus.core;

import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;

import com.energyict.protocolimpl.edf.trimarandlms.axdr.TrimaranDataContainer;
import com.energyict.protocolimpl.edf.trimarandlms.common.DateType;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author Koen
 */
public class ParametresP extends AbstractTrimaranObject {

    private DateType DateDebutPeriode; // -- date de debut de la periode de facturation P
    private int CodeAF; // Integer16, - code de l'action facturation e l'origine de la periode - bits
    // 0 e 7 : code action, bits 8 e 15 : commentaire
    private DateType DateFinPeriode; // DateType, - champ sans signification, date par defaut
    private int TC; // Integer16, -- rapport de transformation de puissance, de 1 e 400
    private int TT; // Integer16, -- rapport de transformation de puissance, de 1 e 450
    private Quantity[] PS; // SEQUENCE OF Integer16, -- puissances souscrites par periode tarifaire, exprimees en kW
    private int KJ; // Integer16, -- valeur du coefficient de pertes joules, multiplie par 1000
    private int KPr; // Integer8, -- valeur du coefficient utilise pour le calcul de l'energie reactive positive en kvarh ramenes au primaire multiplie par 100
    private Quantity KF; // Integer16, -- valeur du parametre pertes Fer, exprime en W
    private boolean A5; // BOOLEAN, -- e VRAI si le tarif est A5, e FAUX si le tarif est A8
    private int TCourbeCharge; // Integer8, -- periode d'integration Tc pour le suivi de la courbe de charge en nombre de fois 5 min.
    private int[] TableauHeureJour1; // SEQUENCE OF Integer8, -- champ heure, de 0 e 23, des elements de la table journaliere 1
    private int[] TableauMinuteJour1; // SEQUENCE OF Integer8, -- champ minute des elements de la table journaliere 1, exprime en nombre de fois Td
    private int[] TableauPosteJour1; // SEQUENCE OF Integer8, -- champ poste horaire, des elements de la table journaliere 1
    private int[] TableauHeureJour2; // SEQUENCE OF Integer8, -- champ heure, de 0 e 23, des elements de la table journaliere 2
    private int[] TableauMinuteJour2; // SEQUENCE OF Integer8, -- champ minute des elements de la table journaliere 2, exprime en nombre de fois Td
    private int[] TableauPosteJour2; // SEQUENCE OF Integer8 } -- champ poste horaire, des elements de la table journaliere 2

    private int variableName;

    /** Creates a new instance of ParametresP */
    public ParametresP(TrimaranObjectFactory trimaranObjectFactory) {
        super(trimaranObjectFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();

        try {
            strBuff.append("ParametresP ("+VariableNameFactory.getVariableName(variableName)+"):\n");
        }
        catch(IOException e) {
            strBuff.append("ParametresP ("+e.toString()+"):\n");
        }
        strBuff.append("   dateDebutPeriode="+getDateDebutPeriode()+"\n");
        strBuff.append("   dateFinPeriode="+getDateFinPeriode()+"\n");
        strBuff.append("   KF="+getKF()+"\n");
        strBuff.append("   KJ="+getKJ()+"\n");
        strBuff.append("   KPr="+getKPr()+"\n");
        for (int i=0;i<getPS().length;i++) {
            strBuff.append("       PS["+i+"]="+getPS()[i]+"\n");
        }
        strBuff.append("   TC="+getTC()+"\n");
        strBuff.append("   TCourbeCharge="+getTCourbeCharge()+"\n");
        strBuff.append("   TT="+getTT()+"\n");
        if (getTrimaranObjectFactory().getTrimaran().getVDEType().isVDEBASE() || getTrimaranObjectFactory().getTrimaran().getVDEType().isVDEEJP()){
            strBuff.append("   a5="+isA5()+"\n");
        }
        strBuff.append("   codeAF="+getCodeAF()+"\n");
        for (int i=0;i<getTableauHeureJour1().length;i++) {
            strBuff.append("       tableauHeureJour1["+i+"]="+getTableauHeureJour1()[i]+"\n");
        }
        for (int i=0;i<getTableauMinuteJour1().length;i++) {
            strBuff.append("       tableauMinuteJour1["+i+"]="+getTableauMinuteJour1()[i]+"\n");
        }
        for (int i=0;i<getTableauPosteJour1().length;i++) {
            strBuff.append("       tableauPosteJour1["+i+"]="+getTableauPosteJour1()[i]+"\n");
        }
        if (getTrimaranObjectFactory().getTrimaran().getVDEType().isVDEBASE() || getTrimaranObjectFactory().getTrimaran().getVDEType().isVDEEJP()) {
            for (int i=0;i<getTableauHeureJour2().length;i++) {
                strBuff.append("       tableauHeureJour2["+i+"]="+getTableauHeureJour2()[i]+"\n");
            }
            for (int i=0;i<getTableauMinuteJour2().length;i++) {
                strBuff.append("       tableauMinuteJour2["+i+"]="+getTableauMinuteJour2()[i]+"\n");
            }
            for (int i=0;i<getTableauPosteJour2().length;i++) {
                strBuff.append("       tableauPosteJour2["+i+"]="+getTableauPosteJour2()[i]+"\n");
            }
        }
        return strBuff.toString();
    }

    protected int getVariableName() {
        return variableName;
    }

    protected byte[] prepareBuild() throws IOException {
        return null;
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        TrimaranDataContainer dc = new TrimaranDataContainer();
        dc.parseObjectList(data, getTrimaranObjectFactory().getTrimaran().getLogger());

        //int structureLength = dc.getRoot().getNrOfElements(); // 17 for VDEType BASE and EJP, 13 for VDEType MODULABLE

//        if (structureLength==17)
//            getTrimaranObjectFactory().getTrimaranPlus().getVDEType().setVDEType(VDEType.getVDEBASE());
//        else
//            getTrimaranObjectFactory().getTrimaranPlus().getVDEType().setVDEType(VDEType.getVDEMODULABLE());

        setDateDebutPeriode(new DateType(dc.getRoot().getLong(offset++), getTrimaranObjectFactory().getTrimaran().getTimeZone()));
        setCodeAF(dc.getRoot().getInteger(offset++));
        setDateFinPeriode(new DateType(dc.getRoot().getLong(offset++), getTrimaranObjectFactory().getTrimaran().getTimeZone()));
        setTC(dc.getRoot().getInteger(offset++));
        setTT(dc.getRoot().getInteger(offset++));
        setPS(new Quantity[dc.getRoot().getStructure(offset).getNrOfElements()]);
        for (int i=0;i<getPS().length;i++) {
			getPS()[i] = new Quantity(new BigDecimal(""+dc.getRoot().getStructure(offset).getInteger(i)),Unit.get("kW"));
		}
        offset++;
        setKJ(dc.getRoot().getInteger(offset++));
        setKPr(dc.getRoot().getInteger(offset++));
        setKF(new Quantity(new BigDecimal(""+dc.getRoot().getInteger(offset++)),Unit.get("W")));
        if ((getTrimaranObjectFactory().getTrimaran().getVDEType().isVDEBASE() || getTrimaranObjectFactory().getTrimaran().getVDEType().isVDEEJP())) {
			setA5((dc.getRoot().getInteger(offset++)==1));
		}
        setTCourbeCharge(dc.getRoot().getInteger(offset++));
        setTableauHeureJour1(new int[dc.getRoot().getStructure(offset).getNrOfElements()]);
        for (int i=0;i<getTableauHeureJour1().length;i++) {
			getTableauHeureJour1()[i] = dc.getRoot().getStructure(offset).getInteger(i);
		}
        offset++;
        setTableauMinuteJour1(new int[dc.getRoot().getStructure(offset).getNrOfElements()]);
        for (int i=0;i<getTableauMinuteJour1().length;i++) {
			getTableauMinuteJour1()[i] = dc.getRoot().getStructure(offset).getInteger(i);
		}
        offset++;
        setTableauPosteJour1(new int[dc.getRoot().getStructure(offset).getNrOfElements()]);
        for (int i=0;i<getTableauPosteJour1().length;i++) {
			getTableauPosteJour1()[i] = dc.getRoot().getStructure(offset).getInteger(i);
		}
        offset++;
        if (getTrimaranObjectFactory().getTrimaran().getVDEType().isVDEBASE() || getTrimaranObjectFactory().getTrimaran().getVDEType().isVDEEJP()) {
            setTableauHeureJour2(new int[dc.getRoot().getStructure(offset).getNrOfElements()]);
            for (int i=0;i<getTableauHeureJour2().length;i++) {
				getTableauHeureJour2()[i] = dc.getRoot().getStructure(offset).getInteger(i);
			}
            offset++;
            setTableauMinuteJour2(new int[dc.getRoot().getStructure(offset).getNrOfElements()]);
            for (int i=0;i<getTableauMinuteJour2().length;i++) {
				getTableauMinuteJour2()[i] = dc.getRoot().getStructure(offset).getInteger(i);
			}
            offset++;
            setTableauPosteJour2(new int[dc.getRoot().getStructure(offset).getNrOfElements()]);
            for (int i=0;i<getTableauPosteJour2().length;i++) {
				getTableauPosteJour2()[i] = dc.getRoot().getStructure(offset).getInteger(i);
			}
            offset++;
        }
    }

    public DateType getDateDebutPeriode() {
        return DateDebutPeriode;
    }

    public void setDateDebutPeriode(DateType DateDebutPeriode) {
        this.DateDebutPeriode = DateDebutPeriode;
    }

    public int getCodeAF() {
        return CodeAF;
    }

    public void setCodeAF(int CodeAF) {
        this.CodeAF = CodeAF;
    }

    public DateType getDateFinPeriode() {
        return DateFinPeriode;
    }

    public void setDateFinPeriode(DateType DateFinPeriode) {
        this.DateFinPeriode = DateFinPeriode;
    }

    public int getTC() {
        return TC;
    }

    public void setTC(int TC) {
        this.TC = TC;
    }

    public int getTT() {
        return TT;
    }

    public void setTT(int TT) {
        this.TT = TT;
    }

    public Quantity[] getPS() {
        return PS;
    }

    public void setPS(Quantity[] PS) {
        this.PS = PS;
    }

    public int getKJ() {
        return KJ;
    }

    public void setKJ(int KJ) {
        this.KJ = KJ;
    }

    public int getKPr() {
        return KPr;
    }

    public void setKPr(int KPr) {
        this.KPr = KPr;
    }

    public Quantity getKF() {
        return KF;
    }

    public void setKF(Quantity KF) {
        this.KF = KF;
    }

    public boolean isA5() {
        return A5;
    }

    public void setA5(boolean A5) {
        this.A5 = A5;
    }

    public int getTCourbeCharge() {
        return TCourbeCharge;
    }

    public void setTCourbeCharge(int TCourbeCharge) {
        this.TCourbeCharge = TCourbeCharge;
    }

    public int[] getTableauHeureJour1() {
        return TableauHeureJour1;
    }

    public void setTableauHeureJour1(int[] TableauHeureJour1) {
        this.TableauHeureJour1 = TableauHeureJour1;
    }

    public int[] getTableauMinuteJour1() {
        return TableauMinuteJour1;
    }

    public void setTableauMinuteJour1(int[] TableauMinuteJour1) {
        this.TableauMinuteJour1 = TableauMinuteJour1;
    }

    public int[] getTableauPosteJour1() {
        return TableauPosteJour1;
    }

    public void setTableauPosteJour1(int[] TableauPosteJour1) {
        this.TableauPosteJour1 = TableauPosteJour1;
    }

    public int[] getTableauHeureJour2() {
        return TableauHeureJour2;
    }

    public void setTableauHeureJour2(int[] TableauHeureJour2) {
        this.TableauHeureJour2 = TableauHeureJour2;
    }

    public int[] getTableauMinuteJour2() {
        return TableauMinuteJour2;
    }

    public void setTableauMinuteJour2(int[] TableauMinuteJour2) {
        this.TableauMinuteJour2 = TableauMinuteJour2;
    }

    public int[] getTableauPosteJour2() {
        return TableauPosteJour2;
    }

    public void setTableauPosteJour2(int[] TableauPosteJour2) {
        this.TableauPosteJour2 = TableauPosteJour2;
    }

    public void setVariableName(int variableName) {
        this.variableName = variableName;
    }
}
