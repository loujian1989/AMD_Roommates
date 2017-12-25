/**
 * Created by loujian on 11/18/17.
 * It is the main function for testing MSW mechanism, it is for the monotonic analysis
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class main_MSW_test {

    public static void main(String[] args)throws Exception
    {

        Scanner cin=new Scanner(new File("test_in"));
        File writename = new File("test_out");
        writename.createNewFile();
        BufferedWriter out = new BufferedWriter(new FileWriter(writename));

        int N= 4; //the number of players

        double[][] utility = new double[N][N]; //utility[i][j]
        //List<Map<Double, Integer>> maps= new ArrayList<>();

        for(int i=0; i<N; i++)
        {
            Integer number_nei= cin.nextInt();
            out.write(number_nei+ " ");
            for(int j=0; j<number_nei; j++)
            {
                Map<Double, Integer> map= new HashMap<>();
                Integer tmp= cin.nextInt();
                tmp--;
                out.write(tmp+" ");
                utility[i][tmp]= (double) (number_nei-j)/number_nei; //we set utility to be normalized utility
                //map.put(utility[i][tmp], tmp);
                //maps.add(map);
            }
            out.write("\r\n");
        }

        MSW_test test_obj= new MSW_test(N, utility);

        test_obj.solve_problem();
        int[] teammates= test_obj.getTeammates();
        for(int i=1; i<=N; i++)
        {
            int teammate= teammates[i-1]+1;
            System.out.print(i + " " + teammate + "\r\n");
        }


    }

}
