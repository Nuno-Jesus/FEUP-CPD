import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        char c;
        int lin, col, blockSize;
        int op = 1;

        Scanner sc = new Scanner(System.in);

        do{
            System.out.println();
            System.out.println("0. Exit");
            System.out.println("1. Normal Multiplication");
            System.out.println("2. Line Multiplication");
            System.out.println("3. Block Multiplication");
            System.out.print("Option: ");
            op = sc.nextInt();

            if (op == 0) break;

            System.out.print("Dimensions: lins=cols ? ");
            lin = sc.nextInt();
            col = lin;

            switch (op){
                case 1:
                    onMult(lin, col);
                    break;
                case 2:
                    onMultLine(lin, col);
                    break;
                case 3:
                    int divisors[] = getDivisors(lin);

                    System.out.print("Possible block sizes: ");
                    for (int i = 0; i < divisors.length; i++){
                        if (divisors[i] != 0) System.out.print(divisors[i] + " ");
                    }

                    System.out.print("\nBlock size: ");
                    blockSize = sc.nextInt();

                    // loop until the user enters a valid block size
                    while (blockSize > lin || blockSize < 1 || !contains(divisors, blockSize)){
                        System.out.print("Invalid block size. Block size: ");
                        blockSize = sc.nextInt();
                    }

                    onMultBlock(lin, col, blockSize);
                    break;
            }


        } while (op != 0) ;
    }

    private static int[] getDivisors(int n){
        int divisors[] = new int[n];
        int i, j = 0;

        for (i = 1; i <= n; i++){
            if (n % i == 0){
                divisors[j] = i;
                j++;
            }
        }

        return divisors;
    }

    private static boolean contains(int [] arr, int n){
        for (int i = 0; i < arr.length; i++){
            if (arr[i] == n) return true;
        }

        return false;
    }

    // "normal" matrix multiplication
    public static void onMult(int m_ar, int m_br){
        int matrixSize = m_ar * m_br;

        double pha[] = new double[matrixSize];
        double phb[] = new double[matrixSize];
        double phc[] = new double[matrixSize];

        int i, j;
        double temp;

        for (i = 0; i < m_ar; i++){
            for (j = 0; j < m_ar; j++){
                pha[i*m_ar + j] = 1.0;
            }
        }

        for (i = 0; i < m_br; i++){
            for (j = 0; j < m_br; j++){
                phb[i*m_br + j] = i + 1;
            }
        }

        // Start measuring time
        long startTime = System.nanoTime();

        for (i = 0; i < m_ar; i++){
            for (j = 0; j < m_br; j++){
                temp = 0;
                for (int k = 0; k < m_ar; k++){
                    temp += pha[i*m_ar + k] * phb[k*m_br + j];
                }
                phc[i*m_ar + j] = temp;
            }
        }

        // Stop measuring time and calculate the elapsed time
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        // Convert nanoseconds to seconds
        double seconds = (double)duration / 1000000000.0;


        //System.out.println("Time Elapsed: " + seconds + " seconds");
        System.out.printf("Time: %3.3f seconds\n", seconds);

        // displaying the first 10 elements of the result to verify correctness
        System.out.println("Result: ");
        for (i = 0; i <1; i++){
            for (j = 0; j < Math.min(10, m_br); j++){
                System.out.print(phc[j] + " ");
            }
        }

        System.out.println();
    }

    // line * line matrix multiplication
    public static void onMultLine(int m_ar, int m_br){
        int mSize = m_ar * m_br;
        int n = m_ar;

        double matrixA[] = new double[mSize];
        double matrixB[] = new double[mSize];
        double matrixRes[] = new double[mSize];

        int i, j, k;
        double temp;

        // initializing the matrices

        for (i = 0; i < n; i++){
            for (j = 0; j < n; j++){
                matrixA[i*m_ar + j] = 1.0;
            }
        }

        for (i = 0; i < n; i++){
            for (j = 0; j < n; j++){
                matrixB[i*m_br + j] = i + 1;
            }
        }

        // Start measuring time
        long startTime = System.nanoTime();

        // Line * line matrix multiplication

        // for each line of matrix A
        for (i = 0; i < n; i++){
            // for each column of matrix B
            for (k = 0; k < n; k++){
                temp = matrixA[i*n + k];
                // for each element of the line
                for (j = 0; j < n; j++){
                    matrixRes[i*n + j] += temp * matrixB[k*n + j];
                }
            }
        }


        // Stop measuring time and calculate the elapsed time
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        // Convert nanoseconds to seconds
        double seconds = (double)duration / 1000000000.0;

        System.out.printf("Time: %3.3f seconds\n", seconds);

        // displaying the first 10 elements of the result to verify correctness
        System.out.println("Result: ");
        for (i = 0; i <1; i++){
            for (j = 0; j < Math.min(10, m_br); j++){
                System.out.print(matrixRes[j] + " ");
            }
        }

        System.out.println();
    }

    // block * block matrix multiplication
    public static void onMultBlock(int m, int n, int bkSize){
        // size of the array that will hold the matrices, 2d to 1d conversion
        int mSize = m * n;

        double matrixA[] = new double[mSize];
        double matrixB[] = new double[mSize];

        // initializing the matrices
        for (int i = 0; i < m; i++){
            for (int j = 0; j < n; j++){
                matrixA[i*m + j] = 1.0;
            }
        }

        for (int i = 0; i < m; i++){
            for (int j = 0; j < n; j++){
                matrixB[i*n + j] = i + 1;
            }
        }

        // the matrix is divided into nBlocks blocks of size bkSize * bkSize
        int nBlocks = m / bkSize;

        double block[] = new double[bkSize * bkSize];

        for (int i = 0; i < bkSize; i++){
            for (int j = 0; j < bkSize; j++){
                block[i*bkSize + j] = matrixA[i*m + j];
            }
        }

        for (int i = 0; i < bkSize; i++){
            for (int j = 0; j < bkSize; j++){
                System.out.print(block[i*bkSize + j] + " ");
            }
            System.out.println();
        }

        return;

    }
}
