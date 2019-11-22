import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
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
        ArrayList<String> base = new ArrayList<>(KB);
        Queue<String> queryQueue = new LinkedList<>();
        queryQueue.add(negate(query));
        while (queryQueue.size()!=0)
        {
            if (!checkAgainstKB(queryQueue, base))
            {
                return "TRUE";
            }
            
            if (!base.contains(queryQueue.peek()))
            {
                base.add(queryQueue.poll());
            }
        }
        
        return "False";
    }
    private static boolean checkAgainstKB(Queue<String> queue, ArrayList<String> base)
    {
        for (String k: base)
        {
            //check if a contradiction arise, if found return false
            if (negate(k).equals(queue.peek()))
            {
                return false;
            }
            //if we can derive new sentences, add to the queue
            String newK = entailNewKnowledge(k, queue.peek());
            if (newK != null)
            {
                queue.add(newK);
            }
            
            
        }
        return true;
    }
    private static String entailNewKnowledge(String k, String q)
    {
        String[] implication = k.split("=>");
        
        //check for implication
        if (implication.length>1)
        {
            if (implication[0].contains(q))
            {
            
            }
        }
        
        
        
        return null;
    }
    private static String negate(String sentence)
    {
        if (sentence.length() == 0)
        {
            System.out.println("empty sentence");
            return "~";
        }
        if (sentence.charAt(0) == '~')
        {
            return sentence.substring(1);
        }
        return "~" + sentence;
    }

}
