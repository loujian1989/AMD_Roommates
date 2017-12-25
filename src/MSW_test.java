import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloMPModeler;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import java.util.*;

/**
 * Created by loujian on 11/18/17.
 * It is just for the test of maximizing social welfare. It is for the monotonic analysis
 */
public class MSW_test {

    int N;
    double[][] utility;
    IloCplex cplex;
    IloIntVar[][] var;
    IloRange[][] rng;
    List<Map<Double, Integer>> maps;
    int[] teammates;

    MSW_test(int N, double[][] utility) {
        this.N = N;

        teammates = new int[N];
        for (int i = 0; i < N; i++)
            teammates[i] = i;

        this.utility = new double[N][N];
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++)
                this.utility[i][j] = utility[i][j];

        this.maps = maps;

        try {
            cplex = new IloCplex();
            var = new IloIntVar[1][];
            rng = new IloRange[3][];

        } catch (IloException e) {
            System.err.println("Concert exception caught: " + e);
        }
    }

    public double solve_problem() {
        double result = 0;
        try {

            populateByRow(cplex, var, rng);

            if (cplex.solve()) {
                double[] x_value = cplex.getValues(var[0]);

                for (int i = 0; i < N * N; i++) {
                    if (Math.abs(x_value[i] - 1.0) < 0.000001) {
                        teammates[i / N] = i % N;
                        teammates[i % N] = i / N;
                    }
                }

                result = cplex.getObjValue();

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

    int[] getTeammates()
    {
        return teammates;
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

}
