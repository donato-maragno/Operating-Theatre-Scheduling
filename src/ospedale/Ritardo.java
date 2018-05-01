package ospedale;

import java.util.Random;

public class Ritardo {
    public static int generateDelay(){
        Random random = new Random();
        int ritardo = random.nextInt(40);
        
        return ritardo;
    }
    
    public static Slot slotPazienteDaRitardare(Sala s){
        Random random = new Random();
        Slot slotPazRitardato = s.getSlot(random.nextInt(s.getBufferSize()-1));// qui meno 1
        return slotPazRitardato;
    }
    
    public static Sala salaDelPazienteDaRitardare(){
        Random random = new Random();
        Sala s = Ospedale.reparto.get(random.nextInt(Ospedale.reparto.size()-1));//da 0 a 19 visto che ho 20 sale (4 tipi per 5 giorni)
        return s;
    }
   
}
