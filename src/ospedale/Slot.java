
package ospedale;

/**
 *
 * @author Donato
 */
class Slot {
    protected int id;//da 1 a 24
    protected Specialita specialita;//del dott
    protected Paziente pazAssistito; 
    
    Slot (int id, Specialita specialita, Paziente paziente){
        this.id = id;
        this.specialita = specialita;
        this.pazAssistito = paziente;
    }
    
    public void setId(int id){
        this.id = id;
    }
        
    public int getId(){
        return this.id;
    }
    
    public Specialita getSpecialita(){
        return this.specialita;
    }
    
    public Paziente getPaziente(){
        return this.pazAssistito;
    }
    
    public boolean isFree(){
        boolean r = false;
        if(this.pazAssistito == null)
            r = true;
        return r;
    }
    
    public boolean occupa(Paziente p){
        boolean r = true;
        if(this.isFree() || p.getUnita_operativa() == this.specialita)
            this.pazAssistito = p;
        else
            r = false;
            
        return r;   
    }
    
    public boolean libera(){
        boolean r = false;
        if(!this.isFree()){
            this.pazAssistito = null;
            r = true;
        }
        return r;
    }
    
    public void rimpiazza(Paziente p){
        this.pazAssistito = p;
        this.specialita = p.getUnita_operativa();
    }
    
    public Slot cloneSlot(){
        Slot clone = new Slot(this.getId(), this.getSpecialita(), ((this.getPaziente() != null) ? this.getPaziente().clonePaziente() : null));
        return clone;
    }
    
}
