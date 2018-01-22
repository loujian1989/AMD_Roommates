/**
 * Created by loujian on 12/25/17.
 * It is for the permutation IC program
 */

import ilog.concert.*;
import ilog.cplex.IloCplex;
import java.util.*;

public class permutation_IC {

    int N;
    double[][] utility;
    //IloCplex cplex;
    IloIntVar[][] var;
    IloRange[][] rng;
    IloNumVar epsilon;
    int[] teammates;
    double object_value;
    double e=0; //here e means the final epsilon value
    double alpha; //here alpha means the tradeoff between social welfare and incentive

    permutation_IC(int N, double[][] utility, double alpha)
    {
        this.N= N;
        this.utility = new double[N][N];
        for(int i=0; i<N; i++)
            for(int j=0; j<N; j++)
                this.utility[i][j]=utility[i][j];
        teammates= new int[N];

        this.alpha= alpha;

    }


    public void setUtility(double[][] payoff)
    {
        for(int i=0; i<N; i++)
            for(int j=0; j<N; j++)
            {
                utility[i][j]= payoff[i][j];
            }
    }


    public double solve_problem() {
        object_value=0;
        try {

            IloCplex cplex = new IloCplex();
            var = new IloIntVar[1][];
            rng = new IloRange[4][]; //here we need to add the permutation IC constraints
            epsilon= cplex.numVar(0, Double.MAX_VALUE);

            populateByRow(cplex, var, rng, epsilon);
            if (cplex.solve()) {
                double[] x_value = cplex.getValues(var[0]);
                e= cplex.getValue(epsilon);
                for (int i = 0; i < N * N; i++) {
                    if (Math.abs(x_value[i] - 1.0) < 0.000001) {
                        teammates[i / N] = i % N;
                        teammates[i % N] = i / N;
                    }
                }

                object_value = cplex.getObjValue();

            }

        } catch (IloException e) {
            System.err.println("Concert exception caught: " + e);
        }

        return object_value;
    }

    int[] getTeammates()
    {
        return teammates;
    }

    double getSW() //get the social welfare of the system
    {
        return (object_value+(N* e*alpha))/(1-alpha);
    }

    double getObject_value()
    {
        return object_value;
    }

    double getEpsilon()
    {
        return e;
    }

    void populateByRow(IloMPModeler model, IloIntVar[][]var, IloRange[][]rng, IloNumVar epsilon)throws IloException
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
        rng[3]= new IloRange[N*N];

        //we would like to maximize the social welfare
        model.addMaximize(model.sum( model.prod(1-alpha, model.scalProd(x, objvals)), model.prod(N*alpha, model.negative(epsilon))));

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

        //add constraint: \sum_k x_{ik} u_{ik} \geq u_{ij}-\epsilon, \forall i, j\in N, j\in R_i
        for(int i=0; i<N; i++)
            for(int j=0; j<N; j++)
            {
                if(utility[i][j]<=0)
                    continue;
                double[] local_obj= new double[N*N];
                for(int k=0; k<N; k++)
                    local_obj[i*N+k] = utility[i][k];
                rng[3][i*N+j]= model.addGe(model.sum(model.scalProd(x, local_obj), epsilon)  , objvals[i*N+j]);
            }
    }


}
