import java.io.*;
import java.util.*;

public class LogicAgent
{
    private static ArrayList<String> KB;
    private static String[] Queries;
    private static int MaxKBSize;
//    private static Map<String, String[]> backtrackMap = new HashMap<>();
    public static void main(String[] args) throws IOException
    {
//        System.out.println(Arrays.toString(parseFunction("~Alert(Bob,NSAIDs)")))
//        String[] a = parseFunction("Alert(Bob,NSAIDs)");
//        String[] b = parseFunction("Alert(x,NSAIDs)");
//        System.out.println(entailNewKnowledge("~Missile4(x) & ~Owns4(Nono,x) => Sells4(West,x,Nono)",
//                "~American4(West) | ~Weapon4(y) | ~Sells4(West,y,z) | ~Hostile4(z)"));



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
        MaxKBSize = count*10;
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
        while (queryQueue.size()!=0&&base.size()<MaxKBSize)
        {
            if (!checkAgainstKB(queryQueue, base))
            {
//                System.out.println("TRUE");
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
//        System.out.println("FALSE");
        return "FALSE";
    }
    private static boolean checkAgainstKB(Queue<String> queue, ArrayList<String> base)
    {
        for (String k: base)
        {
            //check if a contradiction arise, if found return false
            if (isAtomic(k)&&isAtomic(queue.peek()))
            {

                if ((k.charAt(0)=='~'^queue.peek().charAt(0)=='~')&& unify(parseFunction(k), parseFunction(queue.peek()), new HashMap<>()))
                {
//                    System.out.println(k+"   "+queue.peek());
//                    Queue<String> printQ = new LinkedList<>();
//                    printQ.offer(queue.peek());
//                    while (!printQ.isEmpty())
//                    {
//                        if (backtrackMap.containsKey(printQ.peek()))
//                        {
//                            System.out.println(printQ.peek()+"::"+Arrays.toString(backtrackMap.get(printQ.peek())));
//                            for (int i = 0; i < 2; i++)
//                            {
//                                printQ.offer(backtrackMap.get(printQ.peek())[i]);
//                            }
//                        }
//                        printQ.poll();
//                    }
                    return false;
                }
            }

            //if we can derive new sentences, add to the queue
//            System.out.println(queue);
            ArrayList<String> newK = entailNewKnowledge(k, queue.peek());
//            System.out.println(newK);
            if (newK != null)
            {
                for (String s: newK)
                {
                    if (s!=null&&!base.contains(s)&&!queue.contains(s))
                    {
//                        backtrackMap.put(s, new String[] {k, queue.peek()});
//                        System.out.println(k+" ++ "+queue.peek()+" >> "+s);
                        queue.add(s);
                    }
                }

            }
        }
        return true;
    }
    private static ArrayList<String> entailNewKnowledge(String k, String q)
    {
//        System.out.println(k);
//        System.out.println(q);
        ArrayList<String> result = new ArrayList<>();
//        System.out.println("=>");
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
//        System.out.println("||");
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
            if (q.contains("~"))
            {
                result.add(negate(assembleFunction(funcK, unification)));
            }else
            {
                result.add(assembleFunction(funcK, unification));
            }
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
            Map<String, String> unification = new HashMap<>();
            for (String value : or)
            {
                if (unify(parseFunction(implication[1]), parseFunction(value), unification))
                {
                    if (implication[1].contains("~") ^ value.contains("~"))
                    {
                        String[] and = implication[0].split(" & ");
                        for (String s : and)
                        {
                            if (s.contains("~"))
                            {
                                list.add(assembleFunction(parseFunction(s), unification));
                            }else
                            {
                                list.add(negate(assembleFunction(parseFunction(s), unification)));
                            }

                        }
                    }
                } else
                {
                    list.add(value);
                }
            }
//            System.out.println(unification);
            for (int i = 0; i < list.size(); i++)
            {
//                System.out.println(list.get(i));
                if (list.get(i).contains("~"))
                {

                    list.set(i, negate(assembleFunction(parseFunction(list.get(i)), unification)));
                }else
                {
                    list.set(i, assembleFunction(parseFunction(list.get(i)), unification));
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
        boolean negate = false;
        if (b.charAt(0) == '~' ^ implication[1].charAt(0) == '~')
        {
//            System.out.println(implication[1]);
//            System.out.println(b);
//            System.out.println(implication[1].charAt(0));
//            System.out.println(b.charAt(0));
            negate = true;
        }

        if (implication[0].contains(function[0]))
        {
//            System.out.println("func =>");
            String[] and = implication[0].split(" & ");
            ArrayList<String[]> list = new ArrayList<>();
            Map<String, String> unification = new HashMap<>();
            boolean ok = false;
            for (int i = 0; i < and.length; i++)
            {
                String[] f = parseFunction(and[i]);
                if (unify(f, function, unification))
                {
                    if (!(b.charAt(0) == '~' ^ and[i].charAt(0) == '~'))
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
                    if (implication[1].contains("~"))
                    {
                        result.add(negate(assembleFunction(parseFunction(implication[1]), unification)));
                    }else
                    {
                        result.add(assembleFunction(parseFunction(implication[1]), unification));
                    }
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
                if (!and[0].contains("~"))
                {
                    sb.append("~");
                }
                sb.append(assembleFunction(parseFunction(and[0]), unification));
                for (int i = 1; i < and.length; i++)
                {
                    sb.append(" | ");
                    if (!and[i].contains("~"))
                    {
                        sb.append("~");
                    }
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
//        System.out.println(Arrays.toString(or));
//        System.out.println(b);
        ArrayList<String[]> list = new ArrayList<>();
        ArrayList<String> nList = new ArrayList<>();
        Map<String, String> unification = new HashMap<>();

        for (int i = 0; i < or.length; i++)
        {
            String[] func = parseFunction(or[i]);
            if (i == or.length-1 && list.size() == 0)
            {
                list.add(func);
                if (or[i].charAt(0)=='~')
                {
                    nList.add("~");
                }else
                {
                    nList.add("");
                }
                break;
            }
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
                        if (unifyVariable(b, a, map, i)) return false;

                    }else if (isVariable(b[i]))
                    {
                        if (unifyVariable(a, b, map, i)) return false;
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

    private static boolean unifyVariable(String[] a, String[] b, Map<String, String> map, int i)
    {
        if (map.containsKey(b[i]))
        {
            if (!map.get(b[i]).equals(a[i]))
            {
                return true;
            }
        }else
        {
            for (String s : map.keySet())
            {
                if (map.get(s).equals(b[i]))
                {
                    map.put(s, a[i]);
                }
            }
            map.put(b[i], a[i]);
        }
        return false;
    }

    private static boolean isAtomic(String s)
    {
        return !(s.contains("=>")||s.contains("|")||s.contains("&"));
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
