
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;


public class main_permutation_two_current {


    public static void main(String[] args)throws Exception {

        Scanner cin = new Scanner(new File("SN_Scale_Free_n10m2.txt"));
        File writename = new File("test_two_current_alpha100.txt");
        writename.createNewFile();
        BufferedWriter out = new BufferedWriter(new FileWriter(writename));

        int num_cases = 100;
        int N = 10; //the number of players
        double alpha = 0.9999;

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

    public static Outcome function(int N, double[][] utility, double alpha, List<LinkedList<Integer>> linked_value) {
        permutation_two_current ic_obj = new permutation_two_current(N, utility, alpha, linked_value);

        ic_obj.solve_problem();
        int[] teammates = ic_obj.getTeammates();


        double social_welfare = ic_obj.getSW() / N;
        double epsilon = ic_obj.getEpsilon();


        /*************************************************/
        //here it is the experiment for the epsilon

        double real_epsilon = 0;

        for (int player = 0; player < N; player++) // for each player we check promotion-one
        {
            int deviate_player = player;
            double current_utility = utility[deviate_player][teammates[deviate_player]];
            if (current_utility == 1)
                continue;

            double[][] new_utility = new double[N][N];
            for (int i = 0; i < N; i++) {
                if (i == deviate_player)
                    continue;
                for (int j = 0; j < N; j++)
                    new_utility[i][j] = utility[i][j];
            }


            int number_nei = linked_value.get(deviate_player).size();
            for (int i = 0; i < number_nei; i++) //for each possible promoted player
            {
                if (i == 0)
                    continue;

                Integer mate= linked_value.get(deviate_player).get(i);
                if(utility[deviate_player][mate] <= current_utility)
                    continue;

                LinkedList<Integer> tmp_linked_value= new LinkedList<>(linked_value.get(deviate_player));
                tmp_linked_value.remove(mate);
                tmp_linked_value.add(0, mate);

                List<LinkedList<Integer>> new_linked_value= new ArrayList<>();
                for(LinkedList<Integer> ls: linked_value)
                    new_linked_value.add(new LinkedList<>(ls));
                new_linked_value.set(deviate_player, new LinkedList<>(tmp_linked_value));

                for(int j=0; j<N; j++)
                    new_utility[deviate_player][j]=0;

                for(int j=0; j< number_nei; j++)
                {
                    int tmp= tmp_linked_value.get(j);
                    new_utility[deviate_player][tmp]= (double)(number_nei - j)/ number_nei;
                }

                permutation_two_current new_ic_obj= new permutation_two_current(N, new_utility, alpha, new_linked_value);
                new_ic_obj.solve_problem();

                int[] deviate_teammate = new_ic_obj.getTeammates();

                double deviate_utility = utility[deviate_player][deviate_teammate[deviate_player]];

                real_epsilon = Math.max(real_epsilon, deviate_utility - current_utility);

            }
        }
        Outcome outcome= new Outcome(epsilon, real_epsilon, social_welfare);

        ic_obj=null;
        System.gc();
        return outcome;

    }



}
