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

        Scanner cin = new Scanner(new File("SN_Scale_Free_1000_n10m2.txt"));
        File writename = new File("ICPO_SN_Scale_Free_n10m2_1000_alpha050.txt");
        writename.createNewFile();
        BufferedWriter out = new BufferedWriter(new FileWriter(writename));

        int num_cases = 1000;
        int N = 10; //the number of players
        double alpha = 0.5;

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

            permutation_IC ic_obj = new permutation_IC(N, utility, alpha);

            ic_obj.solve_problem();
            int[] teammates = ic_obj.getTeammates();
            for (int i = 1; i <= N; i++) {
                int teammate = teammates[i - 1] + 1;
                out.write(i + " " + teammate + "\r\n");
            }

            double social_welfare = ic_obj.getSW()/N;
            double epsilon= ic_obj.getEpsilon();
            out.write("The average social welfare is " + social_welfare + "\r\n");


            /*************************************************/
            //here it is the experiment for the epsilon
            /*
            double real_epsilon=0;

            for(int idev=0; idev< num_deviate; idev++) {

                //random get a player to deviate
                int deviate_player = rd.nextInt(N);
                double current_utility= utility[deviate_player][teammates[deviate_player]];
                //store the current utility and linked_value
                int tmp_length = utility[deviate_player].length;
                double[] tmp_utility_deviate = new double[tmp_length];
                for (int i = 0; i < tmp_length; i++)
                    tmp_utility_deviate[i]= utility[deviate_player][i];
                LinkedList<Integer> tmp_linked_value= new LinkedList<>(linked_value.get(deviate_player));
                Collections.shuffle(tmp_linked_value); //shuffle the current order
                int number_nei= tmp_linked_value.size();
                for(int j=0; j< number_nei; j++)
                {
                    int tmp= tmp_linked_value.get(j);
                    utility[deviate_player][tmp]= (double)(number_nei - j)/ number_nei;
                }

                double[][] para_utility = new double[N][N];
                for(int i=0; i<N; i++)
                    for(int j=0; j<N; j++)
                        para_utility[i][j]= utility[i][j];
                permutation_IC deviate_ic_obj= new permutation_IC(N, para_utility, alpha);

                deviate_ic_obj.solve_problem();
                int[] deviate_teammate= deviate_ic_obj.getTeammates();

                double deviate_utility= tmp_utility_deviate[deviate_teammate[deviate_player]];

                real_epsilon= Math.max(real_epsilon, deviate_utility- current_utility);

                for(int i=0; i<tmp_length; i++)
                    utility[deviate_player][i]= tmp_utility_deviate[i];


            }
            */

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

                    permutation_IC deviate_ic_obj= new permutation_IC(N, new_utility, alpha);

                    deviate_ic_obj.solve_problem();
                    int[] deviate_teammate= deviate_ic_obj.getTeammates();

                    double deviate_utility= utility[deviate_player][deviate_teammate[deviate_player]];

                    real_epsilon= Math.max(real_epsilon, deviate_utility- current_utility);

                }

            }

            out.write("The epsilon value in the program is " + epsilon + "\r\n");
            out.write("The real epsilon value in the experiment is "+ real_epsilon + "\r\n");

            //then we tries to check the real promotion

            sw_sum+= social_welfare;
            epsilon_sum+= epsilon;
            real_epsilon_sum+= real_epsilon;

        }

        out.write("The overall average social welfare is "+ sw_sum/num_cases + "\r\n");
        out.write("The overall average epsilon in the program is "+ epsilon_sum/num_cases + "\r\n");
        out.write("The overall average real epsilon in the program is "+ real_epsilon_sum/num_cases + "\r\n");
        out.flush();
        out.close();

    }

}
