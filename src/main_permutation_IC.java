/**
 * Created by loujian on 12/25/17.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;


public class main_permutation_IC {


    public static void main(String[] args)throws Exception {

        Scanner cin = new Scanner(new File("SN_Scale_Free_n10m2.txt"));
        File writename = new File("IC_SN_Scale_Free_n10m2_alpha100.txt");
        writename.createNewFile();
        BufferedWriter out = new BufferedWriter(new FileWriter(writename));

        int num_cases = 100;
        int N = 10; //the number of players
        double alpha = 0.999999999;

        double sw_sum=0;
        double epsilon_sum=0;

        for (int iter = 0; iter < num_cases; iter++) {
            double[][] utility = new double[N][N]; //utility[i][j]

            List<LinkedList<Integer>> linked_value= new ArrayList<>();

            for (int i = 0; i < N; i++) {
                Integer number_nei = cin.nextInt();
                LinkedList<Integer> tmp_list= new LinkedList<>();
                out.write(number_nei + " ");
                for (int j = 0; j < number_nei; j++) {
                    Integer tmp = cin.nextInt();
                    tmp--;
                    tmp_list.add(tmp);
                    out.write(tmp + " ");
                    utility[i][tmp] = (double) (number_nei - j) / number_nei; //we set utility to be normalized utility
                }
                linked_value.add(tmp_list);
                out.write("\r\n");
            }

            boolean[] flag = new boolean[N];
            for (int i = 0; i < N; i++)
                flag[i] = true; //it means all players are available

            permutation_IC ic_obj = new permutation_IC(N, utility, flag, alpha);

            ic_obj.solve_problem();
            int[] teammates = ic_obj.getTeammates();
            for (int i = 1; i <= N; i++) {
                int teammate = teammates[i - 1] + 1;
                out.write(i + " " + teammate + "\r\n");
            }

            double social_welfare = ic_obj.getSW()/N;
            double epsilon= ic_obj.getEpsilon();
            out.write("The average social welfare is " + social_welfare + "\r\n");
            out.write("The epsilon value is " + epsilon + "\r\n");

            //then we tries to check the real promotion

            sw_sum+= social_welfare;
            epsilon_sum+= epsilon;

        }

        out.write("The overall average social welfare is "+ sw_sum/num_cases + "\r\n");
        out.write("The overall average epsilon is "+ epsilon_sum/num_cases + "\r\n");
        out.flush();
        out.close();

    }
}
