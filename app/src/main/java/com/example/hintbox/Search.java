package com.example.hintbox;

public class Search {
    public static final boolean USE_TWIST_FLIP_PRUN = true;

    static final int MAX_PRE_MOVES = 20;
    static final boolean TRY_INVERSE = true;
    static final boolean TRY_THREE_AXES = true;

    static final boolean USE_COMBP_PRUN = USE_TWIST_FLIP_PRUN;
    static final boolean USE_CONJ_PRUN = USE_TWIST_FLIP_PRUN;
    protected static int MIN_P1LENGTH_PRE = 7;
    protected static int MAX_DEPTH2 = 12;

    static boolean inited = false;

    protected int[] move = new int[31];

    protected CoordCube[] nodeUD = new CoordCube[21];
    protected CoordCube[] nodeRL = new CoordCube[21];
    protected CoordCube[] nodeFB = new CoordCube[21];

    protected long selfSym;
    protected int conjMask;
    protected int urfIdx;
    protected int length1;
    protected int depth1;
    protected int maxDep2;
    protected int solLen;
    protected Util.Solution solution;
    protected long probe;
    protected long probeMax;
    protected long probeMin;
    protected int verbose;
    protected int valid1;
    protected boolean allowShorter = false;
    protected CubieCube cc = new CubieCube();
    protected CubieCube[] urfCubieCube = new CubieCube[6];
    protected CoordCube[] urfCoordCube = new CoordCube[6];
    protected CubieCube[] phase1Cubie = new CubieCube[21];

    CubieCube[] preMoveCubes = new CubieCube[MAX_PRE_MOVES + 1];
    int[] preMoves = new int[MAX_PRE_MOVES];
    int preMoveLen = 0;
    int maxPreMoves = 0;

    protected boolean isRec = false;

    public static final int USE_SEPARATOR = 0x1;
    public static final int INVERSE_SOLUTION = 0x2;
    public static final int APPEND_LENGTH = 0x4;
    public static final int OPTIMAL_SOLUTION = 0x8;

    public Search() {
        for (int i = 0; i < 21; i++) {
            nodeUD[i] = new CoordCube();
            nodeRL[i] = new CoordCube();
            nodeFB[i] = new CoordCube();
            phase1Cubie[i] = new CubieCube();
        }
        for (int i = 0; i < 6; i++) {
            urfCubieCube[i] = new CubieCube();
            urfCoordCube[i] = new CoordCube();
        }
        for (int i = 0; i < MAX_PRE_MOVES; i++) {
            preMoveCubes[i + 1] = new CubieCube();
        }
    }

    public synchronized String solution(String facelets, int maxDepth, long probeMax, long probeMin, int verbose) {
        try {
            initializeSearchParameters(facelets, maxDepth, probeMax, probeMin, verbose);
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
        return (verbose & OPTIMAL_SOLUTION) == 0 ? search() : searchopt();
    }

    protected void initSearch() {
        conjMask = (TRY_INVERSE ? 0 : 0x38) | (TRY_THREE_AXES ? 0 : 0x36);
        selfSym = cc.selfSymmetry();
        conjMask |= (selfSym >> 16 & 0xffff) != 0 ? 0x12 : 0;
        conjMask |= (selfSym >> 32 & 0xffff) != 0 ? 0x24 : 0;
        conjMask |= (selfSym >> 48 & 0xffff) != 0 ? 0x38 : 0;
        selfSym &= 0xffffffffffffL;
        maxPreMoves = conjMask > 7 ? 0 : MAX_PRE_MOVES;

        for (int i = 0; i < 6; i++) {
            urfCubieCube[i].copy(cc);
            urfCoordCube[i].setWithPrun(urfCubieCube[i], 20);
            cc.URFConjugate();
            if (i % 3 == 2) {
                cc.invCubieCube();
            }
        }
    }

    public synchronized String next(long probeMax, long probeMin, int verbose) {
        this.probe = 0;
        this.probeMax = probeMax;
        this.probeMin = Math.min(probeMin, probeMax);
        this.solution = null;
        this.isRec = (this.verbose & OPTIMAL_SOLUTION) == (verbose & OPTIMAL_SOLUTION);
        this.verbose = verbose;
        return (verbose & OPTIMAL_SOLUTION) == 0 ? search() : searchopt();
    }

    public static boolean isInited() {
        return inited;
    }

    public long numberOfProbes() {
        return probe;
    }

    public int length() {
        return solLen;
    }

    public synchronized static void init() {
        CoordCube.init(true);
        inited = true;
    }

    int verify(String facelets) {
        int count = 0x000000;
        byte[] f = new byte[54];
        try {
            String center = new String(
                    new char[] {
                            facelets.charAt(Util.U5),
                            facelets.charAt(Util.R5),
                            facelets.charAt(Util.F5),
                            facelets.charAt(Util.D5),
                            facelets.charAt(Util.L5),
                            facelets.charAt(Util.B5)
                    }
            );
            for (int i = 0; i < 54; i++) {
                f[i] = (byte) center.indexOf(facelets.charAt(i));
                if (f[i] == -1) {
                    return -1;
                }
                count += 1 << (f[i] << 2);
            }
        } catch (Exception e) {
            return -1;
        }
        if (count != 0x999999) {
            return -1;
        }
        Util.toCubieCube(f, cc);
        return cc.verify();
    }

    protected int phase1PreMoves(int maxl, int lm, CubieCube cc, int ssym) {
        preMoveLen = maxPreMoves - maxl;
        if (isRec ? depth1 == length1 - preMoveLen
                : (preMoveLen == 0 || (0x36FB7 >> lm & 1) == 0)) {
            depth1 = length1 - preMoveLen;
            phase1Cubie[0] = cc;
            allowShorter = depth1 == MIN_P1LENGTH_PRE && preMoveLen != 0;

            if (nodeUD[depth1 + 1].setWithPrun(cc, depth1)
                    && phase1(nodeUD[depth1 + 1], ssym, depth1, -1) == 0) {
                return 0;
            }
        }

        if (maxl == 0 || preMoveLen + MIN_P1LENGTH_PRE >= length1) {
            return 1;
        }

        int skipMoves = CubieCube.getSkipMoves(ssym);
        if (maxl == 1 || preMoveLen + 1 + MIN_P1LENGTH_PRE >= length1) { //last pre move
            skipMoves |= 0x36FB7; // 11 0110 1111 1011 0111
        }

        lm = lm / 3 * 3;
        for (int m = 0; m < 18; m++) {
            if (m == lm || m == lm - 9 || m == lm + 9) {
                m += 2;
                continue;
            }
            if (isRec && m != preMoves[maxPreMoves - maxl] || (skipMoves & 1 << m) != 0) {
                continue;
            }
            CubieCube.CornMult(CubieCube.moveCube[m], cc, preMoveCubes[maxl]);
            CubieCube.EdgeMult(CubieCube.moveCube[m], cc, preMoveCubes[maxl]);
            preMoves[maxPreMoves - maxl] = m;
            int ret = phase1PreMoves(maxl - 1, m, preMoveCubes[maxl], ssym & (int) CubieCube.moveCubeSym[m]);
            if (ret == 0) {
                return 0;
            }
        }
        return 1;
    }

    protected String search() {
        for (length1 = isRec ? length1 : 0; length1 < solLen; length1++) {
            maxDep2 = Math.min(MAX_DEPTH2, solLen - length1 - 1);
            for (urfIdx = isRec ? urfIdx : 0; urfIdx < 6; urfIdx++) {
                if ((conjMask & 1 << urfIdx) != 0) {
                    continue;
                }
                if (phase1PreMoves(maxPreMoves, -30, urfCubieCube[urfIdx], (int) (selfSym & 0xffff)) == 0) {
                    return solution == null ? "Решение не найдено" : solution.toString();
                }
            }
        }
        return solution == null ? "Error 7" : solution.toString();
    }

    protected int initPhase2Pre() {
        isRec = false;
        if (probe >= (solution == null ? probeMax : probeMin)) {
            return 0;
        }
        ++probe;

        for (int i = valid1; i < depth1; i++) {
            CubieCube.CornMult(phase1Cubie[i], CubieCube.moveCube[move[i]], phase1Cubie[i + 1]);
            CubieCube.EdgeMult(phase1Cubie[i], CubieCube.moveCube[move[i]], phase1Cubie[i + 1]);
        }
        valid1 = depth1;

        int p2corn = phase1Cubie[depth1].getCPermSym();
        int p2csym = p2corn & 0xf;
        p2corn >>= 4;
        int p2edge = phase1Cubie[depth1].getEPermSym();
        int p2esym = p2edge & 0xf;
        p2edge >>= 4;
        int p2mid = phase1Cubie[depth1].getMPerm();
        int edgei = CubieCube.getPermSymInv(p2edge, p2esym, false);
        int corni = CubieCube.getPermSymInv(p2corn, p2csym, true);

        int lastMove = depth1 == 0 ? -1 : move[depth1 - 1];
        int lastPre = preMoveLen == 0 ? -1 : preMoves[preMoveLen - 1];

        int ret = 0;
        int p2switchMax = (preMoveLen == 0 ? 1 : 2) * (depth1 == 0 ? 1 : 2);
        for (int p2switch = 0, p2switchMask = (1 << p2switchMax) - 1;
             p2switch < p2switchMax; p2switch++) {
            // 0 normal; 1 lastmove; 2 lastmove + premove; 3 premove
            if ((p2switchMask >> p2switch & 1) != 0) {
                p2switchMask &= ~(1 << p2switch);
                ret = initPhase2(p2corn, p2csym, p2edge, p2esym, p2mid, edgei, corni);
                if (ret == 0 || ret > 2) {
                    break;
                } else if (ret == 2) {
                    p2switchMask &= 0x4 << p2switch; // 0->2; 1=>3; 2=>N/A
                }
            }
            if (p2switchMask == 0) {
                break;
            }
            if ((p2switch & 1) == 0 && depth1 > 0) {
                int m = Util.std2ud[lastMove / 3 * 3 + 1];
                move[depth1 - 1] = Util.ud2std[m] * 2 - move[depth1 - 1];

                p2mid = CoordCube.MPermMove[p2mid][m];
                p2corn = CoordCube.CPermMove[p2corn][CubieCube.SymMoveUD[p2csym][m]];
                p2csym = CubieCube.SymMult[p2corn & 0xf][p2csym];
                p2corn >>= 4;
                p2edge = CoordCube.EPermMove[p2edge][CubieCube.SymMoveUD[p2esym][m]];
                p2esym = CubieCube.SymMult[p2edge & 0xf][p2esym];
                p2edge >>= 4;
                corni = CubieCube.getPermSymInv(p2corn, p2csym, true);
                edgei = CubieCube.getPermSymInv(p2edge, p2esym, false);
            } else if (preMoveLen > 0) {
                int m = Util.std2ud[lastPre / 3 * 3 + 1];
                preMoves[preMoveLen - 1] = Util.ud2std[m] * 2 - preMoves[preMoveLen - 1];

                p2mid = CubieCube.MPermInv[CoordCube.MPermMove[CubieCube.MPermInv[p2mid]][m]];
                p2corn = CoordCube.CPermMove[corni >> 4][CubieCube.SymMoveUD[corni & 0xf][m]];
                corni = p2corn & ~0xf | CubieCube.SymMult[p2corn & 0xf][corni & 0xf];
                p2corn = CubieCube.getPermSymInv(corni >> 4, corni & 0xf, true);
                p2csym = p2corn & 0xf;
                p2corn >>= 4;
                p2edge = CoordCube.EPermMove[edgei >> 4][CubieCube.SymMoveUD[edgei & 0xf][m]];
                edgei = p2edge & ~0xf | CubieCube.SymMult[p2edge & 0xf][edgei & 0xf];
                p2edge = CubieCube.getPermSymInv(edgei >> 4, edgei & 0xf, false);
                p2esym = p2edge & 0xf;
                p2edge >>= 4;
            }
        }
        if (depth1 > 0) {
            move[depth1 - 1] = lastMove;
        }
        if (preMoveLen > 0) {
            preMoves[preMoveLen - 1] = lastPre;
        }
        return ret == 0 ? 0 : 2;
    }

    protected int initPhase2(int p2corn, int p2csym, int p2edge, int p2esym, int p2mid, int edgei, int corni) {
        int prun = Math.max(
                CoordCube.getPruning(CoordCube.EPermCCombPPrun,
                        (edgei >> 4) * CoordCube.N_COMB + CoordCube.CCombPConj[CubieCube.Perm2CombP[corni >> 4] & 0xff][CubieCube.SymMultInv[edgei & 0xf][corni & 0xf]]),
                Math.max(
                        CoordCube.getPruning(CoordCube.EPermCCombPPrun,
                                p2edge * CoordCube.N_COMB + CoordCube.CCombPConj[CubieCube.Perm2CombP[p2corn] & 0xff][CubieCube.SymMultInv[p2esym][p2csym]]),
                        CoordCube.getPruning(CoordCube.MCPermPrun,
                                p2corn * CoordCube.N_MPERM + CoordCube.MPermConj[p2mid][p2csym])));

        if (prun > maxDep2) {
            return prun - maxDep2;
        }

        int depth2;
        for (depth2 = maxDep2; depth2 >= prun; depth2--) {
            int ret = phase2(p2edge, p2esym, p2corn, p2csym, p2mid, depth2, depth1, 10);
            if (ret < 0) {
                break;
            }
            depth2 -= ret;
            solLen = 0;
            solution = new Util.Solution();
            solution.setArgs(verbose, urfIdx, depth1);
            for (int i = 0; i < depth1 + depth2; i++) {
                solution.appendSolMove(move[i]);
            }
            for (int i = preMoveLen - 1; i >= 0; i--) {
                solution.appendSolMove(preMoves[i]);
            }
            solLen = solution.length;
        }

        if (depth2 != maxDep2) { //At least one solution has been found.
            maxDep2 = Math.min(MAX_DEPTH2, solLen - length1 - 1);
            return probe >= probeMin ? 0 : 1;
        }
        return 1;
    }

    protected int phase1(CoordCube node, int ssym, int maxl, int lm) {
        if (node.prun == 0 && maxl < 5) {
            if (allowShorter || maxl == 0) {
                depth1 -= maxl;
                int ret = initPhase2Pre();
                depth1 += maxl;
                return ret;
            } else {
                return 1;
            }
        }

        int skipMoves = CubieCube.getSkipMoves(ssym);

        for (int axis = 0; axis < 18; axis += 3) {
            if (axis == lm || axis == lm - 9) {
                continue;
            }
            for (int power = 0; power < 3; power++) {
                int m = axis + power;

                if (isRec && m != move[depth1 - maxl]
                        || skipMoves != 0 && (skipMoves & 1 << m) != 0) {
                    continue;
                }

                int prun = nodeUD[maxl].doMovePrun(node, m, true);
                if (prun > maxl) {
                    break;
                } else if (prun == maxl) {
                    continue;
                }

                if (USE_CONJ_PRUN) {
                    prun = nodeUD[maxl].doMovePrunConj(node, m);
                    if (prun > maxl) {
                        break;
                    } else if (prun == maxl) {
                        continue;
                    }
                }

                move[depth1 - maxl] = m;
                valid1 = Math.min(valid1, depth1 - maxl);
                int ret = phase1(nodeUD[maxl], ssym & (int) CubieCube.moveCubeSym[m], maxl - 1, axis);
                if (ret == 0) {
                    return 0;
                } else if (ret >= 2) {
                    break;
                }
            }
        }
        return 1;
    }

    protected String searchopt() {
        int maxprun1 = 0;
        int maxprun2 = 0;
        for (int i = 0; i < 6; i++) {
            urfCoordCube[i].calcPruning(false);
            if (i < 3) {
                maxprun1 = Math.max(maxprun1, urfCoordCube[i].prun);
            } else {
                maxprun2 = Math.max(maxprun2, urfCoordCube[i].prun);
            }
        }
        urfIdx = maxprun2 > maxprun1 ? 3 : 0;
        phase1Cubie[0] = urfCubieCube[urfIdx];
        for (length1 = isRec ? length1 : 0; length1 < solLen; length1++) {
            CoordCube ud = urfCoordCube[0 + urfIdx];
            CoordCube rl = urfCoordCube[1 + urfIdx];
            CoordCube fb = urfCoordCube[2 + urfIdx];

            if (ud.prun <= length1 && rl.prun <= length1 && fb.prun <= length1
                    && phase1opt(ud, rl, fb, selfSym, length1, -1) == 0) {
                return solution == null ? "Error 8" : solution.toString();
            }
        }
        return solution == null ? "Error 7" : solution.toString();
    }

    protected int phase1opt(CoordCube ud, CoordCube rl, CoordCube fb, long ssym, int maxl, int lm) {
        if (ud.prun == 0 && rl.prun == 0 && fb.prun == 0 && maxl < 5) {
            maxDep2 = maxl;
            depth1 = length1 - maxl;
            return initPhase2Pre() == 0 ? 0 : 1;
        }

        int skipMoves = CubieCube.getSkipMoves(ssym);

        for (int axis = 0; axis < 18; axis += 3) {
            if (axis == lm || axis == lm - 9) {
                continue;
            }
            for (int power = 0; power < 3; power++) {
                int m = axis + power;

                if (isRec && m != move[length1 - maxl]
                        || skipMoves != 0 && (skipMoves & 1 << m) != 0) {
                    continue;
                }

                // UD Axis
                int prun_ud = Math.max(nodeUD[maxl].doMovePrun(ud, m, false),
                        USE_CONJ_PRUN ? nodeUD[maxl].doMovePrunConj(ud, m) : 0);
                if (prun_ud > maxl) {
                    break;
                } else if (prun_ud == maxl) {
                    continue;
                }

                // RL Axis
                m = CubieCube.urfMove[2][m];

                int prun_rl = Math.max(nodeRL[maxl].doMovePrun(rl, m, false),
                        USE_CONJ_PRUN ? nodeRL[maxl].doMovePrunConj(rl, m) : 0);
                if (prun_rl > maxl) {
                    break;
                } else if (prun_rl == maxl) {
                    continue;
                }

                // FB Axis
                m = CubieCube.urfMove[2][m];

                int prun_fb = Math.max(nodeFB[maxl].doMovePrun(fb, m, false),
                        USE_CONJ_PRUN ? nodeFB[maxl].doMovePrunConj(fb, m) : 0);
                if (prun_ud == prun_rl && prun_rl == prun_fb && prun_fb != 0) {
                    prun_fb++;
                }

                if (prun_fb > maxl) {
                    break;
                } else if (prun_fb == maxl) {
                    continue;
                }

                m = CubieCube.urfMove[2][m];

                move[length1 - maxl] = m;
                valid1 = Math.min(valid1, length1 - maxl);
                int ret = phase1opt(nodeUD[maxl], nodeRL[maxl], nodeFB[maxl], ssym & CubieCube.moveCubeSym[m], maxl - 1, axis);
                if (ret == 0) {
                    return 0;
                }
            }
        }
        return 1;
    }

    protected int phase2(int edge, int esym, int corn, int csym, int mid, int maxl, int depth, int lm) {
        if (edge == 0 && corn == 0 && mid == 0) {
            return maxl;
        }
        int moveMask = Util.ckmv2bit[lm];
        for (int m = 0; m < 10; m++) {
            if ((moveMask >> m & 1) != 0) {
                m += 0x42 >> m & 3;
                continue;
            }
            int midx = CoordCube.MPermMove[mid][m];
            int cornx = CoordCube.CPermMove[corn][CubieCube.SymMoveUD[csym][m]];
            int csymx = CubieCube.SymMult[cornx & 0xf][csym];
            cornx >>= 4;
            int edgex = CoordCube.EPermMove[edge][CubieCube.SymMoveUD[esym][m]];
            int esymx = CubieCube.SymMult[edgex & 0xf][esym];
            edgex >>= 4;
            int edgei = CubieCube.getPermSymInv(edgex, esymx, false);
            int corni = CubieCube.getPermSymInv(cornx, csymx, true);

            int prun = CoordCube.getPruning(CoordCube.EPermCCombPPrun,
                    (edgei >> 4) * CoordCube.N_COMB + CoordCube.CCombPConj[CubieCube.Perm2CombP[corni >> 4] & 0xff][CubieCube.SymMultInv[edgei & 0xf][corni & 0xf]]);
            if (prun > maxl + 1) {
                return maxl - prun + 1;
            } else if (prun >= maxl) {
                m += 0x42 >> m & 3 & (maxl - prun);
                continue;
            }
            prun = Math.max(
                    CoordCube.getPruning(CoordCube.MCPermPrun,
                            cornx * CoordCube.N_MPERM + CoordCube.MPermConj[midx][csymx]),
                    CoordCube.getPruning(CoordCube.EPermCCombPPrun,
                            edgex * CoordCube.N_COMB + CoordCube.CCombPConj[CubieCube.Perm2CombP[cornx] & 0xff][CubieCube.SymMultInv[esymx][csymx]]));
            if (prun >= maxl) {
                m += 0x42 >> m & 3 & (maxl - prun);
                continue;
            }
            int ret = phase2(edgex, esymx, cornx, csymx, midx, maxl - 1, depth + 1, m);
            if (ret >= 0) {
                move[depth] = Util.ud2std[m];
                return ret;
            }
            if (ret < -2) {
                break;
            }
            if (ret < -1) {
                m += 0x42 >> m & 3;
            }
        }
        return -1;
    }

    private void initializeSearchParameters(String facelets, int maxDepth, long probeMax, long probeMin, int verbose) {
        int check = verify(facelets);
        if (check != 0) {
            throw new IllegalArgumentException("Error " + Math.abs(check));
        }
        this.solLen = maxDepth + 1;
        this.probe = 0;
        this.probeMax = probeMax;
        this.probeMin = Math.min(probeMin, probeMax);
        this.verbose = verbose;
        this.solution = null;
        this.isRec = false;

        CoordCube.init(false);
        initSearch();
    }
}
