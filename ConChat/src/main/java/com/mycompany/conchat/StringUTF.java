/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.conchat;

import java.util.ArrayList;

/**
 *
 * @author xxx
 */
public final class StringUTF implements Cloneable
{
    private final ArrayList<Integer> chrs;
    
    public StringUTF()
    {
        chrs = new ArrayList<>();
    }

    public StringUTF(String S)
    {
        chrs = new ArrayList<>();
        set(S);
    }

    public StringUTF(StringUTF obj)
    {
        chrs = new ArrayList<>();
        for (int i = 0; i < obj.chrs.size(); i++)
        {
            int t = obj.chrs.get(i);
            chrs.add(t);
        }
    }

    
    /**
     * Get the text string
     * @return 
     */
    public String get()
    {
        StringBuilder sb = new StringBuilder();
        int l = length();
        for (int i = 0; i < l; i++)
        {
            int chr = chrs.get(i);
            if (chr < 65536)
            {
                sb.append(((char)chr));
            }
            else
            {
                sb.append((char)(((chr - 0x10000) >> 10) + 0xD800));
                sb.append((char)(((chr - 0x10000) & 0x3FF) + 0xDC00));
            }
        }
        return sb.toString();
    }

    /**
     * Set the text string
     * @param str 
     */
    public void set(String str)
    {
        clear();
        append(str);
    }

    /**
     * Cleat the object
     * @return 
     */
    public StringUTF clear()
    {
        chrs.clear();
        return this;
    }
    
    /**
     * Append additional text
     * @param str
     * @return 
     */
    public StringUTF append(String str)
    {
        int l = str.length();
        for (int i = 0; i < l; i++)
        {
            int chr = str.charAt(i);
            if ((chr >= 0xD800) && (chr < 0xDBFF))
            {
                if ((i < (l - 1)))
                {
                    int chrX = str.charAt(i + 1);
                    chr = 0x10000 + ((chr - 0xD800) << 10) + (chrX - 0xDC00);
                    i++;
                }
                else
                {
                    chr = '?';
                }
            }

            if ((chr >= 0xD800) && (chr < 0xDFFF))
            {
                chr = '?';
            }
            
            chrs.add(chr);
        }
        return this;
    }

    /**
     * Append additional character
     * @param chr
     * @return 
     */
    public StringUTF append(int chr)
    {
        chrs.add(chr);
        return this;
    }

    /**
     * Append additional character sequence
     * @param chr
     * @param n
     * @return 
     */
    public StringUTF append(int chr, int n)
    {
        while (n > 0)
        {
            chrs.add(chr);
            n--;
        }
        return this;
    }
    
    /**
     * Append other StringUTF object
     * @param obj
     * @return 
     */
    public StringUTF append(StringUTF obj)
    {
        chrs.addAll(obj.chrs);
        return this;
    }
    
    /**
     * Prepend additional text
     * @param str
     * @return 
     */
    public StringUTF prepend(String str)
    {
        int idx = 0;
        int l = str.length();
        for (int i = 0; i < l; i++)
        {
            int chr = str.charAt(i);
            if ((chr >= 0xD800) && (chr < 0xDBFF))
            {
                if ((i < (l - 1)))
                {
                    int chrX = str.charAt(i + 1);
                    chr = 0x10000 + ((chr - 0xD800) << 10) + (chrX - 0xDC00);
                    i++;
                }
                else
                {
                    chr = '?';
                }
            }

            if ((chr >= 0xD800) && (chr < 0xDFFF))
            {
                chr = '?';
            }
            
            chrs.add(idx, chr);
            idx++;
        }
        return this;
    }

    /**
     * Prepend additional character
     * @param chr
     * @return 
     */
    public StringUTF prepend(int chr)
    {
        chrs.add(0, chr);
        return this;
    }

    /**
     * Prepend additional character sequence
     * @param chr
     * @param n
     * @return 
     */
    public StringUTF prepend(int chr, int n)
    {
        while (n > 0)
        {
            chrs.add(0, chr);
            n--;
        }
        return this;
    }

    /**
     * Prepend other StringUTF object
     * @param obj
     * @return 
     */
    public StringUTF prepend(StringUTF obj)
    {
        chrs.addAll(0, obj.chrs);
        return this;
    }
    
    /**
     * Change single character into other character
     * @param idx
     * @param chr
     * @return 
     */
    public StringUTF setChar(int idx, int chr)
    {
        chrs.set(idx, chr);
        return this;
    }
    
    /**
     * Insert additional text
     * @param idx
     * @param obj
     * @return 
     */
    public StringUTF insert(int idx, StringUTF obj)
    {
        chrs.addAll(idx, obj.chrs);
        return this;
    }

    /**
     * Insert additional character
     * @param idx
     * @param chr
     * @return 
     */
    public StringUTF insert(int idx, int chr)
    {
        chrs.add(idx, chr);
        return this;
    }
    
    /**
     * Remove specified fragment
     * @param idx
     * @param n
     * @return 
     */
    public StringUTF remove(int idx, int n)
    {
        chrs.subList(idx, idx + n).clear();
        return this;
    }

    /**
     * Remove specified fragment
     * @param idx
     * @return 
     */
    public StringUTF remove(int idx)
    {
        chrs.subList(idx, chrs.size()).clear();
        return this;
    }



    /**
     * Extract the substring
     * @param idx
     * @param n
     * @return 
     */
    public StringUTF substring(int idx, int n)
    {
        if (idx > 0)
        {
            chrs.subList(0, idx).clear();
        }
        if (n < chrs.size())
        {
            chrs.subList(n, chrs.size()).clear();
        }
        return this;
    }

    /**
     * Extract the substring
     * @param idx
     * @return 
     */
    public StringUTF substring(int idx)
    {
        if (idx > 0)
        {
            chrs.subList(0, idx).clear();
        }
        return this;
    }

    /**
     * Remove leading and trailing whitespaces
     * @return 
     */
    public StringUTF trim()
    {
        for (int i = 0; i < length(); i++)
        {
            if (!isWhiteChar(chrs.get(i)))
            {
                int t1 = 0;
                int t2 = chrs.size() - 1;
                while (isWhiteChar(chrs.get(t1))) { t1++; }
                while (isWhiteChar(chrs.get(t2))) { t2--; }
                remove(t2 + 1);
                remove(0, t1);
                return this;
            }
        }
        chrs.clear();
        return this;
    }
    


    /**
     * Create copy of the object
     * @return 
     */
    @Override
    public StringUTF clone()
    {
        return new StringUTF(this);
    }
    

    
    private boolean matchStr(int idx, String str)
    {
        StringUTF temp = new StringUTF(str);
        int tempL = temp.chrs.size();
        for (int i = 0; i < tempL; i++)
        {
            if (!chrs.get(idx + i).equals(temp.chrs.get(i)))
            {
                return false;
            }
        }
        return true;
    }



    
    /**
     * Compare with string
     * @param str
     * @return 
     */
    public boolean equalsStr(String str)
    {
        return (str.length() == chrs.size()) && matchStr(0, str);
    }
    
    public boolean startsWith(int chr)
    {
        return ((chrs.size() > 0) && (chrs.get(0) == chr));
    }

    public boolean startsWith(String str)
    {
        return (str.length() <= chrs.size()) && matchStr(0, str);
    }

    public boolean endsWith(int chr)
    {
        return ((chrs.size() > 0) && (chrs.get(chrs.size() - 1) == chr));
    }

    public boolean endsWith(String str)
    {
        return (str.length() <= chrs.size()) && matchStr(chrs.size() - str.length(), str);
    }

    public boolean contains(String str)
    {
        return (indexOf(str, 0) >= 0);
    }

    public boolean contains(int chr)
    {
        return (indexOf(chr, 0) >= 0);
    }
    
    public int indexOf(String str, int startPos)
    {
        if (startPos >= 0)
        {
            for (int i = startPos; i <= (chrs.size() - str.length()); i++)
            {
                if (matchStr(i, str)) return i;
            }
        }
        else
        {
            for (int i = (0 - startPos); i > 0; i--)
            {
                if (matchStr(i, str)) return i;
            }
        }
        return -1;
    }
    
    public int indexOf(int chr, int startPos)
    {
        if (startPos >= 0)
        {
            for (int i = startPos; i < chrs.size(); i++)
            {
                int c = chrs.get(i);
                if (c == chr) return i;
            }
        }
        else
        {
            for (int i = (0 - startPos); i > 0; i--)
            {
                int c = chrs.get(i);
                if (c == chr) return i;
            }
        }
        return -1;
    }
    
    public int length()
    {
        return chrs.size();
    }
    
    public int charAt(int idx)
    {
        return chrs.get(idx);
    }


    
    public boolean isSpacesOnly()
    {
        for (int i = 0; i < chrs.size(); i++)
        {
            if (!isWhiteChar(chrs.get(i)))
            {
                return false;
            }
        }
        return true;
    }
    
    public boolean isDigitsOnly()
    {
        for (int i = 1; i < chrs.size(); i++)
        {
            if (!CommonTools.isChar(chrs.get(i), false, false, true, false))
            {
                return false;
            }
        }
        return true;
    }

    
    /**
     * Force explicitly conversion into standard string object
     * @return 
     */
    @Override
    public String toString()
    {
        throw new UnsupportedOperationException();
    }
    
    
    
    private static boolean isWhiteChar(int c)
    {
        switch (c)
        {
            case 0x20:
            case 0x0D:
            case 0x0A:
            case 0x09:
                return true;
        }
        return false;
    }
    
    public static StringUTF indent(int n, char chr)
    {
        StringUTF temp = new StringUTF();
        int chr_ = chr;
        for (int i = 0; i < n; i++)
        {
            temp.chrs.add(chr_);
        }
        return temp;
    }
}
