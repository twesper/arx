package org.deidentifier.arx.framework;

public class MemoryBenchmark {

    private static boolean equal;
    private static int code;
    
    public static void main(String[] args) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        
        // Create random data
        final int repeats = 1000;
        final int rows = 100000;
        byte[] sizes = {2, 3, 5, 9, 16, 4, 7, 22, 1, 4};
        int[][] data = new int[rows][sizes.length];
        for (int row=0; row<rows; row++){
            for (int col=0; col<sizes.length; col++){
                data[row][col] = random(sizes, col);
            }
        }
        
        // Check that everything works
        MemoryAlignedIntArray int0 = new MemoryAlignedIntArray(sizes, rows);
        MemoryUnalignedLongArray long0 = new MemoryUnalignedLongArray(sizes, rows);
        MemoryUnsafe unsafe0 = new MemoryUnsafe(sizes, rows);
        for (int row=0; row<rows; row++){
            for (int col=0; col<sizes.length; col++){
                int0.set(row, col, data[row][col]);
                long0.set(row, col, data[row][col]);
                unsafe0.set(row, col, data[row][col]);
                if (int0.get(row,col)!=long0.get(row, col)) {
                    throw new RuntimeException("Mismatch!");
                }
                if (int0.get(row,col)!=unsafe0.get(row, col)) {
                    throw new RuntimeException("Mismatch!");
                }
            }
        }
        
        System.out.println("Everything works. Starting benchmark!");
        
        // Int benchmark
        MemoryAlignedIntArray int1 = new MemoryAlignedIntArray(sizes, rows);
        MemoryAlignedIntArray int2 = new MemoryAlignedIntArray(sizes, rows);
        benchmark1(int1, int2, sizes, rows, data, repeats);
        
        // Long benchmark
        MemoryUnalignedLongArray long1 = new MemoryUnalignedLongArray(sizes, rows);
        MemoryUnalignedLongArray long2 = new MemoryUnalignedLongArray(sizes, rows);
        benchmark2(long1, long2, sizes, rows, data, repeats);
        

        // Unsafe benchmark
        MemoryUnsafe unsafe1 = new MemoryUnsafe(sizes, rows);
        MemoryUnsafe unsafe2 = new MemoryUnsafe(sizes, rows);
        benchmark3(unsafe1, unsafe2, sizes, rows, data, repeats);
    }

    private static int random(byte[] sizes, int col) {
        return (int)(Math.random() * (Math.pow(2, sizes[col])-1d));
    }
    
    private static void benchmark1(MemoryAlignedIntArray m1, MemoryAlignedIntArray m2, byte[] sizes, int rows, int[][] data, int repeats){
        
        long start = System.currentTimeMillis();
        
        // Write
        for (int i=0; i<repeats; i++){
            for (int row=0; row<100000; row++){
                for (int col=0; col<sizes.length; col++){
                    m1.set(row, col, data[row][col]);
                }
            }
        }
        
        long write = System.currentTimeMillis() - start;
        
        // Copy
        for (int i=0; i<repeats; i++){
            for (int row=0; row<100000; row++){
                for (int col=0; col<sizes.length; col++){
                    m2.set(row, col, m1.get(row, col));
                }
            }
        }
        
        long copy = System.currentTimeMillis() - start - write;
        
        // Compare
        for (int i=0; i<repeats; i++){
            for (int row=0; row<100000; row++){
                equal = m1.equals(m2, row);
            }
        }
        
        long compare = System.currentTimeMillis() - start - copy - write;

        // Hashcode
        for (int i=0; i<repeats; i++){
            for (int row=0; row<100000; row++){
                code = m1.hashcode(row);
            }
        }
        
        long hashcode = System.currentTimeMillis() - start - copy - compare - write;
        
        System.out.println(m1.getClass().getSimpleName());
        System.out.println(" - Size     : "+m1.getByteSize());
        System.out.println(" - Write    : "+write);
        System.out.println(" - Copy     : "+copy);
        System.out.println(" - Compare  : "+compare);
        System.out.println(" - Hashcode : "+hashcode);
        System.out.println(" - Total    : "+(copy+compare+hashcode+write));
    }
    

    private static void benchmark2(MemoryUnalignedLongArray m1, MemoryUnalignedLongArray m2, byte[] sizes, int rows, int[][] data, int repeats){
        
        long start = System.currentTimeMillis();
        
        // Write
        for (int i=0; i<repeats; i++){
            for (int row=0; row<100000; row++){
                for (int col=0; col<sizes.length; col++){
                    m1.set(row, col, data[row][col]);
                }
            }
        }
        
        long write = System.currentTimeMillis() - start;
        
        // Copy
        for (int i=0; i<repeats; i++){
            for (int row=0; row<100000; row++){
                for (int col=0; col<sizes.length; col++){
                    m2.set(row, col, m1.get(row, col));
                }
            }
        }
        
        long copy = System.currentTimeMillis() - start - write;
        
        // Compare
        for (int i=0; i<repeats; i++){
            for (int row=0; row<100000; row++){
                equal = m1.equals(m2, row);
            }
        }
        
        long compare = System.currentTimeMillis() - start - copy - write;

        // Hashcode
        for (int i=0; i<repeats; i++){
            for (int row=0; row<100000; row++){
                code = m1.hashcode(row);
            }
        }
        
        long hashcode = System.currentTimeMillis() - start - copy - compare - write;
        
        System.out.println(m1.getClass().getSimpleName());
        System.out.println(" - Size     : "+m1.getByteSize());
        System.out.println(" - Write    : "+write);
        System.out.println(" - Copy     : "+copy);
        System.out.println(" - Compare  : "+compare);
        System.out.println(" - Hashcode : "+hashcode);
        System.out.println(" - Total    : "+(copy+compare+hashcode+write));
    }
    
    private static void benchmark3(MemoryUnsafe m1, MemoryUnsafe m2, byte[] sizes, int rows, int[][] data, int repeats){
        
        long start = System.currentTimeMillis();
        
        // Write
        for (int i=0; i<repeats; i++){
            for (int row=0; row<100000; row++){
                for (int col=0; col<sizes.length; col++){
                    m1.set(row, col, data[row][col]);
                }
            }
        }
        
        long write = System.currentTimeMillis() - start;
        
        // Copy
        for (int i=0; i<repeats; i++){
            for (int row=0; row<100000; row++){
                for (int col=0; col<sizes.length; col++){
                    m2.set(row, col, m1.get(row, col));
                }
            }
        }
        
        long copy = System.currentTimeMillis() - start - write;
        
        // Compare
        for (int i=0; i<repeats; i++){
            for (int row=0; row<100000; row++){
                equal = m1.equals(m2, row);
            }
        }
        
        long compare = System.currentTimeMillis() - start - copy - write;

        // Hashcode
        for (int i=0; i<repeats; i++){
            for (int row=0; row<100000; row++){
                code = m1.hashcode(row);
            }
        }
        
        long hashcode = System.currentTimeMillis() - start - copy - compare - write;
        
        System.out.println(m1.getClass().getSimpleName());
        System.out.println(" - Size     : "+m1.getByteSize());
        System.out.println(" - Write    : "+write);
        System.out.println(" - Copy     : "+copy);
        System.out.println(" - Compare  : "+compare);
        System.out.println(" - Hashcode : "+hashcode);
        System.out.println(" - Total    : "+(copy+compare+hashcode+write));
    }
}
