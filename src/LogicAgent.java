import java.io.*;
import java.util.*;

public class LogicAgent
{
    private static ArrayList<String> KB;
    private static String[] Queries;
    public static void main(String[] args) throws IOException
    {
//        System.out.println(Arrays.toString(parseFunction("~Alert(Bob,NSAIDs)")))
//        String[] a = parseFunction("Alert(Bob,NSAIDs)");
//        String[] b = parseFunction("Alert(x,NSAIDs)");
//        System.out.println(entailNewKnowledge("Parent(x,y) & Ancestor(y,z) => Ancestor(x,z)", "~Ancestor(Liz,Billy)"));
        readInput();
        BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));
        for (int i = 0; i < Queries.length; i++)
        {
            writer.write(process(Queries[i])+"\n");
        }
        writer.close();
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
            }else
            {
                queryQueue.poll();
            }
        }
        
        return "FALSE";
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
            ArrayList<String> newK = entailNewKnowledge(k, queue.peek());
//            System.out.println(newK);
            if (newK != null)
            {
                for (String s: newK)
                {
                    if (!base.contains(s))
                    {
                        queue.add(s);
                    }
                }

            }
        }
        return true;
    }
    private static ArrayList<String> entailNewKnowledge(String k, String q)
    {
        ArrayList<String> result = new ArrayList<>();
        if (k.contains(" => ") || q.contains(" => "))
        {

            String[] kImplication = k.split(" => ");
            String[] qImplication = q.split(" => ");
//            System.out.println("=>");
            if (kImplication.length>1 && qImplication.length>1)
            {
                return null;
            }
            if (kImplication.length>1)
            {
//                System.out.println("k imply");
                return imply(kImplication, q);
            }
            if (qImplication.length>1)
            {
                return imply(qImplication, k);
            }
        }

        if (k.contains(" | ") || q.contains(" | "))
        {
            String[] kNegateOr = k.split(" \\| ");
            String[] qNegateOr = q.split(" \\| ");

            if (kNegateOr.length>1 && qNegateOr.length>1)
            {
                return null;
            }
            if (kNegateOr.length>1)
            {
                result.add(negateOr(kNegateOr, q));
                return result;
            }
            if (qNegateOr.length>1)
            {
                result.add(negateOr(qNegateOr, k));
                return result;
            }
        }

        Map<String, String> unification = new HashMap<>();
        String[] funcK = parseFunction(k);
        String[] funcQ = parseFunction(q);
        if (unify(funcK, funcQ, unification))
        {
            result.add(assembleFunction(funcK, unification));
            return result;
        }
        return null;
    }
    private static ArrayList<String> imply(String[] implication, String b)
    {
        ArrayList<String> result = new ArrayList<>();
        if (b.contains("|"))
        {
//            System.out.println("=> |");
            String[] or = b.split(" \\| ");
            ArrayList<String> list = new ArrayList<>();
            String[] function = parseFunction(implication[1]);
            if (!b.contains(function[0]))
            {
                return null;
            }
            for (String value : or)
            {
                Map<String, String> unification = new HashMap<>();
                if (unify(parseFunction(implication[1]), parseFunction(value), unification))
                {
                    if (implication[1].contains("~") || value.contains("~"))
                    {
                        String[] and = implication[0].split(" & ");
                        for (String s : and)
                        {
                            list.add(negate(assembleFunction(parseFunction(s), unification)));
                        }
                    }
                } else
                {
                    list.add(value);
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append(list.remove(0));
            for (int i = 0; i < list.size(); i++)
            {
                sb.append(" | ");
                sb.append(list.get(i));
            }
            result.add(sb.toString());
            return result;
        }
        String[] function = parseFunction(b);
//        System.out.println(b);
//        System.out.println(-1);
        boolean negate = b.charAt(0) == '~';
        if (implication[0].contains(function[0]))
        {
//            System.out.println("func =>");
            String[] and = implication[0].split(" & ");
            ArrayList<String[]> list = new ArrayList<>();
            Map<String, String> unification = new HashMap<>();
            boolean ok = true;
            if (negate)
            {
                for (int i = 0; i < and.length; i++)
                {
                    String[] f = parseFunction(and[i]);
                    if (unify(f, function, unification))
                    {
                        ok = true;
                    }
                }
            }
            if (ok)
            {
                for (int i = 0; i < and.length; i++)
                {
                    String[] f = parseFunction(and[i]);
                    if (!unify(f, function, unification))
                    {
                        list.add(f);
                    }
                }
                if (list.size()==0)
                {
                    result.add(assembleFunction(parseFunction(implication[1]), unification));
                }else
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append(assembleFunction(list.remove(0), unification));
                    for (String[] sa: list)
                    {
                        sb.append(" & ");
                        sb.append(assembleFunction(sa, unification));
                    }
                    sb.append(" => ");
                    sb.append(assembleFunction(parseFunction(implication[1]), unification));
                    result.add(sb.toString());
                }
            }
        }
//        System.out.println(0);
//        System.out.println(implication[1].contains(function[0]));
//        System.out.println(negate);
        if (implication[1].contains(function[0])&&negate)
        {
//            System.out.println("=> func");
            Map<String, String> unification = new HashMap<>();
            String[] func1 = parseFunction(implication[1]);
            String[] func2 = parseFunction(b);

            if (unify(func1, func2, unification))
            {
                String[] and = implication[0].split(" & ");
                StringBuilder sb = new StringBuilder();
                sb.append("~");
                sb.append(assembleFunction(parseFunction(and[0]), unification));
                for (int i = 1; i < and.length; i++)
                {
                    sb.append(" | ");
                    sb.append("~");
                    sb.append(assembleFunction(parseFunction(and[i]), unification));
                }
                result.add(sb.toString());
            }
        }
//        System.out.println(1);
        if (result.size() == 0)
        {
            return null;
        }
        return result;
    }
    private static String negateOr(String[] or, String b)
    {
        String[] function = parseFunction(b);
        ArrayList<String[]> list = new ArrayList<>();
        ArrayList<String> nList = new ArrayList<>();
        Map<String, String> unification = new HashMap<>();

        for (int i = 0; i < or.length; i++)
        {
            String[] func = parseFunction(or[i]);
            if (unify(func, function, unification))
            {
                if (or[i].contains("~")^b.contains("~"))
                {

                }else
                {
                    list.add(func);
                    if (or[i].charAt(0)=='~')
                    {
                        nList.add("~");
                    }else
                    {
                        nList.add("");
                    }
                }
            }else
            {
                list.add(func);
                if (or[i].charAt(0)=='~')
                {
                    nList.add("~");
                }else
                {
                    nList.add("");
                }
            }
        }
        if (list.size()<or.length)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(nList.remove(0));
            sb.append(assembleFunction(list.remove(0), unification));
            for (int i = 0; i < list.size(); i++)
            {
                sb.append(" | ");
                sb.append(nList.get(i));
                sb.append(assembleFunction(list.get(i), unification));
            }

            return sb.toString();
        }
        return null;
    }
    private static String assembleFunction(String[] function, Map<String, String> unification)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(function[0]);
        sb.append('(');
        for (int i = 1; i < function.length; i++)
        {
            if (unification.containsKey(function[i]))
            {
                sb.append(unification.get(function[i]));
            }else
            {
                sb.append(function[i]);
            }
            if (i!=function.length-1)
            {
                sb.append(',');
            }
        }
        sb.append(')');
        return sb.toString();
    }
    private static boolean unify(String[] a, String[] b, Map<String, String> map)
    {
        if (a[0].equals(b[0]))
        {
            for (int i = 0; i < a.length; i++)
            {
                if (!a[i].equals(b[i]))
                {
                    if (isVariable(a[i]))
                    {
                        if (map.containsKey(a[i]))
                        {
                            if (!map.get(a[i]).equals(b[i]))
                            {
                                return false;
                            }
                        }else
                        {
                            map.put(a[i], b[i]);
                        }

                    }else if (isVariable(b[i]))
                    {
                        if (map.containsKey(b[i]))
                        {
                            if (!map.get(b[i]).equals(a[i]))
                            {
                                return false;
                            }
                        }else
                        {
                            map.put(b[i], a[i]);
                        }
                    }else
                    {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }
    private static boolean isVariable(String s)
    {
        if (s.length() != 1)
        {
            return false;
        }
        return s.charAt(0)>='a' && s.charAt(0)<='z';
    }
    private static String[] parseFunction(String f)
    {
        if (f.charAt(0)=='~')
        {
            f = f.substring(1);
        }
        String[] result = f.split("[(,)]");

        return result;
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
