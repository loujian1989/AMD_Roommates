import ilog.concert.*;
import ilog.cplex.IloCplex;
import java.util.*;


public class permutation_current {


    int N;
    double[][] utility;

    int[] teammates;
    double object_value;
    double epsilon_difference=0.0001;
    double e=0; //here e means the final epsilon value
    double alpha; //here alpha means the tradeoff between social welfare and incentive
    List<LinkedList<Integer>> linked_value;

    permutation_current(int N, double[][] utility, double alpha, List<LinkedList<Integer>> linked_value)
    {
        this.N= N;
        this.utility = new double[N][N];
        for(int i=0; i<N; i++)
            for(int j=0; j<N; j++)
                this.utility[i][j]=utility[i][j];
        teammates= new int[N];

        this.linked_value= new ArrayList<>();
        for(LinkedList<Integer> ls: linked_value)
            this.linked_value.add(new LinkedList<>(ls));

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
            IloIntVar[][] var;
            IloRange[][] rng;
            IloNumVar epsilon;
            var = new IloIntVar[1][];
            rng = new IloRange[4][]; //here we need to add the permutation IC constraints
            epsilon= cplex.numVar(0, Double.MAX_VALUE);

            populateByRow(cplex, var, rng, epsilon);

            double epsilon_current= 0;
            double current_SW=0;
            if(cplex.solve())
            {
                epsilon_current= cplex.getValue(epsilon);
                e= epsilon_current;
                object_value= cplex.getObjValue();
                current_SW= getSW();
            }

            //for use to add constraints
            double[] objvals= new double[N*N];
            for(int i=0; i<N; i++)
                for(int j=0; j<N; j++) {
                    objvals[i * N + j] = utility[i][j];
                }

            boolean[][] flag= new boolean[N][N]; //label whether the constraint has been added into the program
            while(true)
            {
                double epsilon_update= 0;
                for(int deviated_player=0; deviated_player<N; deviated_player++)
                {
                    double[][] new_utility= new double[N][N];
                    for(int i=0; i<N; i++) {
                        if(i==deviated_player)
                            continue;
                        for (int j = 0; j < N; j++)
                            new_utility[i][j] = utility[i][j];
                    }

                    //then we get the utility of promoted profile
                    int number_nei= linked_value.get(deviated_player).size();

                    for(int i=0; i<number_nei; i++) //for each possible promoted player
                    {
                        if (i == 0)
                            continue;
                        Integer mate= linked_value.get(deviated_player).get(i);
                        if(flag[deviated_player][mate]) //has been added into the program
                            continue;

                        LinkedList<Integer> tmp_linked_value= new LinkedList<>(linked_value.get(deviated_player));
                        tmp_linked_value.remove(mate);
                        tmp_linked_value.add(0, mate);

                        for(int j=0; j<N; j++)
                            new_utility[deviated_player][j]=0;

                        for(int j=0; j< number_nei; j++)
                        {
                            int tmp= tmp_linked_value.get(j);
                            new_utility[deviated_player][tmp]= (double)(number_nei - j)/ number_nei;
                        }

                        max_SW new_MSW= new max_SW(N, new_utility);
                        new_MSW.solve_problem();
                        double deviate_SW= new_MSW.getSW();

                        if(deviate_SW - current_SW > 0.001)
                        {
                            double[] local_obj= new double[N*N];
                            for(int k=0; k<N; k++)
                                local_obj[deviated_player*N+k] = utility[deviated_player][k];
                            rng[3][deviated_player*N + mate]= cplex.addGe(cplex.sum(cplex.scalProd(var[0], local_obj), epsilon), objvals[deviated_player*N + mate]);

                            flag[deviated_player][mate]=true;

                            if(cplex.solve())
                            {
                                epsilon_update = Math.max(cplex.getValue(epsilon), epsilon_update); //the epsilon to update
                                e= epsilon_current;
                                object_value= cplex.getObjValue();
                                current_SW= getSW();
                            }

                        }
                    }

                }
                if(Math.abs(epsilon_update- epsilon_current)<=epsilon_difference)
                    break;
                epsilon_current=epsilon_update;
            }

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

            cplex.end();

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


    }

}
