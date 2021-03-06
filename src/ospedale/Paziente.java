
package ospedale;

class Paziente {
    protected int id;
    protected Specialita unita_operativa;
    protected int durata;
    
    Paziente (int id, Specialita unita_operativa, int durata){
        this.id = id;
        this.unita_operativa = unita_operativa;
        this.durata = durata;      
    }
    
    public void setId(int id){
        this.id = id;
    }
    public void setUnita_operativa(Specialita unita_operativa){
        this.unita_operativa = unita_operativa;
    }
    public void setDurata(int durata){
        this.durata = durata;
    }
    
    public int getId(){
        return id;
    }
    
    public Specialita getUnita_operativa (){
        return unita_operativa;
    }
    
    public int getDurata(){
        return durata;
    }
    
    @Override
    public boolean equals(Object obj){
        boolean r = false;
        if (obj instanceof Paziente){
            Paziente b = (Paziente) obj;
            if(this.id == b.getId())
                r = true;
        }
        return r;
    }
    
    public Paziente clonePaziente(){
        Paziente clone = new Paziente(this.getId(), this.getUnita_operativa(), this.getDurata());
        return clone;
    }
}
