
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
    //ho messo void
    public void rimpiazzaSlots (Paziente p, int startSlot, int durata, boolean wip){
        int nSlot = Sala.getNumSlot(durata);
        //int endSlot = startSlot;
        for(int i = startSlot; i < buffer.size() && nSlot > 0; i++){
            buffer.get(i).rimpiazza(p);
            nSlot--;
            //endSlot++;
        }
        if(wip)//solo per i pazienti ritardati che vengono operati negli ultimi slot del giorno
            for(int i = startSlot + (Sala.getNumSlot(durata) - nSlot); nSlot > 0; i++){
                Slot s = new Slot(i, p.getUnita_operativa(), p);
                this.addSlot(s);
                nSlot--;
                //endSlot++;
            }
        //return endSlot;
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
    
    public ArrayList<Slot> getSlots()
    {
        return this.buffer;
    }
    
    public int contaSlotPaziente(Paziente paz){ //conta quanti slot ha occupato quel paziente
        boolean t = true;
        int c = 0;
        for (int i = 0; i < buffer.size() && t; i++){
            if(buffer.get(i).getPaziente() != null && buffer.get(i).getPaziente().equals(paz)){
                c++;
                if(i+1 < buffer.size()&& buffer.get(i+1).getPaziente() != null  && !buffer.get(i+1).getPaziente().equals(paz))
                    t = false;
            }
                
        }
        
        return c;
    }
    //mi ridà Index dell'ultimo slot del paziente
    public int getIndiceUltimoSlot (Paziente p){
        return this.getIndiceDiInizio(p) + this.contaSlotPaziente(p);
    }
    
    //rimpiazzo una sala con un'altra
    public void rimpiazzaSala (Sala s){
        this.buffer.removeAll(this.buffer);
        this.buffer.addAll(s.buffer);
    }
    // in totale, non consecutivi
    public int countaLiberi(){ 
        int c = 0;
        for (int i = 0; i < buffer.size(); i++)
            if (buffer.get(i).isFree())
                c++;
        return c;
    }
    //conta quanti sono gli slot liberi (se presenti) subito dopo il paziente
    public int countaPrimoBlocco(Paziente paziente){
        boolean f = false;      //il blocco del paziente non è ancora finito
        int c = 0;
        for(int i = 0; i < buffer.size() && !f; i++)
            if(buffer.get(i).getPaziente().equals(paziente)){
                if(i+1 < buffer.size()-1 && !buffer.get(i+1).getPaziente().equals(paziente)){
                    f = true;
                    c = this.countFirstBlock(getDurataDaNSlot(i));
                }
            }
            
        return c;
    }
    
    public static int getDurataDaNSlot (int nSlot){
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
    
    public int getIndiceDiInizio(Paziente p){
        boolean t = false;
        int index = -1;
        for(int i = 0; i < this.getBufferSize() && !t; i++){
            if (this.getSlot(i).getPaziente() != null && this.getSlot(i).getPaziente().equals(p)){
                index = i;
                t = true;
            }
        }
        return index;
    }
    
    @Override
    public String toString(){
        String r = "";
        for(Slot s : buffer)
            r += ((s.getPaziente() != null) ? s.getPaziente().getId() : "0") + "\t";
        return r;
    }
    
    public Sala cloneSala(){
        ArrayList<Slot> cloneList = new ArrayList<Slot>(this.getBufferSize());
        for (Slot item : this.buffer) cloneList.add(item.cloneSlot());
        Sala clone = new Sala(this.getId(), this.getGiorno(), cloneList);
        return clone;
    }
    
    public void liberaSlot(ArrayList<Slot> listaSlotDaLib){
        for (int i = 0; i < this.getBufferSize(); i++){
            for(int j = 0; j < listaSlotDaLib.size(); j++)
                if(this.getSlot(i).equals(listaSlotDaLib.get(j)))
                    this.getSlot(i).libera();
        }
    }
    
    
    
    @Override
    public boolean equals(Object obj){
        boolean r = false;
        if (obj instanceof Sala){
            Sala b = (Sala) obj;
            if(this.id == b.getId() && this.giorno == b.getGiorno())
                r = true;
        }
        return r;
    }

    public void addSlot(Slot extra_slot) {
        this.buffer.add(extra_slot);
    }
}
