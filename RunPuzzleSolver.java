//AI_HW1

/**
 *
 * @authors 
 * Ninad Khalate
 * Mukesh Kumar Sunder
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RunPuzzleSolver {
    static String Heuristic;                                                    
    static int tmax;                                                            
    static List<int[]> neighbours;
    static HashMap<int[],Integer> g_val = new HashMap<>(); 
    static HashMap<int[],Integer> f_val = new HashMap<>(); 
    static HashMap<int[],int[]> path = new HashMap<>();
    static int col;
    static HashMap<int[],Integer> p = new HashMap<>();
    static HashMap<int[],Character> move = new HashMap<>();
    static int[] state_array=null;                                              
    static String outFile;
    
    
    
    public static void main(String[] args){
        String inFile;     
        FileReader f=null;
        inFile=args[0]+".txt";
        Heuristic=args[1];
        tmax=Integer.parseInt(args[2]);
        outFile=args[3]+".txt";
        int rows,cols;
        try {
            f=new FileReader(inFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(RunPuzzleSolver.class.getName()).log(Level.SEVERE, null, ex);
        }
        Scanner sc=new Scanner(f);
        rows=sc.nextInt();
        cols=sc.nextInt();
        
        state_array=new int[rows*cols];
        for(int i=0;i<rows*cols;i++)
            state_array[i]=sc.nextInt();        
        int pos=0;
        for(int i=0;i<state_array.length;i++){
            if(state_array[i]==0)
                pos=i;
        }
        col=cols;
        p.put(state_array,pos);
        move.put(state_array,' ');
        System.out.println("Intial State: "+printState(state_array));
        if (!isSolvable(state_array)) {
            
            System.out.printf("Given puzzle: "+printState(state_array)+" is NOT solvable!\n" );
            System.exit(0);
        }
        long startTime = System.currentTimeMillis();    
        solve(state_array);
        long solveTime = System.currentTimeMillis() - startTime;
        System.out.println("Time taken: "+ solveTime+ " ms");
    }
    
    /**
     * This method implements the A* algorithm
     * @param start the start state from which the goal state is to 
     * be reached
     */
    
    static void solve(int[] start){
        List<int[]> closed = new ArrayList<>();  
        List<int[]> open = new ArrayList<>(); 
        open.add(start);
        Min(open);
        g_val.put(start,0);   
        f_val.put(start,getHeuristic(start,col));
        System.out.println("Solving...");
        long start_time = System.currentTimeMillis();
        long cutOff_time = start_time + tmax*1000;
        boolean solved=false;
        while(!open.isEmpty()&&(System.currentTimeMillis() < cutOff_time)){
            int[] current=open.get(0);
            if(isSolved(current)){
                System.out.println("Solved state: "+printState(current));
                String steps=allSteps(current);
                File file = new File(outFile);
                PrintStream out;
                try {
                    out = new PrintStream(new FileOutputStream(outFile));
                    System.out.println("steps "+(steps.length())+"\n"+steps);                
                    char[] st=steps.toCharArray();
                    for(int i=0;i<st.length;i++)
                        out.println(st[i]);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(RunPuzzleSolver.class.getName()).log(Level.SEVERE, null, ex);
                }
                solved = true;
                return;
            }
             open.remove(current);
             closed.add(current);
            getNeighbours(current);
            int temp;
            for (int[] neighbour : neighbours) {
                if(lcontains(closed,neighbour)){
                    continue;
                }
                temp=g_val.get(current)+1;
                if(!lcontains(open,neighbour)){
                    open.add(neighbour);
                    Min(open);
                    //System.out.println("Opening :"+printState(neighbour)+"with move :"+move.get(neighbour));
                }
                else if(temp>=g_val.get(neighbour)){
                    continue;
                }
                path.put(neighbour,current);
                g_val.replace(neighbour, temp);
                f_val.replace(neighbour,getHeuristic(neighbour,col));    
            }
        }
        if(!solved)
            System.out.println("tmax reached");
    }
    
    /**
     * Converts the given state into a string that can be printed
     * @param s the state the is to be converted to string
     * @return String - the converted format of the state
     */
    
    static String printState(int[] s){
        String result="";
        for(int i=0;i<s.length;i++)
            result=result+s[i]+" ";
        return result;
    }
    
    /**
     * Gives the the heuristic value of the provided state based on what
     * heuristic is to be used 
     * @param array the state of which the heuristic is to be generated
     * @param cols number of columns
     * @return int - the heuristic value for the given state
     */
    
    static int getHeuristic(int[] array,int cols) {
        int heuristic=0; 
        if(Heuristic.equalsIgnoreCase("H1"))
            heuristic = misplacedTiles(array);
        if(Heuristic.equalsIgnoreCase("H2")){
            for(int i = 0; i < array.length; i++) {
                if (array[i] != 0)
                    heuristic += getManhattanDistance(i, array[i],cols);
            }           
        } 
        if(Heuristic.equalsIgnoreCase("H3"))
            heuristic = tilesOutOfRowColumn(array, cols);
        return heuristic;
    }
    
    /**
     * Generates the manhattan distance of a tile
     * @param index current index of the tile
     * @param number tile number
     * @param cols number of columns for the state
     * @return int - manhattan distance of the tile from its intended position
     */
    
    static int getManhattanDistance(int index, int number,int cols) {
        return Math.abs((index / cols) - ((number) / cols)) + Math.abs((index % cols) - ((number) % cols));
    }
    
    /**
   * This method calculates the number of misplaced tiles in the current state
   * with respect to the goal state.
   * @param state puzzle state array.
   * @return int - Number of misplaced tiles in the state.
   */
    static int misplacedTiles(int[] state){
        int h=0;
        for (int i=0;i<state.length;i++){
            if(state[i]!=i)
                h++;
        }
        return h;
    }
    
    /**
     * Generates a heuristic value of the given state with respect to 
     * how many tiles are out of their rows and columns respectively
     * @param state the state for which the heuristic to be generated
     * @param cols the number of columns
     * @return int - number of tiles out of rows and column
     */
    
    static int tilesOutOfRowColumn(int[] state,int cols){
        int h=0;
        int n;
        int rows=state.length/cols; 
        for(int i=0;i<state.length;i++)
        {
            if(((i)%cols)!=(state[i]%cols))
                h++;              
        }
        for(int i=0;i<state.length;i++){
            for(n=1;n<rows;n++){
                if((i)<(cols*n)&&state[i]>cols*n)
                    h++;
            }
        }
        return h;
    }
    
    /**
     * Checks if the given state is the  goal state or solved state
     * @param state which is to be checked
     * @return true if its a solved state, false otherwise
     */
    
    static boolean isSolved(int[] state) {
        int[] p = state;
        for (int i = 0; i < p.length; i++)
            if(p[i]!=i) return false;
        return (p[0] == 0);
    }
    
    /**
     * Generates all the possible neighboring states for the given state
     * @param state of which the neighbors are to be generated
     */
    
    static void getNeighbours(int[] state){
        neighbours=new ArrayList<int[]>();
        addToNeighbour(left(state));
        addToNeighbour(right(state));
        addToNeighbour(up(state));
        addToNeighbour(down(state));
    }
    
    /**
     * Adds the state as a neighbor if its not null state
     * @param state to be added as neighbor
     */
    
    static void addToNeighbour(int[] state){
        if(state != null){
            neighbours.add(state);
            g_val.put(state,999999999);
            f_val.put(state,999999999);
        }
    }
    
    /**
     * Generates a String with all the recorded steps from the beginning
     * @param s the final state from which the path has to be traced
     * @return String with all the steps performed till now
     */
    
    static String allSteps(int[] s){
        String steps=""+move.get(s);
        while(path.get(s)!=state_array){
            steps=move.get(path.get(s))+steps;
            s=path.get(s);
        }
        return steps;
    }
    
    /**
     * Takes a list as input and returns a sorted list in an ascending order
     * @param l list to be sorted
     * @return list in an ascending order
     */
    
    static List Min(List<int[]> l){
        Collections.sort(l, new Comparator<int[]>(){
            @Override
            public int compare(int[] state1, int[] state2) {
                return f_val.get(state1)- f_val.get(state2);}
        });
        return l;
    }
    
    /**
   * Returns a new state with the blank space swapped
   * with the tile to the right.
   * @param state The state being operated on.
   * @return null if the state is invalid, or else the new state.
   */
    
    static int[] left(int[] state) {
        if((p.get(state)% col > 0)){
            int[] new_state=new int[state.length];
            System.arraycopy(state, 0, new_state, 0, state.length);
            int pos = p.get(state) - 1;
            new_state[p.get(state)]=state[pos];
            new_state[pos] = 0;
            p.put(new_state, pos);
            move.put(new_state,'0');
            return new_state;
        }
        return null;
    }
    
    /**
   * Returns a new state with the blank space swapped
   * with the tile to the left.
   * @param state The state being operated on.
   * @return null if the state is invalid, or else the new state.
   */
          
    static int[] right(int[] state) {
        if((p.get(state) % col < col-1)){
            int[] new_state=new int[state.length];
            System.arraycopy(state, 0, new_state, 0, state.length);
            int pos = p.get(state) + 1;
            new_state[p.get(state)]=state[pos];
            new_state[pos] = 0;
            p.put(new_state, pos);
            move.put(new_state,'1');
            return new_state;
        }
        return null;
    }
    
    /**
   * Returns a new state with the blank space swapped
   * with the tile above it.
   * @param state The state being operated on.
   * @return null if the state is invalid, or else the new state.
   */
    
    static int[] up(int[] state) {       
        if((p.get(state)) > col-1){
            int[] new_state=new int[state.length];
            System.arraycopy(state, 0, new_state, 0, state.length);
            int pos = p.get(state) - col;
            new_state[p.get(state)]=state[pos];
            new_state[pos] = 0;
            p.put(new_state, pos);
            move.put(new_state,'2');
            return new_state;
        }
        return null;
    }
    
    /**
   * Returns a new state with the blank space swapped
   * with the tile below it.
   * @param state The state being operated on.
   * @return null if the state is invalid, or else the new state.
   */
    
    
    static int[] down(int[] state) {        
        if((p.get(state) < (state.length-col))){ 
            int[] new_state=new int[state.length];
            System.arraycopy(state, 0, new_state, 0, state.length);
            int pos = p.get(state) + col;
            new_state[p.get(state)]=state[pos];
            new_state[pos] = 0;
            p.put(new_state, pos);
            move.put(new_state,'3');
            return new_state;
        }
        return null;
    }
    
    /**
    * Checks if the given array configuration is solvable.
    * @param List to be searched
    * @param int[] that has to be searched
    * returns true if its solvable false otherwise
    */
    
    static boolean lcontains(List<int[]> o,int[] s){
        Iterator<int[]> it=o.iterator();
        int[] n;
        boolean b=false;
        while(it.hasNext()){
            n=it.next();
            loop:for(int i=0;i<n.length;i++){
                if(n[i]==s[i]){
                    b=true;
                }
                else{
                    b=false;
                    break loop;
                }
            }
            if(b){
                return true;
            }
        }
        return false;
    }
    
    /**
    * Checks if the given array configuration is solvable.
    * @param state_array
    * returns true if its solvable false otherwise
    */
    
    static boolean isSolvable(int []state_array) {
        int inversions = 0;
        int[] p = state_array;
        for(int i = 0; i < p.length - 1; i++) {
            for(int j = i + 1; j < p.length; j++)
                if(p[i] > p[j]) inversions++;
            if(p[i] == 0 && i % 2 == 1) inversions++;
        }
        return (inversions % 2 == 0);
    }
}
