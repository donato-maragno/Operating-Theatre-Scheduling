
package ospedale;

import java.util.ArrayList;

/**
 *
 * @author Donato
 */
class Sala {
    protected int id;
    protected int giorno;
    protected ArrayList<Slot> buffer = new ArrayList<Slot>();
    Sala (int id, int giorno, ArrayList<Slot> buffer){
        this.id = id;
        this.giorno = giorno;
        this.buffer = buffer;
    }
    
    //Occupa un blocco di slot libero con un paziente
    public void setSlots(Paziente paziente){
        int nSlot = Sala.getNumSlot(paziente.getDurata());
        int c = 0;
        for (int i = 0; i < buffer.size() && c < nSlot; i++){
            if(buffer.get(i).isFree()){
                c++;  
                if(c == nSlot)
                    buffer.get(i-nSlot).occupa(paziente);
            }else
                c = 0;
        }
        
                   
    }
    //wip serve nel caso dovessimo usare il replaceSlots per un paziente la cui operazione ritarda
    public void replaceSlots (Paziente p, int start, int durata, boolean wip){
        int nSlot = Sala.getNumSlot(durata);
        for(int i = Sala.getNumSlot(start); i < buffer.size() && nSlot > 0; i++){
            buffer.get(i).rimpiazza(p);
            nSlot--;
            
        }
        if(wip)//solo per i pazienti ritardati che vengono operati negli ultimi slot del giorno
            for(int i = Sala.getNumSlot(start) + (Sala.getNumSlot(durata) - nSlot); nSlot > 0; i++){
                Slot s = new Slot(i, p.getUnita_operativa(), p);
                buffer.add(s);
                nSlot--;
            }
    }
    
    public int getGiorno(){
        return this.giorno;
    }
    
    public Slot getSlot(int id){
        return buffer.get(id);
    }
    
    public static int getNumSlot(int durata){
        return (int) Math.ceil(durata/Ospedale.DURATASLOT);
    }
    
    public int getId (){
        return this.id;
    }
    
    public int getBufferSize (){
        return buffer.size();
    }
    
    public int countPaziente(Paziente paz){ //conta quanti slot ha occupato quel paziente
        boolean t = true;
        int c = 0;
        for (int i = 0; i < buffer.size() && t; i++){
            if(buffer.get(i).getPaziente().equals(paz)){
                c++;
                if(i+1 < buffer.size() && !buffer.get(i+1).getPaziente().equals(paz))
                    t = false;
            }
                
        }
        
        return c;
    }
    // in totale, non consecutivi
    public int countFree(){ 
        int c = 0;
        for (int i = 0; i < buffer.size(); i++)
            if (buffer.get(i).isFree())
                c++;
        return c;
    }
    //conta quanti sono gli slot liberi (se presenti) subito dopo il paziente
    public int countFirstBlock(Paziente paziente){
        boolean f = false;      //il blocco del paziente non è ancora finito
        int c = 0;
        for(int i = 0; i < buffer.size() && !f; i++)
            if(buffer.get(i).getPaziente().equals(paziente)){
                if(i+1 < buffer.size()-1 && !buffer.get(i+1).getPaziente().equals(paziente)){
                    f = true;
                    c = this.countFirstBlock(getTimeBySlot(i));
                }
            }
            
        return c;
    }
    
    public static int getTimeBySlot (int nSlot){
        return (int) (nSlot*Ospedale.DURATASLOT);
    }
    
    //conta i blocchi liberi dopo tot minuti
    public int countFirstBlock(int minuti){
        int nSlot = Sala.getNumSlot(minuti);
        boolean f = false;      //il blocco non è ancora finito
        int c = 0;
        for(int i = nSlot; i < buffer.size() && !f; i++)
            if(buffer.get(i).isFree()){
                c++;
                if(i+1 < buffer.size()-1 && !buffer.get(i+1).isFree())
                    f = true;
            }
        return c;
    }
    
    public int getStartSlotID(Paziente p){
        boolean t = false;
        int id = -1;
        for(int i = 0; i < this.getBufferSize() && !t; i++){
            if (this.getSlot(i).getPaziente().equals(p)){
                id = this.getSlot(i).getId();
                t = true;
            }
        }
        return id;
    }
    
    @Override
    public String toString(){
        String r = "";
        for(Slot s : buffer)
            r += ((s.getPaziente() != null) ? s.getPaziente().getId() : "0") + "\t";
        return r;
    }
    
}
