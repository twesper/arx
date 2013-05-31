package org.deidentifier.arx.framework;

public class MemoryBenchmark {

    private static boolean equal;
    private static int     code;

    public static void main(String[] args) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {

        // Create random data
        final int repeats = 1000;
        final int rows = 100000;
        byte[] sizes = { 2, 3, 5, 9, 16, 4, 7, 22, 1, 4 };
        int[][] data = new int[rows][sizes.length];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < sizes.length; col++) {
                data[row][col] = random(sizes, col);
            }
        }

        // Check that everything works
        MemoryAlignedUnsafeIntArray2 int0 = new MemoryAlignedUnsafeIntArray2(sizes, rows);
        MemoryUnsafe3 long0 = new MemoryUnsafe3(sizes, rows);
        MemoryUnsafe unsafe0 = new MemoryUnsafe(sizes, rows);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < sizes.length; col++) {
                int0.set(row, col, data[row][col]);
                long0.set(row, col, data[row][col]);
                unsafe0.set(col, data[row][col]);
                if (int0.get(row, col) != long0.get(row, col)) { throw new RuntimeException("Mismatch!"); }
                if (int0.get(row, col) != unsafe0.get(col)) { throw new RuntimeException("Mismatch!"); }
            }
            unsafe0.base += unsafe0.rowSize;
        }

        System.out.println("Everything works. Starting benchmark!");

        // Int benchmark
        MemoryAlignedUnsafeIntArray2 int1 = new MemoryAlignedUnsafeIntArray2(sizes, rows);
        MemoryAlignedUnsafeIntArray2 int2 = new MemoryAlignedUnsafeIntArray2(sizes, rows);
        benchmark1(int1, int2, sizes, rows, data, repeats);

        // Long benchmark
        MemoryUnsafe4 long1 = new MemoryUnsafe4(sizes, rows);
        MemoryUnsafe4 long2 = new MemoryUnsafe4(sizes, rows);
        benchmark2(long1, long2, sizes, rows, data, repeats);

        // Unsafe benchmark
        MemoryUnsafe unsafe1 = new MemoryUnsafe(sizes, rows);
        MemoryUnsafe unsafe2 = new MemoryUnsafe(sizes, rows);
        benchmark3(unsafe1, unsafe2, sizes, rows, data, repeats);
    }

    private static int random(byte[] sizes, int col) {
        return (int) (Math.random() * (Math.pow(2, sizes[col]) - 1d));
    }

    private static void benchmark1(MemoryAlignedUnsafeIntArray2 m1, MemoryAlignedUnsafeIntArray2 m2, byte[] sizes, int rows, int[][] data, int repeats) {

        long start = System.currentTimeMillis();

        // Write
        for (int i = 0; i < repeats; i++) {
            for (int row = 0; row < 100000; row++) {
                for (int col = 0; col < sizes.length; col++) {
                    m1.set(row, col, data[row][col]);
                }
            }
        }

        long write = System.currentTimeMillis() - start;

        // Copy
        for (int i = 0; i < repeats; i++) {
            for (int row = 0; row < 100000; row++) {
                for (int col = 0; col < sizes.length; col++) {
                    m2.set(row, col, m1.get(row, col));
                }
            }
        }

        long copy = System.currentTimeMillis() - start - write;

        // Compare
        for (int i = 0; i < repeats; i++) {
            for (int row = 0; row < 100000; row++) {
                equal = m1.equals(m2, row);
            }
        }

        long compare = System.currentTimeMillis() - start - copy - write;

        // Hashcode
        for (int i = 0; i < repeats; i++) {
            for (int row = 0; row < 100000; row++) {
                code = m1.hashcode(row);
            }
        }

        long hashcode = System.currentTimeMillis() - start - copy - compare - write;

        System.out.println(m1.getClass().getSimpleName());
        System.out.println(" - Size     : " + m1.getByteSize());
        System.out.println(" - Write    : " + write);
        System.out.println(" - Copy     : " + copy);
        System.out.println(" - Compare  : " + compare);
        System.out.println(" - Hashcode : " + hashcode);
        System.out.println(" - Total    : " + (copy + compare + hashcode + write));
    }

    private static void benchmark2(MemoryUnsafe4 m1, MemoryUnsafe4 m2, byte[] sizes, int rows, int[][] data, int repeats) {

        long start = System.currentTimeMillis();

        // Write
        for (int i = 0; i < repeats; i++) {
            for (int row = 0; row < 100000; row++) {
                for (int col = 0; col < sizes.length; col++) {
                    m1.set(row, col, data[row][col]);
                }
            }
        }

        long write = System.currentTimeMillis() - start;

        // Copy
        for (int i = 0; i < repeats; i++) {
            for (int row = 0; row < 100000; row++) {
                for (int col = 0; col < sizes.length; col++) {
                    m2.set(row, col, m1.get(row, col));
                }
            }
        }

        long copy = System.currentTimeMillis() - start - write;

        // Compare
        for (int i = 0; i < repeats; i++) {
            for (int row = 0; row < 100000; row++) {
                equal = m1.equals(m2, row);
            }
        }

        long compare = System.currentTimeMillis() - start - copy - write;

        // Hashcode
        for (int i = 0; i < repeats; i++) {
            for (int row = 0; row < 100000; row++) {
                code = m1.hashcode(row);
            }
        }

        long hashcode = System.currentTimeMillis() - start - copy - compare - write;

        System.out.println(m1.getClass().getSimpleName());
        System.out.println(" - Size     : " + m1.getByteSize());
        System.out.println(" - Write    : " + write);
        System.out.println(" - Copy     : " + copy);
        System.out.println(" - Compare  : " + compare);
        System.out.println(" - Hashcode : " + hashcode);
        System.out.println(" - Total    : " + (copy + compare + hashcode + write));
    }

    private static void benchmark3(MemoryUnsafe m1, MemoryUnsafe m2, byte[] sizes, int rows, int[][] data, int repeats) {

        long start = System.currentTimeMillis();

        // Write
        for (int i = 0; i < repeats; i++) {
            m1.resetRow();
            for (int row = 0; row < 100000; row++) {
                for (int col = 0; col < sizes.length; col++) {
                    m1.set(col, data[row][col]);
                }
                m1.base += m1.rowSize;
            }
        }

        long write = System.currentTimeMillis() - start;

        // Copy
        for (int i = 0; i < repeats; i++) {
            m1.resetRow();
            m2.resetRow();
            for (int row = 0; row < 100000; row++) {
                for (int col = 0; col < sizes.length; col++) {
                    m2.set(col, m1.get(col));
                }
                m1.base += m1.rowSize;
                m2.base += m2.rowSize;
            }
        }

        long copy = System.currentTimeMillis() - start - write;

        // Compare
        for (int i = 0; i < repeats; i++) {
            m1.resetRow();
            m2.resetRow();
            for (int row = 0; row < 100000; row++) {
                equal = m1.equals(m2);
                m1.base += m1.rowSize;
                m2.base += m2.rowSize;
            }
        }

        long compare = System.currentTimeMillis() - start - copy - write;

        // Hashcode
        for (int i = 0; i < repeats; i++) {
            m1.resetRow();
            for (int row = 0; row < 100000; row++) {
                code = m1.hashcode();
                m1.base += m1.rowSize;
            }
        }

        long hashcode = System.currentTimeMillis() - start - copy - compare - write;

        System.out.println(m1.getClass().getSimpleName());
        System.out.println(" - Size     : " + m1.getByteSize());
        System.out.println(" - Write    : " + write);
        System.out.println(" - Copy     : " + copy);
        System.out.println(" - Compare  : " + compare);
        System.out.println(" - Hashcode : " + hashcode);
        System.out.println(" - Total    : " + (copy + compare + hashcode + write));
    }
}
