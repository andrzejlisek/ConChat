/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.conchat;

/**
 *
 * @author xxx
 */
public class StringUTFTest
{
    void xAssert(StringUTF obj, String val) throws Exception
    {
        if (!obj.get().equals(val)) throw new Exception("xAssert failed: {" + obj.get() + "}=={" + val + "}");
    }

    void xAssert(boolean obj, boolean val) throws Exception
    {
        if (obj != val) throw new Exception("xAssert failed: {" + obj + "}=={" + val + "}");
    }

    void xAssert(int obj, int val) throws Exception
    {
        if (obj != val) throw new Exception("xAssert failed: {" + obj + "}=={" + val + "}");
    }

    
    public void test()
    {
        StringUTF temp1 = new StringUTF("abcd");
        StringUTF temp2 = new StringUTF();
        
        try
        {
            xAssert(temp1, "abcd");
            
            temp2.set("A\uD83D\uDD2BB");

            xAssert(temp2.length(), 3);
            xAssert(temp2.charAt(0), 0x41);
            xAssert(temp2.charAt(1), 0x1f52b);
            xAssert(temp2.charAt(2), 0x42);
            
            xAssert(temp2, "A\uD83D\uDD2BB");
            xAssert(temp2.clone(), "A\uD83D\uDD2BB");
            
            temp1.append("zxcvb");

            xAssert(temp1, "abcdzxcvb");
            xAssert(temp1.clone(), "abcdzxcvb");

            xAssert(temp1.length(), 9);
            xAssert(temp1.clone().length(), 9);

            xAssert(temp1.charAt(0), 'a');
            xAssert(temp1.clone().charAt(0), 'a');

            xAssert(temp1.charAt(1), 'b');
            xAssert(temp1.clone().charAt(1), 'b');

            xAssert(temp1.charAt(7), 'v');
            xAssert(temp1.clone().charAt(7), 'v');

            xAssert(temp1.charAt(8), 'b');
            xAssert(temp1.clone().charAt(8), 'b');
            
            xAssert(temp1.clone().remove(0, 2), "cdzxcvb");
            xAssert(temp1.clone().remove(1, 2), "adzxcvb");
            xAssert(temp1.clone().remove(2), "ab");
            xAssert(temp1.clone().remove(6, 2), "abcdzxb");
            xAssert(temp1.clone().remove(7, 2), "abcdzxc");
            
            
            xAssert(temp1.clone().substring(0), "abcdzxcvb");
            xAssert(temp1.clone().substring(2), "cdzxcvb");
            xAssert(temp1.clone().substring(7), "vb");
            xAssert(temp1.clone().substring(8), "b");

            xAssert(temp1.clone().substring(0, 9), "abcdzxcvb");
            xAssert(temp1.clone().substring(0, 8), "abcdzxcv");
            xAssert(temp1.clone().substring(2, 7), "cdzxcvb");
            xAssert(temp1.clone().substring(2, 6), "cdzxcv");

            temp1.prepend("qwerty");

            xAssert(temp1, "qwertyabcdzxcvb");
            
            temp2.append(new StringUTF("135")).append(65);
            temp2.prepend(new StringUTF("246")).prepend(66);

            xAssert(temp2, "B246A\uD83D\uDD2BB135A");

            temp2.insert(0, new StringUTF("qwe"));
            xAssert(temp2, "qweB246A\uD83D\uDD2BB135A");

            temp2.insert(3, new StringUTF("rty"));
            xAssert(temp2, "qwertyB246A\uD83D\uDD2BB135A");

            temp2.insert(17, new StringUTF("cvb"));
            xAssert(temp2, "qwertyB246A\uD83D\uDD2BB135Acvb");

            temp2.insert(17, new StringUTF("zxc"));
            xAssert(temp2, "qwertyB246A\uD83D\uDD2BB135Azxccvb");

            xAssert(temp2.length(), 23);

            
            temp1.set("    qwerty    ");
            xAssert(temp1.clone().trim(), "qwerty");
            
            temp1.set("        ");
            xAssert(temp1.clone().trim(), "");

            temp1.set("q   w");
            xAssert(temp1.clone().trim(), "q   w");

            temp1.set("  q   w");
            xAssert(temp1.clone().trim(), "q   w");

            temp1.set("q   w  ");
            xAssert(temp1.clone().trim(), "q   w");

            temp1.set("");
            xAssert(temp1.clone().trim(), "");
            
            
            temp2.set("abcd");

            xAssert(temp2.startsWith(""), true);
            xAssert(temp2.startsWith("a"), true);
            xAssert(temp2.startsWith("ab"), true);
            xAssert(temp2.startsWith("abcd"), true);
            xAssert(temp2.startsWith("abcde"), false);
            xAssert(temp2.startsWith("abce"), false);
            xAssert(temp2.startsWith("q"), false);

            xAssert(temp2.startsWith('a'), true);
            xAssert(temp2.startsWith('q'), false);

            xAssert(temp2.endsWith(""), true);
            xAssert(temp2.endsWith("d"), true);
            xAssert(temp2.endsWith("cd"), true);
            xAssert(temp2.endsWith("abcd"), true);
            xAssert(temp2.endsWith("zabcd"), false);
            xAssert(temp2.endsWith("zbcd"), false);
            xAssert(temp2.endsWith("z"), false);

            xAssert(temp2.endsWith('d'), true);
            xAssert(temp2.endsWith('z'), false);

            xAssert(temp2.contains(""), true);
            xAssert(temp2.contains("a"), true);
            xAssert(temp2.contains("d"), true);
            xAssert(temp2.contains("ab"), true);
            xAssert(temp2.contains("bc"), true);
            xAssert(temp2.contains("cd"), true);
            xAssert(temp2.contains("abcd"), true);
            xAssert(temp2.contains("abcde"), false);
            xAssert(temp2.contains("zabcd"), false);
            xAssert(temp2.contains("abce"), false);
            xAssert(temp2.contains("zbcd"), false);

            xAssert(temp2.contains("q"), false);
            xAssert(temp2.contains("z"), false);

            xAssert(temp2.contains('a'), true);
            xAssert(temp2.contains('b'), true);
            xAssert(temp2.contains('c'), true);
            xAssert(temp2.contains('d'), true);
            xAssert(temp2.contains('q'), false);
            xAssert(temp2.contains('z'), false);

            xAssert(temp1.startsWith(""), true);
            xAssert(temp1.startsWith("a"), false);
            xAssert(temp1.endsWith(""), true);
            xAssert(temp1.endsWith("a"), false);
            xAssert(temp1.contains(""), true);
            xAssert(temp1.contains("a"), false);


            xAssert(temp1.length(), 0);
            xAssert(temp2.length(), 4);


            xAssert(temp2, "abcd");
            temp2.setChar(0, 'Q');
            xAssert(temp2, "Qbcd");
            temp2.setChar(3, 'Z');
            xAssert(temp2, "QbcZ");

            temp2.prepend('x', 3).append('y', 3);
            xAssert(temp2, "xxxQbcZyyy");
            
            
            xAssert(temp2.indexOf('x', 0), 0);
            xAssert(temp2.indexOf("x", 0), 0);
            xAssert(temp2.indexOf('x', 1), 1);
            xAssert(temp2.indexOf("x", 1), 1);
            xAssert(temp2.indexOf('x', 3), -1);
            xAssert(temp2.indexOf("x", 3), -1);
            xAssert(temp2.indexOf('x', 1 - 10), 2);
            xAssert(temp2.indexOf("x", 1 - 10), 2);
            xAssert(temp2.indexOf('y', 1 - 10), 9);
            xAssert(temp2.indexOf("y", 1 - 10), 9);
            xAssert(temp2.indexOf("xx", 0), 0);
            xAssert(temp2.indexOf("xQ", 0), 2);
            xAssert(temp2.indexOf("yy", 0), 7);
            xAssert(temp2.indexOf('z', 0), -1);
            xAssert(temp2.indexOf("z", 0), -1);
            xAssert(temp2.indexOf('z', 1 - 10), -1);
            xAssert(temp2.indexOf("z", 1 - 10), -1);
            xAssert(temp2.indexOf("zz", 0), -1);
            xAssert(temp2.indexOf("zz", 1 - 10), -1);
            
            temp1.set("");
            xAssert(temp1, "");
            xAssert(temp1.length(), 0);

            temp1.append("\n");
            xAssert(temp1, "\n");
            xAssert(temp1.length(), 1);

            temp1.remove(temp1.length() - 1, 1);
            xAssert(temp1, "");
            xAssert(temp1.length(), 0);

            temp1.set("A\uD83D\uDD2BB");
            xAssert(temp1, "A\uD83D\uDD2BB");
            xAssert(temp1.length(), 3);

            temp1.append("\n");
            xAssert(temp1, "A\uD83D\uDD2BB\n");
            xAssert(temp1.length(), 4);

            temp1.remove(temp1.length() - 1, 1);
            xAssert(temp1, "A\uD83D\uDD2BB");
            xAssert(temp1.length(), 3);
        }
        catch (Exception ex)
        {
            System.out.println("test failed");
            ex.printStackTrace(System.out);
        }
        System.out.println("test passed");
        System.exit(0);
    }
}
