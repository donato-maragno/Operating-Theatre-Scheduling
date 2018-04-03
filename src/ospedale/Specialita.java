/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ospedale;

class Specialita implements Comparable<Specialita>{
    protected int id;
    protected String name;
    
    Specialita (int id, String name){
        this.id = id;
        this.name = name;
    }
    
    public void setId(int id){
        this.id = id;
    }
    public void setName(String name){
        this.name = name;
    }
    public int getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    
    /**
     * Confronta l'oggetto chiamante con la specialità in argomento.
     * @param obj
     * @return true se le due specialità sono identificate dallo stesso id
     */
    @Override
    public boolean equals(Object obj){
        boolean r = false;
        if (obj instanceof Specialita){
            Specialita b = (Specialita) obj; 
            if(this.id == b.getId())
                r = true;
        }
        return r;
    }

    /**
     * Comparo l'istanza con l'argomento passato. La funzione è utilizzata per il TreeSet che ha bisogno
     * di creare un albero ordinato.
     * @param o
     * @return 
     */
    @Override
    public int compareTo(Specialita o) {
        int r = 0;
        if(!this.equals(o))
            r = this.id - o.getId();
        return r;
    }
}
