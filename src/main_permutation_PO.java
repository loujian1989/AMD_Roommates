/**
 * Created by loujian on 1/14/18.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.Random;

public class main_permutation_PO {


    public static void main(String[] args)throws Exception {

        Scanner cin = new Scanner(new File("SN_Scale_Free_n20m2.txt"));
        File writename = new File("tmp_out.txt");
        writename.createNewFile();
        BufferedWriter out = new BufferedWriter(new FileWriter(writename));

        int num_cases = 100;
        int N = 20; //the number of players
        double alpha = 0.10;

        double sw_sum=0;
        double epsilon_sum=0;
        double real_epsilon_sum=0;


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

            Outcome outcome= function(N, utility, alpha, linked_value);



            out.write("The epsilon value in the program is " + outcome.epsilon + "\r\n");
            out.write("The real epsilon value in the experiment is "+ outcome.real_epsilon + "\r\n");

            //then we tries to check the real promotion

            sw_sum+= outcome.social_welfare;
            epsilon_sum+= outcome.epsilon;
            real_epsilon_sum+= outcome.real_epsilon;

            System.gc();


        }

        out.write("The overall average social welfare is "+ sw_sum/num_cases + "\r\n");
        out.write("The overall average epsilon in the program is "+ epsilon_sum/num_cases + "\r\n");
        out.write("The overall average real epsilon in the program is "+ real_epsilon_sum/num_cases + "\r\n");
        out.flush();
        out.close();

    }

    public static Outcome function(int N, double[][] utility, double alpha, List<LinkedList<Integer>> linked_value)
    {
        permutation_IC ic_obj = new permutation_IC(N, utility, alpha);

        ic_obj.solve_problem();
        int[] teammates = ic_obj.getTeammates();
        for (int i = 1; i <= N; i++) {
            int teammate = teammates[i - 1] + 1;
        }

        double social_welfare = ic_obj.getSW()/N;
        double epsilon= ic_obj.getEpsilon();


        /*************************************************/
        //here it is the experiment for the epsilon

        double real_epsilon=0;

        for(int player=0; player<N; player++) // for each player we check promotion-one
        {
            int deviate_player= player;
            double current_utility= utility[deviate_player][teammates[deviate_player]];
            if(current_utility==1)
                continue;

            double[][] new_utility= new double[N][N];
            for(int i=0; i<N; i++) {
                if(i==deviate_player)
                    continue;
                for (int j = 0; j < N; j++)
                    new_utility[i][j] = utility[i][j];
            }


            int number_nei= linked_value.get(deviate_player).size();
            for(int i=0; i<number_nei; i++) //for each possible promoted player
            {
                if(i==0)
                    continue;
                Integer mate= linked_value.get(deviate_player).get(i);
                LinkedList<Integer> tmp_linked_value= new LinkedList<>(linked_value.get(deviate_player));
                tmp_linked_value.remove(mate);
                tmp_linked_value.add(0, mate);

                for(int j=0; j<N; j++)
                    new_utility[deviate_player][j]=0;

                for(int j=0; j< number_nei; j++)
                {
                    int tmp= tmp_linked_value.get(j);
                    new_utility[deviate_player][tmp]= (double)(number_nei - j)/ number_nei;
                }


                ic_obj.setUtility(new_utility);



                ic_obj.solve_problem();
                int[] deviate_teammate= ic_obj.getTeammates();

                double deviate_utility= utility[deviate_player][deviate_teammate[deviate_player]];

                real_epsilon= Math.max(real_epsilon, deviate_utility- current_utility);

            }

        }


        ic_obj=null;
        System.gc();

        Outcome outcome= new Outcome(epsilon, real_epsilon, social_welfare);
        return outcome;
    }


}
