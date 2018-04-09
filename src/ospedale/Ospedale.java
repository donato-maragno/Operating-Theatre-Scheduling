
package ospedale;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class Ospedale {
    static final String DBPAZIENTI = "istanze.xlsx";
    static final String DBREPARTO = "outputAM.xlsx";
    static final double DURATASLOT = 30.0;
    static final int EXTRA_TIME = 1;
    static final ArrayList<Sala> reparto = new ArrayList<Sala>();
    static final ArrayList<Paziente> DBPazienti = new ArrayList<Paziente>();
    static final TreeSet<Specialita> DBUnita_operative = new TreeSet<Specialita>();
    
    public static void main(String[] args) {
        try {
            ReadDBPazienti();
            System.out.println("DBPazienti = " + DBPazienti.size());
            System.out.println("DBSpecialità = " + DBUnita_operative.size());
            ReadDBReparto();
            System.out.println("Reparto = " + reparto.size());
            System.out.println("Seconda Sala " + reparto.get(1));
            nextCompatibleSlot(DBPazienti.get(96), reparto.get(3), 20);
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
    
     public static ArrayList<Slot> nextCompatibleSlot(Paziente paz, Sala sala, int startSlot){ //la sala serve solo per capire da dove iniziare. L'array va istanziato però il contenuto è solo ADD da cose gia esistenti che prendo da reparto che è gia in questa classe
        ArrayList<Slot> block1 = new ArrayList<Slot>();
        //ArrayList<Slot> zeros = new ArrayList<Slot>();
        ArrayList<Slot> block2 = new ArrayList<Slot>();
        
        int giorno = sala.getGiorno();
        int firstSala = 0, newStartSlot = startSlot;
        boolean t = false;
        boolean tf = false; //terminare il for nel caso cambiasse il paziente dello slot
        boolean g = false;
        boolean z = false;
        boolean back = false; //è true quando ricomincio la scansione dalla sala che non ho terminato
        
        for(int i = 0; !g; i++)
            if(reparto.get(i).getGiorno() == giorno){
                firstSala = reparto.get(i).getId();
                g = true;
            }
        
        
        for(int i = firstSala; i < reparto.size() && !t; i++){
            Sala s = reparto.get(i);
            if(giorno != s.getGiorno())
                newStartSlot = 0;
            else if(!back)          
                newStartSlot = startSlot;
            for(int id = newStartSlot; id < s.getBufferSize() && !tf; id++){
                Slot sl = s.getSlot(id);
                if(paz.getUnita_operativa().equals(sl.getSpecialita())){
                    if(!z)
                        block1.add(sl);
                    else
                        block2.add(sl);
                }else if(sl.isFree()){
                    block2.add(sl);
                    z = true;
                }else{
                    if(!block1.isEmpty())
                        tf = true;                
                }
                newStartSlot = id;      
            }
            if (
                    (block1.size() + block2.size() >= Sala.getNumSlot(paz.getDurata())) ||
                    (!block1.isEmpty() && block2.isEmpty() && block1.get(block1.size()-1) == s.getSlot(s.getBufferSize()-1) && block1.size() + Ospedale.EXTRA_TIME >= Sala.getNumSlot(paz.getDurata())) ||
                    (!block2.isEmpty() && block2.get(block2.size()-1) == s.getSlot(s.getBufferSize()-1) && block1.size() + block2.size() + Ospedale.EXTRA_TIME >= Sala.getNumSlot(paz.getDurata()))
                ){
                t = true;
                block1.addAll(block2);
                //se si vuole allocare lo slot del "caso limite", occorre fare l'if (vedi ultime due condizioni sopra) e un new Slot con id "nuovo".
            }else{
                tf = false;
                z = false;
                block1.clear();
                block2.clear();
                if(newStartSlot < s.getBufferSize() - 1){        //se non ho finito di scansionare la sala, riprendo da dove l'ho lasciata
                    i--;
                    back = true;
                }else                                       //finita la scansione della sala, resetto la variabile
                    back = false;
            }
        }
            
        return block1;    
    }
    
}
