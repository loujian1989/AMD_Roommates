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


public class max_SW {

    int N;
    double[][] utility;
    double max_payoff;
    IloCplex cplex;
    IloIntVar[][] var;
    IloRange[][]rng;
    boolean[] flag;

    max_SW(int N, double[][] utility, boolean[] flag)
    {
        this.N= N;
        max_payoff=1;
        this.flag= new boolean[N];
        for(int i=0; i<N; i++)
            this.flag[i]=flag[i];

        this.utility = new double[N][N];
        for(int i=0; i<N; i++)
            for(int j=0; j<N; j++)
                this.utility[i][j]=utility[i][j];

        try{
            cplex=new IloCplex();
            var= new IloIntVar[1][];
            rng= new IloRange[3][];

        }catch (IloException e) {
            System.err.println("Concert exception caught: " + e);
        }
    }

    public double solve_problem()
    {
        double result=0;
        try{

            populateByRow(cplex, var, rng);

            if(cplex.solve())
            {
                double[]x_value= cplex.getValues(var[0]);

                //System.out.println("Solution status = " + cplex.getStatus());
                //System.out.println("Solution value = " + cplex.getObjValue());
                result= cplex.getObjValue();

                /*
                for(int i=0; i<N; i++) {
                    for (int j = 0; j < N; j++) {
                        System.out.println(x_value[i * N + j]+ " ");
                    }
                    System.out.println("\r\n");

                }
                */
            }

            cplex.exportModel("max_SW1.lp");

            //Promotion(1, 2);

            //cplex.end();

        } catch (IloException e) {
            System.err.println("Concert exception caught: " + e);
        }

        return result;
    }

    void populateByRow(IloMPModeler model, IloIntVar[][]var, IloRange[][]rng)throws IloException
    {

        double[] objvals= new double[N*N];
        for(int i=0; i<N; i++)
            for(int j=0; j<N; j++)
                objvals[i*N+j] = utility[i][j]; //here is the objective function

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

    double Promotion(int a, int b)
    {
        double result= max_payoff+ utility[b][a];
        try {
            rng[0][a].setUB(0);
            rng[0][b].setUB(0);
            rng[1][a].setUB(0);
            rng[1][b].setUB(0);

            cplex.exportModel("max_SW1.lp");

            if(cplex.solve())
            {
                //System.out.println("Solution status = " + cp.getStatus());
                //System.out.println("Solution value = " + cp.getObjValue());

                result+= cplex.getObjValue();
                double[] x_value= cplex.getValues(var[0]);
                //for(int i=0; i<x_value.length; i++)
                //    System.out.println(x_value[i]+ " ");

            }

            rng[0][a].setUB(1);
            rng[0][b].setUB(1);
            rng[1][a].setUB(1);
            rng[1][b].setUB(1);
            cplex.exportModel("max_SW2.lp");



        }catch (IloException e) {
            System.err.println("Concert exception caught: " + e);
        }

        return result;
    }


    /*
    double Promotion(int a, int b)
    {
        double result= (max_payoff+ utility[b][a]);

        try {

            IloCplex cp= new IloCplex();

            IloCopyManager cm= new IloCopyManager(cplex);
            IloRange[][] rng_new= new IloRange[3][];
            rng_new[0]= new IloRange[N];
            rng_new[1]= new IloRange[N];
            rng_new[2]= new IloRange[N*N];

            for(int i=0; i<rng[0].length; i++)
                rng_new[0][i]= (IloRange) cp.add((IloAddable) rng[0][i].makeCopy(cm));
            for(int i=0; i<rng[1].length; i++)
                rng_new[1][i] = (IloRange) cp.add((IloAddable) rng[1][i].makeCopy(cm));
            for(int i=0; i<rng[2].length; i++) {
                if(rng[2][i]!=null)
                    rng_new[2][i]= (IloRange) cp.add((IloAddable) rng[2][i].makeCopy(cm));
            }

            IloObjective obj = (IloObjective)cplex.getObjective().makeCopy(cm);
            double[][] utility_new= new double[N][N];
            for(int i=0; i<N; i++)
            {
                for(int j=0; j<N; j++)
                {

                    if(i==a || i==b || j==a || j==b)
                        utility_new[i][j]=0;
                    else
                        utility_new[i][j]=utility[i][j];
                }
            }
            double[] objvals_new= new double[N*N];
            for(int i=0; i<N; i++)
                for(int j=0; j<N; j++)
                    objvals_new[i*N+j] = utility_new[i][j]; //here is the objective function

            //cp.addMaximize(cp.scalProd(var[0], objvals));
            cp.add(obj);
            cp.setLinearCoefs(obj, objvals_new, var[0]);
            rng_new[0][a].setUB(0);
            rng_new[0][b].setUB(0);
            rng_new[1][a].setUB(0);
            rng_new[1][b].setUB(0);


            cp.exportModel("max_SW2.lp");
            if(cp.solve())
            {
                //System.out.println("Solution status = " + cp.getStatus());
                //System.out.println("Solution value = " + cp.getObjValue());

                result+= cp.getObjValue();

                double[] x_value= cp.getValues(var[0]);


                for(int i=0; i<x_value.length; i++)
                    System.out.println(x_value[i]+ " ");

            }

            cp.end();

        }catch (IloException e) {
            System.err.println("Concert exception caught: " + e);
        }
        return result;
    }
    */


}
