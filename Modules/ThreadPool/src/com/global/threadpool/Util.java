package com.global.threadpool;

/**
 * @author Róbert Dóczi
 *         Date: 2014.12.02.
 */
class Util {

    public static boolean compareFlags(BitField f1, BitField f2) {
        return (f1.getValue() & f2.getValue()) == f2.getValue();
    }

}
