/**
 * Created by loujian on 1/8/18.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.util.*;

public class generate_random_profile {

    public static void main(String[] args)throws Exception {

        File writename = new File("n6_1000_preference.txt");
        writename.createNewFile();
        BufferedWriter out = new BufferedWriter(new FileWriter(writename));

        int num_cases=1000;
        int n= 6;
        for(int iter=0; iter<num_cases; iter++) {
            for(int i=1; i<=n; i++)
            {
                LinkedList<Integer> list= new LinkedList<>();
                for(int j=1; j<=n; j++)
                    list.add(j);
                list.remove((Integer)i);
                Collections.shuffle(list);
                list.add(0, n-1);
                for(Integer num: list)
                    out.write(num+ " ");
                out.write("\r\n");
            }
            out.write("\r\n");
        }

        out.flush();
        out.close();

    }

}
