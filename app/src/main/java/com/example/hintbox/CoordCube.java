package com.example.hintbox;

public class CoordCube {
    // Constants for the number of moves in each phase
    static final int NUM_MOVES_PHASE1 = 18;
    static final int NUM_MOVES_PHASE2 = 10;

    // Constants for various cube states and symmetries
    static final int NUM_SLICE = 495;
    static final int NUM_TWIST = 2187;
    static final int NUM_TWIST_SYM = 324;
    static final int NUM_FLIP = 2048;
    static final int NUM_FLIP_SYM = 336;
    static final int NUM_PERM = 40320;
    static final int NUM_PERM_SYM = 2768;
    static final int NUM_MPERM = 24;
    static final int NUM_COMB = Search.USE_COMBP_PRUN ? 140 : 70;
    static final int PARITY_MOVE_PHASE2 = Search.USE_COMBP_PRUN ? 0xA5 : 0;

    // Phase 1 move tables
    static char[][] udSliceMove = new char[NUM_SLICE][NUM_MOVES_PHASE1];
    static char[][] twistMove = new char[NUM_TWIST_SYM][NUM_MOVES_PHASE1];
    static char[][] flipMove = new char[NUM_FLIP_SYM][NUM_MOVES_PHASE1];
    static char[][] udSliceConjugate = new char[NUM_SLICE][8];
    static int[] udSliceTwistPruning = new int[NUM_SLICE * NUM_TWIST_SYM / 8 + 1];
    static int[] udSliceFlipPruning = new int[NUM_SLICE * NUM_FLIP_SYM / 8 + 1];
    static int[] twistFlipPruning = Search.USE_TWIST_FLIP_PRUN ? new int[NUM_FLIP * NUM_TWIST_SYM / 8 + 1] : null;

    // Phase 2 move tables
    static char[][] cornerPermMove = new char[NUM_PERM_SYM][NUM_MOVES_PHASE2];
    static char[][] edgePermMove = new char[NUM_PERM_SYM][NUM_MOVES_PHASE2];
    static char[][] middlePermMove = new char[NUM_MPERM][NUM_MOVES_PHASE2];
    static char[][] middlePermConjugate = new char[NUM_MPERM][16];
    static char[][] cornerCombPermMove;
    static char[][] cornerCombPermConjugate = new char[NUM_COMB][16];
    static int[] middleCornerPermPruning = new int[NUM_MPERM * NUM_PERM_SYM / 8 + 1];
    static int[] edgePermCornerCombPermPruning = new int[NUM_COMB * NUM_PERM_SYM / 8 + 1];

    // Initialization level
    static int initializationLevel = 0;

    // Initialize the CoordCube with necessary data
    static synchronized void initialize(boolean fullInitialization) {
        if (initializationLevel == 2 || initializationLevel == 1 && !fullInitialization) {
            return;
        }
        if (initializationLevel == 0) {
            CubieCube.initializePermSym2Raw();
            initializeCornerPermMove();
            initializeEdgePermMove();
            initializeMiddlePermMoveConjugate();
            initializeCombPermMoveConjugate();

            CubieCube.initializeFlipSym2Raw();
            CubieCube.initializeTwistSym2Raw();
            initializeFlipMove();
            initializeTwistMove();
            initializeUdSliceMoveConjugate();
        }
        initializeMiddleCornerPermPruning(fullInitialization);
        initializePermCombPermPruning(fullInitialization);
        initializeSliceTwistPruning(fullInitialization);
        initializeSliceFlipPruning(fullInitialization);
        if (Search.USE_TWIST_FLIP_PRUN) {
            initializeTwistFlipPruning(fullInitialization);
        }
        initializationLevel = fullInitialization ? 2 : 1;
    }

    // Set pruning value in the pruning table
    static void setPruning(int[] table, int index, int value) {
        table[index >> 3] ^= value << (index << 2); // index << 2 <=> (index & 7) << 2
    }

    // Get pruning value from the pruning table
    static int getPruning(int[] table, int index) {
        return table[index >> 3] >> (index << 2) & 0xf; // index << 2 <=> (index & 7) << 2
    }

    // Initialize UD slice move and conjugate tables
    static void initializeUdSliceMoveConjugate() {
        CubieCube cube = new CubieCube();
        CubieCube tempCube = new CubieCube();
        for (int i = 0; i < NUM_SLICE; i++) {
            cube.setUdSlice(i);
            for (int j = 0; j < NUM_MOVES_PHASE1; j += 3) {
                CubieCube.edgeMultiply(cube, CubieCube.moveCube[j], tempCube);
                udSliceMove[i][j] = (char) tempCube.getUdSlice();
            }
            for (int j = 0; j < 16; j += 2) {
                CubieCube.edgeConjugate(cube, CubieCube.symMultInverse[0][j], tempCube);
                udSliceConjugate[i][j >> 1] = (char) tempCube.getUdSlice();
            }
        }
        for (int i = 0; i < NUM_SLICE; i++) {
            for (int j = 0; j < NUM_MOVES_PHASE1; j += 3) {
                int udSlice = udSliceMove[i][j];
                for (int k = 1; k < 3; k++) {
                    udSlice = udSliceMove[udSlice][j];
                    udSliceMove[i][j + k] = (char) udSlice;
                }
            }
        }
    }

    // Initialize flip move table
    static void initializeFlipMove() {
        CubieCube cube = new CubieCube();
        CubieCube tempCube = new CubieCube();
        for (int i = 0; i < NUM_FLIP_SYM; i++) {
            cube.setFlip(CubieCube.flipSym2Raw[i]);
            for (int j = 0; j < NUM_MOVES_PHASE1; j++) {
                CubieCube.edgeMultiply(cube, CubieCube.moveCube[j], tempCube);
                flipMove[i][j] = (char) tempCube.getFlipSym();
            }
        }
    }

    // Initialize twist move table
    static void initializeTwistMove() {
        CubieCube cube = new CubieCube();
        CubieCube tempCube = new CubieCube();
        for (int i = 0; i < NUM_TWIST_SYM; i++) {
            cube.setTwist(CubieCube.twistSym2Raw[i]);
            for (int j = 0; j < NUM_MOVES_PHASE1; j++) {
                CubieCube.cornerMultiply(cube, CubieCube.moveCube[j], tempCube);
                twistMove[i][j] = (char) tempCube.getTwistSym();
            }
        }
    }

    // Initialize corner permutation move table
    static void initializeCornerPermMove() {
        CubieCube cube = new CubieCube();
        CubieCube tempCube = new CubieCube();
        for (int i = 0; i < NUM_PERM_SYM; i++) {
            cube.setCornerPerm(CubieCube.edgePermSym2Raw[i]);
            for (int j = 0; j < NUM_MOVES_PHASE2; j++) {
                CubieCube.cornerMultiply(cube, CubieCube.moveCube[Util.ud2std[j]], tempCube);
                cornerPermMove[i][j] = (char) tempCube.getCornerPermSym();
            }
        }
    }

    // Initialize edge permutation move table
    static void initializeEdgePermMove() {
        CubieCube cube = new CubieCube();
        CubieCube tempCube = new CubieCube();
        for (int i = 0; i < NUM_PERM_SYM; i++) {
            cube.setEdgePerm(CubieCube.edgePermSym2Raw[i]);
            for (int j = 0; j < NUM_MOVES_PHASE2; j++) {
                CubieCube.edgeMultiply(cube, CubieCube.moveCube[Util.ud2std[j]], tempCube);
                edgePermMove[i][j] = (char) tempCube.getEdgePermSym();
            }
        }
    }

    // Initialize middle permutation move and conjugate tables
    static void initializeMiddlePermMoveConjugate() {
        CubieCube cube = new CubieCube();
        CubieCube tempCube = new CubieCube();
        for (int i = 0; i < NUM_MPERM; i++) {
            cube.setMiddlePerm(i);
            for (int j = 0; j < NUM_MOVES_PHASE2; j++) {
                CubieCube.edgeMultiply(cube, CubieCube.moveCube[Util.ud2std[j]], tempCube);
                middlePermMove[i][j] = (char) tempCube.getMiddlePerm();
            }
            for (int j = 0; j < 16; j++) {
                CubieCube.edgeConjugate(cube, CubieCube.symMultInverse[0][j], tempCube);
                middlePermConjugate[i][j] = (char) tempCube.getMiddlePerm();
            }
        }
    }

    // Initialize combination permutation move and conjugate tables
    static void initializeCombPermMoveConjugate() {
        CubieCube cube = new CubieCube();
        CubieCube tempCube = new CubieCube();
        cornerCombPermMove = new char[NUM_COMB][NUM_MOVES_PHASE2];
        for (int i = 0; i < NUM_COMB; i++) {
            cube.setCornerComb(i % 70);
            for (int j = 0; j < NUM_MOVES_PHASE2; j++) {
                CubieCube.cornerMultiply(cube, CubieCube.moveCube[Util.ud2std[j]], tempCube);
                cornerCombPermMove[i][j] = (char) (tempCube.getCornerComb() + 70 * ((PARITY_MOVE_PHASE2 >> j & 1) ^ (i / 70)));
            }
            for (int j = 0; j < 16; j++) {
                CubieCube.cornerConjugate(cube, CubieCube.symMultInverse[0][j], tempCube);
                cornerCombPermConjugate[i][j] = (char) (tempCube.getCornerComb() + 70 * (i / 70));
            }
        }
    }

    // Check if a value contains zero in its binary representation
    static boolean containsZero(int value) {
        return ((value - 0x11111111) & ~value & 0x88888888) != 0;
    }

    // Initialize raw symmetry pruning tables
    static void initializeRawSymPruning(int[] pruningTable,
                                        final char[][] rawMove, final char[][] rawConjugate,
                                        final char[][] symMove, final char[] symState,
                                        final int pruningFlag, final boolean fullInitialization) {

        final int SYM_SHIFT = pruningFlag & 0xf;
        final int SYM_E2C_MAGIC = ((pruningFlag >> 4) & 1) == 1 ? CubieCube.SYM_E2C_MAGIC : 0x00000000;
        final boolean IS_PHASE2 = ((pruningFlag >> 5) & 1) == 1;
        final int INV_DEPTH = pruningFlag >> 8 & 0xf;
        final int MAX_DEPTH = pruningFlag >> 12 & 0xf;
        final int MIN_DEPTH = pruningFlag >> 16 & 0xf;
        final int SEARCH_DEPTH = fullInitialization ? MAX_DEPTH : MIN_DEPTH;

        final int SYM_MASK = (1 << SYM_SHIFT) - 1;
        final boolean IS_TWIST_FLIP_PRUNING = rawMove == null;
        final int NUM_RAW = IS_TWIST_FLIP_PRUNING ? NUM_FLIP : rawMove.length;
        final int NUM_SIZE = NUM_RAW * symMove.length;
        final int NUM_MOVES = IS_PHASE2 ? 10 : 18;
        final int NEXT_AXIS_MAGIC = NUM_MOVES == 10 ? 0x42 : 0x92492;

        int depth = getPruning(pruningTable, NUM_SIZE) - 1;
        int completed = 0;

        if (depth == -1) {
            for (int i = 0; i < NUM_SIZE / 8 + 1; i++) {
                pruningTable[i] = 0x11111111;
            }
            setPruning(pruningTable, 0, 0 ^ 1);
            depth = 0;
            completed = 1;
        }

        while (depth < SEARCH_DEPTH) {
            int mask = (depth + 1) * 0x11111111 ^ 0xffffffff;
            for (int i = 0; i < pruningTable.length; i++) {
                int value = pruningTable[i] ^ mask;
                value &= value >> 1;
                pruningTable[i] += value & (value >> 2) & 0x11111111;
            }

            boolean inverse = depth > INV_DEPTH;
            int select = inverse ? (depth + 2) : depth;
            int selectArrayMask = select * 0x11111111;
            int check = inverse ? depth : (depth + 2);
            depth++;
            int xorValue = depth ^ (depth + 1);
            int value = 0;
            for (int i = 0; i < NUM_SIZE; i++, value >>= 4) {
                if ((i & 7) == 0) {
                    value = pruningTable[i >> 3];
                    if (!containsZero(value ^ selectArrayMask)) {
                        i += 7;
                        continue;
                    }
                }
                if ((value & 0xf) != select) {
                    continue;
                }
                int raw = i % NUM_RAW;
                int sym = i / NUM_RAW;
                int flip = 0, flipSym = 0;
                if (IS_TWIST_FLIP_PRUNING) {
                    flip = CubieCube.flipRawToSym[raw];
                    flipSym = flip & 7;
                    flip >>= 3;
                }

                for (int move = 0; move < NUM_MOVES; move++) {
                    int symX = symMove[sym][move];
                    int rawX;
                    if (IS_TWIST_FLIP_PRUNING) {
                        rawX = CubieCube.flipSymToRawFlip[
                                flipMove[flip][CubieCube.sym8Move[move << 3 | flipSym]] ^
                                        flipSym ^ (symX & SYM_MASK)];
                    } else {
                        rawX = rawConjugate[rawMove[raw][move]][symX & SYM_MASK];

                    }
                    symX >>= SYM_SHIFT;
                    int index = symX * NUM_RAW + rawX;
                    int pruning = getPruning(pruningTable, index);
                    if (pruning != check) {
                        if (pruning < depth - 1) {
                            move += NEXT_AXIS_MAGIC >> move & 3;
                        }
                        continue;
                    }
                    completed++;
                    if (inverse) {
                        setPruning(pruningTable, i, xorValue);
                        break;
                    }
                    setPruning(pruningTable, index, xorValue);
                    for (int j = 1, symState = symState[symX]; (symState >>= 1) != 0; j++) {
                        if ((symState & 1) != 1) {
                            continue;
                        }
                        int indexX = symX * NUM_RAW;
                        if (IS_TWIST_FLIP_PRUNING) {
                            indexX += CubieCube.flipSymToRawFlip[CubieCube.flipRawToSym[rawX] ^ j];
                        } else {
                            indexX += rawConjugate[rawX][j ^ (SYM_E2C_MAGIC >> (j << 1) & 3)];
                        }
                        if (getPruning(pruningTable, indexX) == check) {
                            setPruning(pruningTable, indexX, xorValue);
                            completed++;
                        }
                    }
                }
            }
        }
    }

    // Initialize twist-flip pruning table
    static void initializeTwistFlipPruning(boolean fullInitialization) {
        initializeRawSymPruning(
                twistFlipPruning,
                null, null,
                twistMove, CubieCube.symStateTwist, 0x19603,
                fullInitialization
        );
    }

    // Initialize slice-twist pruning table
    static void initializeSliceTwistPruning(boolean fullInitialization) {
        initializeRawSymPruning(
                udSliceTwistPruning,
                udSliceMove, udSliceConjugate,
                twistMove, CubieCube.symStateTwist, 0x69603,
                fullInitialization
        );
    }

    // Initialize slice-flip pruning table
    static void initializeSliceFlipPruning(boolean fullInitialization) {
        initializeRawSymPruning(
                udSliceFlipPruning,
                udSliceMove, udSliceConjugate,
                flipMove, CubieCube.symStateFlip, 0x69603,
                fullInitialization
        );
    }

    // Initialize middle-corner permutation pruning table
    static void initializeMiddleCornerPermPruning(boolean fullInitialization) {
        initializeRawSymPruning(
                middleCornerPermPruning,
                middlePermMove, middlePermConjugate,
                cornerPermMove, CubieCube.symStatePerm, 0x8ea34,
                fullInitialization
        );
    }

    // Initialize edge permutation and corner combination permutation pruning table
    static void initializePermCombPermPruning(boolean fullInitialization) {
        initializeRawSymPruning(
                edgePermCornerCombPermPruning,
                cornerCombPermMove, cornerCombPermConjugate,
                edgePermMove, CubieCube.symStatePerm, 0x7d824,
                fullInitialization
        );
    }

    int twist;
    int twistSym;
    int flip;
    int flipSym;
    int slice;
    int pruning;

    int twistConjugate;
    int flipConjugate;

    CoordCube() { }

    void set(CoordCube node) {
        this.twist = node.twist;
        this.twistSym = node.twistSym;
        this.flip = node.flip;
        this.flipSym = node.flipSym;
        this.slice = node.slice;
        this.pruning = node.pruning;

        if (Search.USE_CONJ_PRUN) {
            this.twistConjugate = node.twistConjugate;
            this.flipConjugate = node.flipConjugate;
        }
    }

    void calculatePruning(boolean isPhase1) {
        pruning = Math.max(
                Math.max(
                        getPruning(udSliceTwistPruning,
                                twist * NUM_SLICE + udSliceConjugate[slice][twistSym]),
                        getPruning(udSliceFlipPruning,
                                flip * NUM_SLICE + udSliceConjugate[slice][flipSym])),
                Math.max(
                        Search.USE_CONJ_PRUN ? getPruning(twistFlipPruning,
                                (twistConjugate >> 3) << 11 | CubieCube.flipSymToRawFlip[flipConjugate ^ (twistConjugate & 7)]) : 0,
                        Search.USE_TWIST_FLIP_PRUN ? getPruning(twistFlipPruning,
                                twist << 11 | CubieCube.flipSymToRawFlip[flip << 3 | (flipSym ^ twistSym)]) : 0));
    }

    boolean setWithPruning(CubieCube cube, int depth) {
        twist = cube.getTwistSym();
        flip = cube.getFlipSym();
        twistSym = twist & 7;
        twist = twist >> 3;

        pruning = Search.USE_TWIST_FLIP_PRUN ? getPruning(twistFlipPruning,
                twist << 11 | CubieCube.flipSymToRawFlip[flip ^ twistSym]) : 0;
        if (pruning > depth) {
            return false;
        }

        flipSym = flip & 7;
        flip = flip >> 3;

        slice = cube.getUdSlice();
        pruning = Math.max(pruning, Math.max(
                getPruning(udSliceTwistPruning,
                        twist * NUM_SLICE + udSliceConjugate[slice][twistSym]),
                getPruning(udSliceFlipPruning,
                        flip * NUM_SLICE + udSliceConjugate[slice][flipSym])));
        if (pruning > depth) {
            return false;
        }

        if (Search.USE_CONJ_PRUN) {
            CubieCube conjugateCube = new CubieCube();
            CubieCube.cornerConjugate(cube, 1, conjugateCube);
            CubieCube.edgeConjugate(cube, 1, conjugateCube);
            twistConjugate = conjugateCube.getTwistSym();
            flipConjugate = conjugateCube.getFlipSym();
            pruning = Math.max(pruning,
                    getPruning(twistFlipPruning,
                            (twistConjugate >> 3) << 11 | CubieCube.flipSymToRawFlip[flipConjugate ^ (twistConjugate & 7)]));
        }

        return pruning <= depth;
    }

    int performMovePruning(CoordCube node, int move, boolean isPhase1) {
        slice = udSliceMove[node.slice][move];

        flip = flipMove[node.flip][CubieCube.sym8Move[move << 3 | node.flipSym]];
        flipSym = (flip & 7) ^ node.flipSym;
        flip >>= 3;

        twist = twistMove[node.twist][CubieCube.sym8Move[move << 3 | node.twistSym]];
        twistSym = (twist & 7) ^ node.twistSym;
        twist >>= 3;

        pruning = Math.max(
                Math.max(
                        getPruning(udSliceTwistPruning,
                                twist * NUM_SLICE + udSliceConjugate[slice][twistSym]),
                        getPruning(udSliceFlipPruning,
                                flip * NUM_SLICE + udSliceConjugate[slice][flipSym])),
                Search.USE_TWIST_FLIP_PRUN ? getPruning(twistFlipPruning,
                        twist << 11 | CubieCube.flipSymToRawFlip[flip << 3 | (flipSym ^ twistSym)]) : 0);
        return pruning;
    }

    int performMovePruningConjugate(CoordCube node, int move) {
        move = CubieCube.symMove[3][move];
        flipConjugate = flipMove[node.flipConjugate >> 3][CubieCube.sym8Move[move << 3 | node.flipConjugate & 7]] ^ (node.flipConjugate & 7);
        twistConjugate = twistMove[node.twistConjugate >> 3][CubieCube.sym8Move[move << 3 | node.twistConjugate & 7]] ^ (node.twistConjugate & 7);
        return getPruning(twistFlipPruning,
                (twistConjugate >> 3) << 11 | CubieCube.flipSymToRawFlip[flipConjugate ^ (twistConjugate & 7)]);
    }
}
