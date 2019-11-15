import java.io.*;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Scanner;

public class LogicAgent
{
    private static ArrayList<String> KB;
    private static String[] Queries;
    public static void main(String[] args) throws IOException
    {
        readInput();
        BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));
        for (int i = 0; i < Queries.length; i++)
        {
            writer.write(process(Queries[i]));
        }
        String a = KB.get(0);
    }
    private static void readInput() throws IOException
    {
        BufferedReader input = new BufferedReader(new FileReader("input.txt"));
        int count = Integer.parseInt(input.readLine());
        Queries = new String[count];
        for (int i = 0; i < count; i++)
        {
            Queries[i] = input.readLine();
        }

        count = Integer.parseInt(input.readLine());
        KB = new ArrayList<>(count);
        for (int i = 0; i < count; i++)
        {
            KB.add(input.readLine());
        }
    }
    private static String process(String query)
    {

        return "";
    }


}
