package me.unc.algorithm.hash;

/**
 * @Description String哈希算法
 * @Date 2020/4/29 11:14
 * @author LZS
 * @version v1.0
 */
public class Hash {

    /**
     * BKDRHash是 Kernighan 和 Dennis 在《The C programming language》中提出的。
     * 这个算法的常数131是如何选取的，我尚不知到
     * @param str 字符串
     * @return 哈希值
     */
    public static int BKDRHash(String str) {
        final int seed = 131;
        int hash = 0;
        for (int i = 0; i < str.length(); i++) {
            hash = hash * seed + (int)str.charAt(i);
        }
        return hash & 0x7FFFFFFF;
    }

    /**
     * Arash Partow 提出了这个算法，声称具有很好地分布性。
     * @param str 字符串
     * @return 哈希值
     */
    public static int APHash(String str) {
        int hash = 0;
        for (int i = 0; i < str.length(); i++) {
            if ((i & 1) == 0) {
                hash ^= (hash << 7) ^ (str.charAt(i)) ^ (hash >> 3);
            } else {
                hash ^= ~((hash << 11) ^ (str.charAt(i)) ^ (hash >> 5));
            }
        }
        return hash & 0x7FFFFFFF;
    }

    /**
     * Justin Sobel 提出的基于位的函数函数。
     * @param str 字符串
     * @return 哈希值
     */
    public static int JSHash(String str) {
        int hash = 0;
        for (int i = 0; i < str.length(); i++) {
            hash ^= (hash << 5) + (int)str.charAt(i) + (hash >> 2);
        }
        return hash & 0x7FFFFFFF;
    }

    /**
     * 作者是Robert Sedgwicks
     * @param str 字符串
     * @return 哈希值
     */
    public static int RSHash(String str) {
        int hash = 0;
        int a = 63689;
        final int b = 378551;
        for (int i = 0; i < str.length(); i++) {
            hash = hash * a + (int)str.charAt(i);
            a *= b;
        }
        return hash & 0x7FFFFFFF;
    }

    /**
     * SDBM项目使用的哈希函数，声称对所有的数据集有很好地分布性。
     * @param str 字符串
     * @return 哈希值
     */
    public static int SDBMHash(String str) {
        int hash = 0;
        for (int i = 0; i < str.length(); i++) {
            hash = (int)str.charAt(i) + (hash << 6) + (hash << 16) - hash;
        }
        return hash & 0x7FFFFFFF;
    }

    /**
     * Peter J. Weinberger在其编译器著作中提出的。
     * @param str 字符串
     * @return 哈希值
     */
    public static int PJWHash(String str) {
        int BitsInUnsignedInt = 32;
        int ThreeQuarters    = 24;
        int OneEighth        = 4;
        int HighBits         = (int)(0xFFFFFFFF) << (BitsInUnsignedInt - OneEighth);
        int hash             = 0;
        int test             = 0;
        for (int i = 0; i < str.length(); i++) {
            hash = (hash << OneEighth) + (int)str.charAt(i);
            if ((test = hash & HighBits) != 0)
            {
                hash = ((hash ^ (test >> ThreeQuarters)) & (~HighBits));
            }
        }
        return hash & 0x7FFFFFFF;
    }

    /**
     * Unix系统上面广泛使用的哈希函数。
     * @param str 字符串
     * @return 哈希值
     */
    public static int ELFHash(String str) {
        int hash = 0;
        int x = 0;
        for (int i = 0; i < str.length(); i++) {
            hash = (hash << 4) + (int)str.charAt(i);
            if ((x & hash & 0xF0000000L) != 0) {
                hash ^= x >> 24;
                hash &= ~x;
            }
        }
        return hash & 0x7FFFFFFF;
    }

    /**
     * Daniel J. Bernstein在comp.lang.c邮件列表中发表的，是距今为止比较高效的哈希函数之一。
     * @param str 字符串
     * @return 哈希值
     */
    public static int DJBHash(String str) {
        int hash = 5381;

        for (int i = 0; i < str.length(); i++) {
            hash += (hash << 5) + (int)str.charAt(i);
        }
        return hash & 0x7FFFFFFF;
    }

    /**
     * Donald E. Knuth在《计算机程序设计的艺术》中提出的哈希函数。
     * @param str 字符串
     * @return 哈希值
     */
    public static int DEKHash(String str) {
        int hash = str.length();
        for (int i = 0; i < str.length(); i++) {
            hash = (hash << 5) ^ (hash >> 27) ^ (int)str.charAt(i);
        }
        return hash & 0x7FFFFFFF;
    }

    /**
     * 无介绍
     * @param str 字符串
     * @return 哈希值
     */
    public static int BPHash(String str) {
        int hash = str.length();
        for (int i = 0; i < str.length(); i++) {
            hash = (hash << 7) ^ (int)str.charAt(i);
        }
        return hash & 0x7FFFFFFF;
    }

    /**
     * 无介绍
     * @param str 字符串
     * @return 哈希值
     */
    public static int FNVHash(String str) {
        int fnvprime = 0x811C9DC5;
        int hash = 0;
        for (int i = 0; i < str.length(); i++) {
            hash *= fnvprime;
            hash ^= (int) str.charAt(i);
        }
        return hash & 0x7FFFFFFF;
    }

    /**
     * Java的字符串类的Hash算法，简单实用高效。（JDK6源码）
     * @param str 字符串
     * @return 哈希值
     */
    public static int JDKHash(String str) {
        int hash = 0;
        for (int i = 0; i < str.length(); i++) {
            hash = hash * 31 + (int)str.charAt(i);
        }
        return hash & 0x7FFFFFFF;
    }

    /**
     * 使用网上提供的一份英语单词文件：http://www.cs.duke.edu/~ola/ap/linuxwords,共45402个单词,
     * 分别比较上面每一个算法在哈希表长度为 100,1000 和 10000 时的最大冲突数，理论上平均为 455, 46 和 5。结果如下：
     *
     * 算法       长度100的哈希          长度1000的哈希              长度10000的哈希
     * bkdrhash      509                     72                         14
     * aphash        519                     72                         15
     * jshash        494                     66                         15
     * rshash        505                     74                         15
     * sdbmhash      518                     67                         15
     * pjwhash       756                    131                         34
     * elfhash       801                    158                         91
     * djbhash       512                     64                         17
     * dekhash       536                     75                         22
     * bphash       1391                    696                        690
     * fnvhash       516                     65                         14
     * javahash      523                     69                         16
     *
     * 结论
     * 从上面的统计数据可以看出对英文单词集而言，jshash,djbhash和fnvhash都有很好地分散性。
     */
    public void contrast() {}

}
