/**
 * Created by loujian on 8/28/17.
 */

import ilog.concert.*;
import ilog.cplex.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;


public class main_max_SW {

    public static void main(String[] args)throws Exception
    {
        Scanner cin=new Scanner(new File("test_in"));
        File writename = new File("test_out");
        writename.createNewFile();
        BufferedWriter out = new BufferedWriter(new FileWriter(writename));

        int N= cin.nextInt(); //the number of players
        int[][] utility = new int[N][N]; //utility[i][j]

        //input the data from the file
        for(int i=0; i<N; i++)
            for(int j=0; j<N; j++)
                utility[i][j]=cin.nextInt();

        //max_SW MSW= new max_SW(N, utility);
        //MSW.solve_problem();

        out.flush();
        out.close();

    }

}
