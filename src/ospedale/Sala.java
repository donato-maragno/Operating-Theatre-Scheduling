
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
        int nSlot = this.getNumSlot(paziente.getDurata());
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
    
    public void replaceSlots (Paziente p, int start, int durata){
        int nSlot = this.getNumSlot(durata);
        
        for(int i = this.getNumSlot(start); i < buffer.size() && nSlot > 0; i++){
            buffer.get(i).rimpiazza(p);
            nSlot--;
        }
    }
    
    public int getGiorno(){
        return this.giorno;
    }
    
    public Slot getSlot(int id){
        return buffer.get(id);
    }
    
    public int getNumSlot(int durata){
        return (int) Math.ceil(durata/Ospedale.DURATASLOT);
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
    
    public int getTimeBySlot (int nSlot){
        return nSlot*Ospedale.DURATASLOT;
    }
    
    //conta i blocchi liberi dopo tot minuti
    public int countFirstBlock(int minuti){
        int nSlot = this.getNumSlot(minuti);
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
    
    @Override
    public String toString(){
        String r = "";
        for(Slot s : buffer)
            r += ((s.getPaziente() != null) ? s.getPaziente().getId() : "0") + "\t";
        return r;
    }
    
}
