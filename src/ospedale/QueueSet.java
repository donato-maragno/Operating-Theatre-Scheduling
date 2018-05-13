
package ospedale;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;
import javafx.util.Pair;

/**
 *
 * @author Donato
 */
public class QueueSet{
    LinkedList <Pair<Sala,Pair<Paziente, Integer>>> s;
    
    public QueueSet(){
        s = new LinkedList <Pair<Sala,Pair<Paziente, Integer>>>();
    }
    
    public void push (Pair<Sala,Pair<Paziente, Integer>> new_item){
        boolean thereis = false;
        Iterator<Pair<Sala,Pair<Paziente, Integer>>> it = this.s.iterator();
        while(it.hasNext()){
            Pair<Sala,Pair<Paziente, Integer>> item = it.next();
            if(new_item.getValue().equals(item.getValue()))
                thereis = true;
        }
        
        if(!thereis)
            this.s.push(new_item);
    }
    
    public Pair<Sala,Pair<Paziente, Integer>> remove(){
        return this.s.pollLast();
    }
    
    public Pair<Sala,Pair<Paziente, Integer>> prendiUltimo(){
        return this.s.getFirst();
    }
    
    public boolean isEmpty(){
        return this.s.isEmpty();
    }
    
}
