package ospedale;

import java.util.Random;

public class Ritardo {
    public static int generateDelay(){//in questo modo decido gli il range per la funzione random
        Random random = new Random();
        int Low = 10;
        int High = 100;
        int ritardo = random.nextInt(High-Low) + Low;
        
        return ritardo;
    }
    
    public static Slot slotPazienteDaRitardare(Sala s){
        Slot slotPazRitardato = null;
        Random random = new Random();
        //mi assicuro di non prendere uno slot senza paziente
        do{ 
            slotPazRitardato = s.getSlot(random.nextInt(s.getBufferSize()-1));// qui meno 1
        }while(slotPazRitardato.isFree());
        
        return slotPazRitardato;
    }
    
    public static Sala salaDelPazienteDaRitardare(){
        Random random = new Random();
        Sala s = Ospedale.reparto.get(random.nextInt(Ospedale.reparto.size()-1));//da 0 a 19 visto che ho 20 sale (4 tipi per 5 giorni)
        return s;
    }
   
}
