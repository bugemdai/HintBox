package com.example.hintbox;

import java.util.Arrays;

public class CubieCube {

    static CubieCube[] cubeSymmetries = new CubieCube[16];
    static CubieCube[] moveCubes = new CubieCube[18];
    static long[] moveCubeSymmetries = new long[18];
    static int[] firstMoveSymmetries = new int[48];

    static int[][] symmetryMultiplication = new int[16][16];
    static int[][] inverseSymmetryMultiplication = new int[16][16];
    static int[][] symmetryMoves = new int[16][18];
    static int[] symmetry8Moves = new int[8 * 18];
    static int[][] symmetryMoveUD = new int[16][18];

    static char[] flipSymmetryToRaw = new char[CoordCube.N_FLIP_SYM];
    static char[] twistSymmetryToRaw = new char[CoordCube.N_TWIST_SYM];
    static char[] edgePermutationSymmetryToRaw = new char[CoordCube.N_PERM_SYM];
    static byte[] permutationToCombinationP = new byte[CoordCube.N_PERM_SYM];
    static char[] inverseEdgePermutationSymmetry = new char[CoordCube.N_PERM_SYM];
    static byte[] inverseMiddlePermutation = new byte[CoordCube.N_MPERM];

    static final int SYMMETRY_EDGE_TO_CORNER_MAGIC = 0x00DDDD00;

    static char[] flipRawToSymmetry = new char[CoordCube.N_FLIP];
    static char[] twistRawToSymmetry = new char[CoordCube.N_TWIST];
    static char[] edgePermutationRawToSymmetry = new char[CoordCube.N_PERM];
    static char[] flipSymmetryToRawFull = Search.USE_TWIST_FLIP_PRUN ? new char[CoordCube.N_FLIP_SYM * 8] : null;

    static char[] symmetryStateTwist;
    static char[] symmetryStateFlip;
    static char[] symmetryStatePermutation;

    static CubieCube urf1 = new CubieCube(2531, 1373, 67026819, 1367);
    static CubieCube urf2 = new CubieCube(2089, 1906, 322752913, 2040);
    static byte[][] urfMoves = new byte[][] {
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17},
            {6, 7, 8, 0, 1, 2, 3, 4, 5, 15, 16, 17, 9, 10, 11, 12, 13, 14},
            {3, 4, 5, 6, 7, 8, 0, 1, 2, 12, 13, 14, 15, 16, 17, 9, 10, 11},
            {2, 1, 0, 5, 4, 3, 8, 7, 6, 11, 10, 9, 14, 13, 12, 17, 16, 15},
            {8, 7, 6, 2, 1, 0, 5, 4, 3, 17, 16, 15, 11, 10, 9, 14, 13, 12},
            {5, 4, 3, 8, 7, 6, 2, 1, 0, 14, 13, 12, 17, 16, 15, 11, 10, 9}
    };

    byte[] cornerArray = {0, 1, 2, 3, 4, 5, 6, 7};
    byte[] edgeArray = {0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22};
    CubieCube temporaryCube = null;

    CubieCube() {
    }

    CubieCube(int cornerPermutation, int twist, int edgePermutation, int flip) {
        this.setCornerPermutation(cornerPermutation);
        this.setTwist(twist);
        Util.setNPerm(edgeArray, edgePermutation, 12, true);
        this.setFlip(flip);
    }

    CubieCube(CubieCube cube) {
        copy(cube);
    }

    void copy(CubieCube cube) {
        for (int i = 0; i < 8; i++) {
            this.cornerArray[i] = cube.cornerArray[i];
        }
        for (int i = 0; i < 12; i++) {
            this.edgeArray[i] = cube.edgeArray[i];
        }
    }

    void invertCubieCube() {
        if (temporaryCube == null) {
            temporaryCube = new CubieCube();
        }
        for (byte edge = 0; edge < 12; edge++) {
            temporaryCube.edgeArray[edgeArray[edge] >> 1] = (byte) (edge << 1 | edgeArray[edge] & 1);
        }
        for (byte corner = 0; corner < 8; corner++) {
            temporaryCube.cornerArray[cornerArray[corner] & 0x7] = (byte) (corner | 0x20 >> (cornerArray[corner] >> 3) & 0x18);
        }
        copy(temporaryCube);
    }

    static void multiplyCorners(CubieCube a, CubieCube b, CubieCube product) {
        for (int corner = 0; corner < 8; corner++) {
            int orientationA = a.cornerArray[b.cornerArray[corner] & 7] >> 3;
            int orientationB = b.cornerArray[corner] >> 3;
            product.cornerArray[corner] = (byte) (a.cornerArray[b.cornerArray[corner] & 7] & 7 | (orientationA + orientationB) % 3 << 3);
        }
    }

    static void multiplyCornersFull(CubieCube a, CubieCube b, CubieCube product) {
        for (int corner = 0; corner < 8; corner++) {
            int orientationA = a.cornerArray[b.cornerArray[corner] & 7] >> 3;
            int orientationB = b.cornerArray[corner] >> 3;
            int orientation = orientationA + ((orientationA < 3) ? orientationB : 6 - orientationB);
            orientation = orientation % 3 + ((orientationA < 3) == (orientationB < 3) ? 0 : 3);
            product.cornerArray[corner] = (byte) (a.cornerArray[b.cornerArray[corner] & 7] & 7 | orientation << 3);
        }
    }

    static void multiplyEdges(CubieCube a, CubieCube b, CubieCube product) {
        for (int edge = 0; edge < 12; edge++) {
            product.edgeArray[edge] = (byte) (a.edgeArray[b.edgeArray[edge] >> 1] ^ (b.edgeArray[edge] & 1));
        }
    }

    static void conjugateCorners(CubieCube a, int index, CubieCube b) {
        CubieCube inverseSymmetry = cubeSymmetries[inverseSymmetryMultiplication[0][index]];
        CubieCube symmetry = cubeSymmetries[index];
        for (int corner = 0; corner < 8; corner++) {
            int orientationA = inverseSymmetry.cornerArray[a.cornerArray[symmetry.cornerArray[corner] & 7] & 7] >> 3;
            int orientationB = a.cornerArray[symmetry.cornerArray[corner] & 7] >> 3;
            int orientation = (orientationA < 3) ? orientationB : (3 - orientationB) % 3;
            b.cornerArray[corner] = (byte) (inverseSymmetry.cornerArray[a.cornerArray[symmetry.cornerArray[corner] & 7] & 7] & 7 | orientation << 3);
        }
    }

    static void conjugateEdges(CubieCube a, int index, CubieCube b) {
        CubieCube inverseSymmetry = cubeSymmetries[inverseSymmetryMultiplication[0][index]];
        CubieCube symmetry = cubeSymmetries[index];
        for (int edge = 0; edge < 12; edge++) {
            b.edgeArray[edge] = (byte) (inverseSymmetry.edgeArray[a.edgeArray[symmetry.edgeArray[edge] >> 1] >> 1] ^ (a.edgeArray[symmetry.edgeArray[edge] >> 1] & 1) ^ (symmetry.edgeArray[edge] & 1));
        }
    }

    static int getPermutationSymmetryInverse(int index, int symmetry, boolean isCorner) {
        int indexInverse = inverseEdgePermutationSymmetry[index];
        if (isCorner) {
            indexInverse = edgeSymmetryToCornerSymmetry(indexInverse);
        }
        return indexInverse & 0xfff0 | symmetryMultiplication[indexInverse & 0xf][symmetry];
    }

    static int getSkipMoves(long symmetryState) {
        int result = 0;
        for (int i = 1; (symmetryState >>= 1) != 0; i++) {
            if ((symmetryState & 1) == 1) {
                result |= firstMoveSymmetries[i];
            }
        }
        return result;
    }

    void urfConjugate() {
        if (temporaryCube == null) {
            temporaryCube = new CubieCube();
        }
        multiplyCorners(urf2, this, temporaryCube);
        multiplyCorners(temporaryCube, urf1, this);
        multiplyEdges(urf2, this, temporaryCube);
        multiplyEdges(temporaryCube, urf1, this);
    }

    int getFlip() {
        int index = 0;
        for (int i = 0; i < 11; i++) {
            index = index << 1 | edgeArray[i] & 1;
        }
        return index;
    }

    void setFlip(int index) {
        int parity = 0, value;
        for (int i = 10; i >= 0; i--, index >>= 1) {
            parity ^= (value = index & 1);
            edgeArray[i] = (byte) (edgeArray[i] & ~1 | value);
        }
        edgeArray[11] = (byte) (edgeArray[11] & ~1 | parity);
    }

    int getFlipSymmetry() {
        return flipRawToSymmetry[getFlip()];
    }

    int getTwist() {
        int index = 0;
        for (int i = 0; i < 7; i++) {
            index += (index << 1) + (cornerArray[i] >> 3);
        }
        return index;
    }

    void setTwist(int index) {
        int twist = 15, value;
        for (int i = 6; i >= 0; i--, index /= 3) {
            twist -= (value = index % 3);
            cornerArray[i] = (byte) (cornerArray[i] & 0x7 | value << 3);
        }
        cornerArray[7] = (byte) (cornerArray[7] & 0x7 | (twist % 3) << 3);
    }

    int getTwistSymmetry() {
        return twistRawToSymmetry[getTwist()];
    }

    int getUDSlice() {
        return 494 - Util.getComb(edgeArray, 8, true);
    }

    void setUDSlice(int index) {
        Util.setComb(edgeArray, 494 - index, 8, true);
    }

    int getCornerPermutation() {
        return Util.getNPerm(cornerArray, 8, false);
    }

    void setCornerPermutation(int index) {
        Util.setNPerm(cornerArray, index, 8, false);
    }

    int getCornerPermutationSymmetry() {
        return edgeSymmetryToCornerSymmetry(edgePermutationRawToSymmetry[getCornerPermutation()]);
    }

    int getEdgePermutation() {
        return Util.getNPerm(edgeArray, 8, true);
    }

    void setEdgePermutation(int index) {
        Util.setNPerm(edgeArray, index, 8, true);
    }

    int getEdgePermutationSymmetry() {
        return edgePermutationRawToSymmetry[getEdgePermutation()];
    }

    int getMiddlePermutation() {
        return Util.getNPerm(edgeArray, 12, true) % 24;
    }

    void setMiddlePermutation(int index) {
        Util.setNPerm(edgeArray, index, 12, true);
    }

    int getCornerCombination() {
        return Util.getComb(cornerArray, 0, false);
    }

    void setCornerCombination(int index) {
        Util.setComb(cornerArray, index, 0, false);
    }

    int verify() {
        int sum = 0;
        int edgeMask = 0;
        for (int edge = 0; edge < 12; edge++) {
            edgeMask |= 1 << (edgeArray[edge] >> 1);
            sum ^= edgeArray[edge] & 1;
        }
        if (edgeMask != 0xfff) {
            return -2; // missing edges
        }
        if (sum != 0) {
            return -3;
        }
        int cornerMask = 0;
        sum = 0;
        for (int corner = 0; corner < 8; corner++) {
            cornerMask |= 1 << (cornerArray[corner] & 7);
            sum += cornerArray[corner] >> 3;
        }
        if (cornerMask != 0xff) {
            return -4; // missing corners
        }
        if (sum % 3 != 0) {
            return -5; // twisted corner
        }
        if ((Util.getNParity(Util.getNPerm(edgeArray, 12, true), 12) ^ Util.getNParity(getCornerPermutation(), 8)) != 0) {
            return -6; // parity error
        }
        return 0; // cube ok
    }

    long selfSymmetry() {
        CubieCube cubeCopy = new CubieCube(this);
        CubieCube cubeTemp = new CubieCube();
        int cornerPermutation = cubeCopy.getCornerPermutationSymmetry() >> 4;
        long symmetry = 0L;
        for (int urfInverse = 0; urfInverse < 6; urfInverse++) {
            int cornerPermutationX = cubeCopy.getCornerPermutationSymmetry() >> 4;
            if (cornerPermutation == cornerPermutationX) {
                for (int i = 0; i < 16; i++) {
                    conjugateCorners(cubeCopy, inverseSymmetryMultiplication[0][i], cubeTemp);
                    if (Arrays.equals(cubeTemp.cornerArray, cornerArray)) {
                        conjugateEdges(cubeCopy, inverseSymmetryMultiplication[0][i], cubeTemp);
                        if (Arrays.equals(cubeTemp.edgeArray, edgeArray)) {
                            symmetry |= 1L << Math.min(urfInverse << 4 | i, 48);
                        }
                    }
                }
            }
            cubeCopy.urfConjugate();
            if (urfInverse % 3 == 2) {
                cubeCopy.invertCubieCube();
            }
        }
        return symmetry;
    }

    static void initializeMoves() {
        moveCubes[0] = new CubieCube(15120, 0, 119750400, 0);
        moveCubes[3] = new CubieCube(21021, 1494, 323403417, 0);
        moveCubes[6] = new CubieCube(8064, 1236, 29441808, 550);
        moveCubes[9] = new CubieCube(9, 0, 5880, 0);
        moveCubes[12] = new CubieCube(1230, 412, 2949660, 0);
        moveCubes[15] = new CubieCube(224, 137, 328552, 137);
        for (int a = 0; a < 18; a += 3) {
            for (int p = 0; p < 2; p++) {
                moveCubes[a + p + 1] = new CubieCube();
                multiplyEdges(moveCubes[a + p], moveCubes[a], moveCubes[a + p + 1]);
                multiplyCorners(moveCubes[a + p], moveCubes[a], moveCubes[a + p + 1]);
            }
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            stringBuilder.append("|" + (cornerArray[i] & 7) + " " + (cornerArray[i] >> 3));
        }
        stringBuilder.append("\n");
        for (int i = 0; i < 12; i++) {
            stringBuilder.append("|" + (edgeArray[i] >> 1) + " " + (edgeArray[i] & 1));
        }
        return stringBuilder.toString();
    }

    static void initializeSymmetries() {
        CubieCube cube = new CubieCube();
        CubieCube tempCube = new CubieCube();
        CubieCube swap;

        CubieCube f2 = new CubieCube(28783, 0, 259268407, 0);
        CubieCube u4 = new CubieCube(15138, 0, 119765538, 7);
        CubieCube lr2 = new CubieCube(5167, 0, 83473207, 0);
        for (int i = 0; i < 8; i++) {
            lr2.cornerArray[i] |= 3 << 3;
        }

        for (int i = 0; i < 16; i++) {
            cubeSymmetries[i] = new CubieCube(cube);
            multiplyCornersFull(cube, u4, tempCube);
            multiplyEdges(cube, u4, tempCube);
            swap = tempCube;  tempCube = cube;  cube = swap;
            if (i % 4 == 3) {
                multiplyCornersFull(cube, lr2, tempCube);
                multiplyEdges(cube, lr2, tempCube);
                swap = tempCube;  tempCube = cube;  cube = swap;
            }
            if (i % 8 == 7) {
                multiplyCornersFull(cube, f2, tempCube);
                multiplyEdges(cube, f2, tempCube);
                swap = tempCube;  tempCube = cube;  cube = swap;
            }
        }
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                multiplyCornersFull(cubeSymmetries[i], cubeSymmetries[j], cube);
                for (int k = 0; k < 16; k++) {
                    if (Arrays.equals(cubeSymmetries[k].cornerArray, cube.cornerArray)) {
                        symmetryMultiplication[i][j] = k;
                        inverseSymmetryMultiplication[k][j] = i;
                        break;
                    }
                }
            }
        }
        for (int j = 0; j < 18; j++) {
            for (int s = 0; s < 16; s++) {
                conjugateCorners(moveCubes[j], inverseSymmetryMultiplication[0][s], cube);
                for (int m = 0; m < 18; m++) {
                    if (Arrays.equals(moveCubes[m].cornerArray, cube.cornerArray)) {
                        symmetryMoves[s][j] = m;
                        symmetryMoveUD[s][Util.std2ud[j]] = Util.std2ud[m];
                        break;
                    }
                }
                if (s % 2 == 0) {
                    symmetry8Moves[j << 3 | s >> 1] = symmetryMoves[s][j];
                }
            }
        }

        for (int i = 0; i < 18; i++) {
            moveCubeSymmetries[i] = moveCubes[i].selfSymmetry();
            int j = i;
            for (int s = 0; s < 48; s++) {
                if (symmetryMoves[s % 16][j] < i) {
                    firstMoveSymmetries[s] |= 1 << i;
                }
                if (s % 16 == 15) {
                    j = urfMoves[2][j];
                }
            }
        }
    }

    static int initializeSymmetryToRaw(final int N_RAW, char[] symmetryToRaw, char[] rawToSymmetry, char[] symmetryState, int coordinate) {
        final int N_RAW_HALF = (N_RAW + 1) / 2;
        CubieCube cube = new CubieCube();
        CubieCube tempCube = new CubieCube();
        int count = 0, index = 0;
        int symmetryIncrement = coordinate >= 2 ? 1 : 2;
        boolean isEdge = coordinate != 1;

        for (int i = 0; i < N_RAW; i++) {
            if (rawToSymmetry[i] != 0) {
                continue;
            }
            switch (coordinate) {
                case 0: cube.setFlip(i); break;
                case 1: cube.setTwist(i); break;
                case 2: cube.setEdgePermutation(i); break;
            }
            for (int s = 0; s < 16; s += symmetryIncrement) {
                if (isEdge) {
                    conjugateEdges(cube, s, tempCube);
                } else {
                    conjugateCorners(cube, s, tempCube);
                }
                switch (coordinate) {
                    case 0: index = tempCube.getFlip();
                        break;
                    case 1: index = tempCube.getTwist();
                        break;
                    case 2: index = tempCube.getEdgePermutation();
                        break;
                }
                if (coordinate == 0 && Search.USE_TWIST_FLIP_PRUN) {
                    flipSymmetryToRawFull[count << 3 | s >> 1] = (char) index;
                }
                if (index == i) {
                    symmetryState[count] |= 1 << (s / symmetryIncrement);
                }
                int symmetryIndex = (count << 4 | s) / symmetryIncrement;
                rawToSymmetry[index] = (char) symmetryIndex;
            }
            symmetryToRaw[count++] = (char) i;
        }
        return count;
    }

    static void initializeFlipSymmetryToRaw() {
        initializeSymmetryToRaw(CoordCube.N_FLIP, flipSymmetryToRaw, flipRawToSymmetry,
                symmetryStateFlip = new char[CoordCube.N_FLIP_SYM], 0);
    }

    static void initializeTwistSymmetryToRaw() {
        initializeSymmetryToRaw(CoordCube.N_TWIST, twistSymmetryToRaw, twistRawToSymmetry,
                symmetryStateTwist = new char[CoordCube.N_TWIST_SYM], 1);
    }

    static void initializePermutationSymmetryToRaw() {
        initializeSymmetryToRaw(CoordCube.N_PERM, edgePermutationSymmetryToRaw, edgePermutationRawToSymmetry,
                symmetryStatePermutation = new char[CoordCube.N_PERM_SYM], 2);
        CubieCube cube = new CubieCube();
        for (int i = 0; i < CoordCube.N_PERM_SYM; i++) {
            cube.setEdgePermutation(edgePermutationSymmetryToRaw[i]);
            permutationToCombinationP[i] = (byte) (Util.getComb(cube.edgeArray, 0, true) + (Search.USE_COMBP_PRUN ? Util.getNParity(edgePermutationSymmetryToRaw[i], 8) * 70 : 0));
            cube.invertCubieCube();
            inverseEdgePermutationSymmetry[i] = (char) cube.getEdgePermutationSymmetry();
        }
        for (int i = 0; i < CoordCube.N_MPERM; i++) {
            cube.setMiddlePermutation(i);
            cube.invertCubieCube();
            inverseMiddlePermutation[i] = (byte) cube.getMiddlePermutation();
        }
    }

    static {
        CubieCube.initializeMoves();
        CubieCube.initializeSymmetries();
    }

    static int edgeSymmetryToCornerSymmetry(int index) {
        return index ^ (SYMMETRY_EDGE_TO_CORNER_MAGIC >> ((index & 0xf) << 1) & 3);
    }
}
