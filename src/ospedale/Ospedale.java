
package ospedale;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class Ospedale {
    static final String DBPAZIENTI = "istanze.xlsx";
    static final String DBREPARTO = "outputAM.xlsx";
    static final double DURATASLOT = 30.0;
    static final int EXTRA_TIME = 1; //slot extra per operare pazienti in rischedulati 
    static final ArrayList<Sala> reparto = new ArrayList<Sala>();
    static final ArrayList<Paziente> DBPazienti = new ArrayList<Paziente>();
    static final TreeSet<Specialita> DBUnita_operative = new TreeSet<Specialita>();
    static Paziente pazRitardato = null;//memorizzo quello che ritardo
    static Sala salaRitardata = null;
    static ArrayList<Paziente> pazSettimanaSucc = new ArrayList<Paziente>();
    
    public static void main(String[] args) {
        try {
            ReadDBPazienti();
            System.out.println("DBPazienti = " + DBPazienti.size());
            System.out.println("DBSpecialità = " + DBUnita_operative.size());
            ReadDBReparto();
            System.out.println("Reparto = " + reparto.size());
            System.out.println("CALENDARIO ORIGINALE REPARTO");
            for(Sala s : reparto){
                System.out.println("Sala " + s.getId() + " - Giorno " + s.getGiorno());
                System.out.println(s);
            }
            System.out.println();
            
            int ritardo = effettuaRitardo();
            System.out.println("Il paziente " + pazRitardato.getId() + " ha subito un ritardo di: " + ritardo);
            if(!pazSettimanaSucc.isEmpty()){
                System.out.println("I pazienti spostati alla settimana successiva sono: ");
                for (int i = 0; i < pazSettimanaSucc.size(); i++){
                    System.out.print(pazSettimanaSucc.get(i).getId() + "\t");
                }
            }else
                System.out.println("Non ci sono pazienti posticipati alla settimana successiva");
            System.out.println();
            
            System.out.println("CALENDARIO MODIFICATO REPARTO");
            for(Sala s : reparto){
                System.out.println("Sala " + s.getId() + " - Giorno " + s.getGiorno());
                System.out.println(s);
            }
            
        } catch (IOException ex) {
            Logger.getLogger(Ospedale.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    public static void ReadDBPazienti() throws IOException {
        FileInputStream fis = new FileInputStream(new File(DBPAZIENTI));
        XSSFWorkbook wb = new XSSFWorkbook(fis);
        XSSFSheet sheet = wb.getSheetAt(0); //0 sta per il foglio 0 (1017 pazienti)
        
        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();
        
        for(int i = 1; i < sheet.getPhysicalNumberOfRows(); i++){ //per ogni riga presente in sheet
            Row row = sheet.getRow(i);
            Specialita specialita = null;
            int id = 0, unita_operativa = 0, durata = 0;
            
            for (int j = 0; row.getCell(j) != null; j++){
                Cell cell = row.getCell(j);                
                if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC && j == 0)
                    id = ((Double) cell.getNumericCellValue()).intValue();
                else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC && j == 2)
                    durata = ((Double) cell.getNumericCellValue()).intValue();
                else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC && j == 5){
                    String name;
                    unita_operativa = ((Double) cell.getNumericCellValue()).intValue();
                      
                    switch(unita_operativa){
                        case 1:
                            name = "GEN";
                            break;
                        case 2:
                            name = "ORL";
                            break;
                        case 3:
                            name = "ORTO";
                            break;
                        case 4:
                            name = "TOR";
                            break;
                        case 5:
                            name = "URO";
                            break;
                        default:
                            name = "NULL";
                    }
                    specialita = new Specialita(unita_operativa, name);
                    DBUnita_operative.add(specialita);
                }
            }   
            if(id != 0 && unita_operativa != 0 && durata != 0){
                Paziente p = new Paziente(id, specialita, durata);
                DBPazienti.add(p);
            }
        }
        
        
    }
    
    public static void ReadDBReparto() throws IOException {
        FileInputStream fis = new FileInputStream(new File(DBREPARTO));
        XSSFWorkbook wb = new XSSFWorkbook(fis);
        XSSFSheet sheet = wb.getSheetAt(18); //18 sta per il foglio 19 (300)
        
        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();
        
        int giorno = 0;
        int sala_operativa_n = 0;
        ArrayList<Slot> tmp_buffer;
        boolean g = false, nome_sala = false;
        
        for(int i = 0; i < sheet.getPhysicalNumberOfRows(); i++){ //per ogni riga presente in sheet
            Row row = sheet.getRow(i);
            
            Cell cell = row.getCell(0);         //Prendo sempre la prima colonna
            String cell_value = cell.getStringCellValue();
            if(cell_value.contains("Giorno") && !g){                    
                giorno = Integer.parseInt(cell_value.substring(cell_value.length()-1));
                g = true;
            }else if(cell_value.contains("Sala") && !nome_sala){
                sala_operativa_n = Integer.parseInt(cell_value.substring(cell_value.length()-1));
                nome_sala = true;
            }else if(cell_value.isEmpty()){
                g = false;
            }else if(g && nome_sala){
                String[] slotPazienti = cell_value.split(" ");
                int j = 0;
                tmp_buffer = new ArrayList<Slot>();
                for(String s : slotPazienti){
                    Paziente paziente = null;
                    int id = 0;
                    s.replace(" ", "");
                    if(s != null && !s.equals("")){
                        id = Integer.parseInt(s);
                        j++;
                    }else
                        continue;
                    
                    for(Paziente p : DBPazienti)
                        if(p.getId() == id)
                            paziente = p;
                    
                    if(paziente != null)
                        tmp_buffer.add(new Slot(j, paziente.getUnita_operativa(),paziente));
                    else
                        tmp_buffer.add(new Slot(j, null, null));
                }
                
                if(sala_operativa_n != 0 && giorno != 0 && tmp_buffer != null)
                    reparto.add(new Sala(sala_operativa_n, giorno, tmp_buffer));
                tmp_buffer = null;
                nome_sala = false;
            }
        }
    }
    
    @SuppressWarnings("empty-statement")
     public static Pair<ArrayList<Slot>,Sala> nextCompatibleSlot(Paziente paz, Sala sala, int startSlot){ //la sala serve solo per capire da dove iniziare. L'array va istanziato però il contenuto è solo ADD da cose gia esistenti che prendo da reparto che è gia in questa classe
        ArrayList<Slot> bloccoSlotUtili = new ArrayList<Slot>();
        Sala salaRisultato = null;
        Pair<ArrayList<Slot>,Sala> risultato = null;
        boolean ricercaCompletata=false;
        boolean procedi = false;
        boolean faiControllo = false;
        int indiceDiPartenzaSala = -1, count = 0, slotDiPartenza = startSlot;// contatoreDiVerificaAccettabilita=0;
        
        while(indiceDiPartenzaSala < 0)
        {
            if(reparto.get(count).getGiorno() == sala.getGiorno()){
                indiceDiPartenzaSala = count;
            }
            count++;
        }
        //slotDiPartenza=reparto.get(indiceDiPartenzaSala).
        for(int i = indiceDiPartenzaSala; i < reparto.size() && !ricercaCompletata; i++)
        {
            Paziente pazPrecedente = null;
            Sala salaCorrente = reparto.get(i);
            bloccoSlotUtili.clear();
            if(salaCorrente.getGiorno() > salaRitardata.getGiorno()){
                slotDiPartenza=0;
                procedi = true;//perchè se inizio da 0 non mi deve fare più questo controllo
                
            }
            if(!procedi){
                     faiControllo = false;
                 }
            
            for(int j = slotDiPartenza; j < salaCorrente.getBufferSize(); j++)
            {
                 if(j != 0){           
                    Slot slotPrecedente = salaCorrente.getSlot(j-1);
                    pazPrecedente = slotPrecedente.getPaziente();
                    }
                 
                Slot slotCorrente = salaCorrente.getSlot(j);
                Paziente pazCorrente = salaCorrente.getSlot(j).getPaziente();
                //Slot slotPrecedente = salaCorrente.getSlot(j-1);
                //Paziente pazPrecedente = slotPrecedente.getPaziente();
                if((pazCorrente == null || pazPrecedente == null || !pazCorrente.equals(pazPrecedente) || faiControllo))
                //VA FATTO IL CONTROLLO CHE IL PAZIENTE CHE RIMPIAZZIAMO NON HA GIA INIZIATO L'OPERAZIONE
                    if(slotCorrente.isFree() || slotCorrente.getSpecialita().equals(paz.getUnita_operativa())){
                        bloccoSlotUtili.add(slotCorrente);//errore, so di che slot parliamo ma non di che sala

                        if(Sala.getNumSlot(paz.getDurata()) <= bloccoSlotUtili.size())
                        {
                           salaRisultato = salaCorrente;
                           ricercaCompletata = true;
                           break;
                        }

                    }
                    else
                        bloccoSlotUtili.clear();
                faiControllo = true;
            }
            if(ricercaCompletata)
                break;
        }
        if(ricercaCompletata)
            risultato = new Pair<ArrayList<Slot>,Sala>(bloccoSlotUtili, salaRisultato);
        else
            risultato = new Pair<ArrayList<Slot>,Sala>(new ArrayList<Slot>(), salaRisultato);
        
            return risultato;
    }
     //se ho più di una settimana va fatto il controllo sul giorno e che quindi sto schedulando solo quella settimana
    @SuppressWarnings("empty-statement")
     public static /*ArrayList<Paziente>*/ void rischedulaEPosticipaPaz(StackSet daAssegnare){
         Paziente pazienteSettimanaSucc = null;
         ArrayList<Sala> tmpSale = cloneReparto();
         ArrayList<Slot> slotDaLiberare = new ArrayList<Slot>();
             
         //prendo il primo paziente da rimpiazzare 
         //pop rimuove e salva!
         Pair<Sala,Pair<Paziente, Integer>> top = daAssegnare.remove();
         Sala s = top.getKey();
         int indexSala = cercaSala(s);
         Sala sala_tmp = tmpSale.get(indexSala);
         Paziente p = top.getValue().getKey();
         int inizio_rimpiazzo = top.getValue().getValue();
         //int idSalaPerRimp;
         boolean scan = true;
         if(p.equals(pazRitardato)){
             //bisogna stare atttenti perche se sono più di 1 i ritardati, "inizio_rimpiazzo" potrebbe non essere giusto (forse)
             sala_tmp.replaceSlots(pazRitardato, inizio_rimpiazzo, pazRitardato.getDurata(), true);
         }else{
            Pair<ArrayList<Slot>,Sala> compatibleSlots = nextCompatibleSlot(p, s, inizio_rimpiazzo);
            int sala_dei_compatibili_index = Ospedale.cercaSala(compatibleSlots.getValue());
            //if(s.getBufferSize() != reparto.get(sala_dei_compatibili_index).getBufferSize()){
                tmpSale = cloneReparto();
                if(sala_dei_compatibili_index != -1)
                    sala_tmp = tmpSale.get(sala_dei_compatibili_index);
                
            //}
            
            if(!compatibleSlots.getKey().isEmpty()){
               //ArrayList<Slot> slotsCompatibili = compatibleSlots.getKey();         
               indexSala = sala_dei_compatibili_index;
               inizio_rimpiazzo = compatibleSlots.getKey().get(0).getId() - 1;
               //sala_tmp.rimpiazzaSala(reparto.get(sala_dei_compatibili_index));//cambio la sala
               sala_tmp.replaceSlots(p, inizio_rimpiazzo, p.getDurata() , false);
            }else{
                pazienteSettimanaSucc=p;
                scan = false;
            }
         }
        if(scan){        
            for(int i = inizio_rimpiazzo; i < sala_tmp.getBufferSize() && i < reparto.get(indexSala).getBufferSize(); i++){
                Paziente tmp_paz = sala_tmp.getSlot(i).getPaziente();
                Paziente ex_paz = reparto.get(indexSala).getSlot(i).getPaziente();
                //Paziente ex_paz_reparto = reparto.get(indexSala).getSlot(i).getPaziente();
                //Paziente ex_paz_sala_tmp = sala_tmp.getSlot(i-1).getPaziente();
                //ex_paz non c'è bisogno che venga messo come nuovo elemento se è null! null crea problemi all'equal
                if(tmp_paz != null && ex_paz != null){
                    if(!tmp_paz.equals(ex_paz)){//entro se sono diversi perchè devo aggiungerlo allo stack
                        // sala_tmp.getSlot(j).getPaziente() != null forse è inutile
                        
                        //Pair<Sala,Paziente> nuovoElemento = new Pair<Sala,Paziente>(sala_tmp, reparto.get(indexSala).getSlot(i).getPaziente());
                        Pair<Sala,Pair<Paziente, Integer>> nuovoElemento = new Pair<Sala,Pair<Paziente, Integer>>(sala_tmp, new Pair<Paziente, Integer>(ex_paz,reparto.get(indexSala).getStartSlotIndex(ex_paz) + 1));
                        daAssegnare.push(nuovoElemento);
                    }
                }
            }
            for(int j = inizio_rimpiazzo; !daAssegnare.isEmpty() && j < reparto.get(cercaSala(daAssegnare.prendiUltimo().getKey())).getLastSlotIndex(daAssegnare.prendiUltimo().getValue().getKey()); j++){
              //Paziente pazienteSucc = new reparto.get(indexSala).getSlot(j).getPaziente();
              if(sala_tmp.getSlot(j).getPaziente() != null && !sala_tmp.getSlot(j).getPaziente().equals(p))
                  sala_tmp.getSlot(j).libera();
                  //slotDaLiberare.add(sala_tmp.getSlot(j));
            }
        }    
        //sala_tmp.liberaSlot(slotDaLiberare);
         reparto.clear();
         reparto.addAll(tmpSale);
         if(pazienteSettimanaSucc!=null)
             pazSettimanaSucc.add(p);
         if(!daAssegnare.isEmpty()){             
            rischedulaEPosticipaPaz(daAssegnare);
         }
         //else
           // return pazientiNextWeek;    
    }
     
     public static int effettuaRitardo(){
           //questi vanno messi nel metodo che chiama quella funzione 
         int ritardo = 60;//Ritardo.generateDelay();
         salaRitardata = reparto.get(11);//Ritardo.salaDelPazienteDaRitardare();
         Slot slotPazRitardato = salaRitardata.getSlot(15);//Ritardo.slotPazienteDaRitardare(s);
         pazRitardato = slotPazRitardato.getPaziente();
         pazRitardato.setDurata(ritardo + pazRitardato.getDurata());//sto modificando la durata del mio paziente
         Pair<Sala,Pair<Paziente, Integer>> pazienteR = new Pair<Sala,Pair<Paziente, Integer>>(salaRitardata, new Pair<Paziente, Integer>(pazRitardato,salaRitardata.getStartSlotIndex(pazRitardato)));
         StackSet pilaPazienti = new StackSet();
         pilaPazienti.push(pazienteR);
         //pazSettimanaSucc = 
         rischedulaEPosticipaPaz(pilaPazienti);
         return ritardo;
    }
     
     public static int cercaSala(Sala s){
         int r = -1;
         boolean t = false;
         for(int i = 0; i < reparto.size() && !t; i++)
             if(reparto.get(i).equals(s)){
                 r = i;
                 t = true;
             }
         return r;
     }
     
     public static ArrayList<Sala> cloneReparto(){
        ArrayList<Sala> clone = new ArrayList<Sala>(reparto.size());
        for (Sala item : reparto) clone.add(item.cloneSala());
        return clone;
    }

}
