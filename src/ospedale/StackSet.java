
package ospedale;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;
import javafx.util.Pair;

/**
 *
 * @author Donato
 */
public class StackSet{
    LinkedList<Pair<Sala,Paziente>> s;
    
    public StackSet(){
        s = new LinkedList<Pair<Sala,Paziente>>();
    }
    
    public void push (Pair<Sala,Paziente> new_item){
        boolean thereis = false;
        Iterator<Pair<Sala,Paziente>> it = this.s.iterator();
        while(it.hasNext()){
            Pair<Sala,Paziente> item = it.next();
            if(new_item.getValue().equals(item.getValue()))
                thereis = true;
        }
        
        if(!thereis)
            this.s.push(new_item);
    }
    
    public Pair<Sala,Paziente> remove(){
        return this.s.remove();
    }
    
    public boolean isEmpty(){
        return this.s.isEmpty();
    }
    
}
