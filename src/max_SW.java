/**
 * Created by loujian on 1/10/18.
 */

import ilog.concert.*;
import ilog.cplex.IloCplex;
import java.util.*;


public class max_SW {

    int N;
    double[][] utility;
    IloIntVar[][] var;
    IloRange[][] rng;
    int[] teammates;
    double object_value;

    max_SW(int N, double[][] utility)
    {
        this.N= N;
        this.utility = new double[N][N];
        for(int i=0; i<N; i++)
            for(int j=0; j<N; j++)
                this.utility[i][j]=utility[i][j];
        teammates= new int[N];

    }

    public void setUtility(double[][] payoff)
    {
        for(int i=0; i<N; i++)
            for(int j=0; j<N; j++)
            {
                utility[i][j]= payoff[i][j];
            }
    }


    public void solve_problem()
    {
        object_value=0;
        try{

            IloCplex cplex = new IloCplex();
            var = new IloIntVar[1][];
            rng = new IloRange[4][]; //here we need to add the permutation IC constraints

            populateByRow(cplex, var, rng);

            if(cplex.solve())
            {
                double[] x_value = cplex.getValues(var[0]);
                for (int i = 0; i < N * N; i++) {
                    if (Math.abs(x_value[i] - 1.0) < 0.000001) {
                        teammates[i / N] = i % N;
                        teammates[i % N] = i / N;
                    }
                }

                object_value = cplex.getObjValue();


            }

            //Promotion(1, 2);

            //cplex.end();

        } catch (IloException e) {
            System.err.println("Concert exception caught: " + e);
        }
    }

    int[] getTeammates()
    {
        return teammates;
    }

    double getSW() //get the social welfare of the system
    {
        return object_value;
    }

    void populateByRow(IloMPModeler model, IloIntVar[][]var, IloRange[][]rng)throws IloException
    {
        double[] objvals= new double[N*N];
        for(int i=0; i<N; i++)
            for(int j=0; j<N; j++) {
                objvals[i * N + j] = utility[i][j];
            }

        IloIntVar[] x= model.boolVarArray(N*N);
        var[0]=x;

        rng[0]= new IloRange[N];
        rng[1]= new IloRange[N];
        rng[2]= new IloRange[N*N];

        //we would like to maximize the social welfare
        model.addMaximize(model.scalProd(x, objvals));

        //add constraint: \sum_{j\in N} pi_{ij} \leq 1,  \forall i\in N
        for(int i=0; i<N; i++)
        {
            int[] binary_vector = new int[N*N];
            for(int j=0; j<N; j++)
                binary_vector[i*N+j]=1;
            rng[0][i]= model.addLe(model.scalProd(x, binary_vector), 1);
        }

        //add constraint: \sum_{i\in N} pi_{ij} \leq 1, \forall j\in N
        for(int j=0; j<N; j++)
        {
            int[]binary_vector= new int[N*N];
            for(int i=0; i<N; i++)
                binary_vector[i*N+j]=1;
            rng[1][j]= model.addLe(model.scalProd(x, binary_vector), 1);
        }

        //add constraint: pi_{ij}-pi_{ji}=0, \forall i, j\in N
        for(int i=0; i<N; i++)
            for(int j=0; j<N; j++)
                rng[2][i*N+j]= model.addEq( model.sum(x[i*N+j], model.negative(x[j*N+i]) ), 0);

    }
}
