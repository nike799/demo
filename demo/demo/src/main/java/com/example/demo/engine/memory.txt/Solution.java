import java.util.Scanner;

public class Solution {
public static void main(String[] args) {
Scanner scanner = new Scanner(System.in);
String[] input = scanner.nextLine().split(",\\s+");

int size = Integer.parseInt(input[0]);
String pattern = input[1];
int[][] matrix;

if (pattern.equals("A")) {
matrix = createMatrixByPatternA(size);

} else {
matrix = createMatrixByPatternB(size);

}


printMatrix(matrix);

}

private static void printMatrix(int[][] matrix) {
for (int r = 0; r < matrix.length; r++) {
for (int c = 0; c < matrix[r].length; c++) {
System.out.print(matrix[r][c] + " ");
}
System.out.println();
}

}

private static int[][] createMatrixByPatternA(int size) { // Square Matrix
int[][] matrix = new int[size][size];
int value = 1;
for (int r = 0; r < size; r++) {
for (int c = 0; c < size; c++) {
matrix[c][r] = value++;
}
}

return matrix;
}

private static int[][] createMatrixByPatternB(int size) { //Square Matrix
int[][] matrix = new int[size][size];
int value = 1;

for (int c = 0; c < size; c++) {
if (c % 2 == 0) {
for (int r = 0; r < size; r++) {
matrix[r][c] = value++;
}
} else {
for (int r = size - 1; r >= 0; r--) {
matrix[r][c] = value++;
}

}
}

return matrix;
}
}