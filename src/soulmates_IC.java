import ilog.concert.*;
import ilog.cplex.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;


/**
 * Created by loujian on 8/29/17.
 */
public class soulmates_IC {

    int N;
    double alpha;
    double[][] utility;
    int[] admire; //list the most preferred player that admire player i
    double max_payoff; //it means the maximum payoff a player could get, it can be changed
    boolean[] flag;
    double obj_value=0; //here obj_value means the final obj value of the integer program
    double e=0; //here e means the final epsilon value

    soulmates_IC(int N, double[][] utility, boolean[] flag, double alpha)
    {
        this.N= N;
        this.alpha= alpha;
        this.max_payoff= 1; //it means the maximum payoff a player could get, it can be changed
        this.utility = new double[N][N];
        for(int i=0; i<N; i++)
            for(int j=0; j<N; j++)
                this.utility[i][j]=utility[i][j];

        this.admire= new int[N];
        for(int i=0; i<N; i++)
            this.admire[i]=-1; //default set no one admires the player
        for(int i=0; i<N; i++) {
            if(flag[i]==false)
                continue;
            double cur_max= -1;
            for (int j = 0; j < N; j++) {
                if(flag[j]==false)
                    continue;
                if(utility[j][i]==max_payoff && utility[j][i]> cur_max)
                {
                    cur_max= utility[j][i];
                    admire[i]=j; //it is most preferred player that admires i;
                }
            }
        }
        this.flag= new boolean[N];
        for(int i=0; i<N; i++)
            this.flag[i]=flag[i];
    }



    public int solve_problem()
    {
        try{


            IloCplex cplex = new IloCplex();

            //pi is binary variable
            IloIntVar[] pi= cplex.boolVarArray(N*N);

            //epsilon is a double variable
            IloNumVar epsilon = cplex.numVar(0, Double.MAX_VALUE);

            double[] objvals= new double[N*N];
            for(int i=0; i<N; i++)
                for(int j=0; j<N; j++) {
                    if(flag[i]==false || flag[j]==false)
                        objvals[i * N + j]=0;
                    else
                        objvals[i * N + j]= utility[i][j];
                }

            //we would like to maximize the social welfare minus epsilon
            cplex.addMaximize( cplex.sum( cplex.prod(1-alpha, cplex.scalProd(pi, objvals)), cplex.prod(N*alpha, cplex.negative(epsilon)))); //here the weight of alpha need to be revised


            //add constraint: \sum_{j\in N} pi_{ij} \leq 1,  \forall i\in N
            for(int i=0; i<N; i++)
            {
                if(flag[i]==false)
                    continue;
                int[] binary_vector = new int[N*N];
                for(int j=0; j<N; j++) {
                    if(flag[j]==true)
                        binary_vector[i * N + j] = 1;
                }
                cplex.addLe(cplex.scalProd(pi, binary_vector), 1);
            }

            //add constraint: \sum_{i\in N} pi_{ij} \leq 1, \forall j\in N
            for(int j=0; j<N; j++)
            {
                if(flag[j]==false)
                    continue;
                int[]binary_vector= new int[N*N];
                for(int i=0; i<N; i++)
                    if(flag[i]==true)
                        binary_vector[i*N+j]=1;
                cplex.addLe(cplex.scalProd(pi, binary_vector), 1);
            }

            //add constraint: pi_{ij}-pi_{ji}=0, \forall i, j\in N
            for(int i=0; i<N; i++)
                for(int j=0; j<=i; j++) {
                    if(flag[i]==true && flag[j]==true)
                    cplex.addEq(cplex.sum(pi[i * N + j], cplex.negative(pi[j * N + i])), 0);
                }


            //here add the soulmates IC constraint
            for(int i=0; i<N; i++)
            {
                if(flag[i]==false)
                    continue;
                if(admire[i]!=-1)
                {
                    int k= admire[i];
                    double[] local_obj= new double[N*N];
                    for(int j=0; j<N; j++)
                        local_obj[i*N+j] = utility[i][j];
                    cplex.addGe(cplex.sum(cplex.scalProd(pi, local_obj), epsilon)  , objvals[i*N+k]);
                }
            }

            cplex.exportModel("soulmates_IC.lp");

            if(cplex.solve())
            {
                System.out.println("Solution status = " + cplex.getStatus());
                obj_value= cplex.getObjValue();
                System.out.println("Solution value = " + obj_value);

                double[] pi_value= cplex.getValues(pi);
                e= cplex.getValue(epsilon); //here is the epsilon value

                for(int i=0; i<N; i++) {
                    for (int j = 0; j < N; j++) {
                        System.out.println(pi_value[i * N + j]+ " ");
                    }
                    System.out.println("\r\n");

                }
                System.out.println("The epsilon is " + e);
            }

            cplex.end();



        } catch (IloException e) {
            System.err.println("Concert exception caught: " + e);
        }

        return 0;
    }

    double get_sw()
    {
        return (obj_value+(N* e*alpha))/(1-alpha);
    }
    double get_epsilon()
    {
        return e;
    }


}
