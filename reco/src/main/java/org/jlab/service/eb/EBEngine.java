/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.eb;

import java.util.List;
import org.jlab.clas.detector.DetectorParticle;
import org.jlab.clas.detector.DetectorResponse;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;

/**
 *
 * @author gavalian
 */
public class EBEngine extends ReconstructionEngine {
    
    public EBEngine(){
        super("EB","gavalian","1.0");
    }
    
    @Override
    public boolean processDataEvent(DataEvent de) {
        
        int eventType = EBio.TRACKS_HB;
        
        if(EBio.isTimeBased(de)==true){
            eventType = EBio.TRACKS_TB;
        }
        
        List<DetectorParticle>  chargedParticles = EBio.readTracks(de, eventType);
        List<DetectorResponse>  ftofResponse     = EBio.readFTOF(de);
        List<DetectorResponse>  ecalResponse     = EBio.readECAL(de);
        
        EBProcessor processor = new EBProcessor(chargedParticles);
        processor.addTOF(ftofResponse);
        processor.addECAL(ecalResponse);
        
        processor.matchTimeOfFlight();
        processor.matchCalorimeter();
        processor.matchNeutral();
        List<DetectorParticle> centralParticles = EBio.readCentralTracks(de);
        
        processor.getParticles().addAll(centralParticles);
        /*
        if(de.hasBank("RUN::config")==true){
            EvioDataBank bankHeader = (EvioDataBank) de.getBank("RUN::config");
            System.out.println(String.format("***>>> RUN # %6d   EVENT %6d", 
                    bankHeader.getInt("Run",0), bankHeader.getInt("Event",0)));
        }*/
        //processor.show();
        /*System.out.println("CHARGED PARTICLES = " + chargedParticles.size());
        for(DetectorParticle p : chargedParticles){
            System.out.println(p);
        }*/
        EvioDataBank pBank = (EvioDataBank) EBio.writeTraks(processor.getParticles(), eventType);
        de.appendBanks(pBank);
        
        return true;
    }

    @Override
    public boolean init() {
        System.out.println("[EB::] --> event builder is ready....");
        return true;
    }
    
}